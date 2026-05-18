const admin = require("firebase-admin");
const logger = require("firebase-functions/logger");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { FieldValue } = require("firebase-admin/firestore");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

function normalizeString(value) {
  return typeof value === "string" ? value.trim() : "";
}

function animalStatsDocument(animalId) {
  return db.collection("animalStats").doc(animalId);
}

function contributionDocument(ownerUid, findingId) {
  return db.collection("globalFindingContributions")
    .doc(`${ownerUid}_${findingId}`);
}

function contributionPayload(ownerUid, findingId, animalId) {
  return {
    ownerUid,
    findingId,
    animalId,
    updatedAt: FieldValue.serverTimestamp(),
  };
}

async function adjustAnimalStatsCount(transaction, animalId, delta) {
  const normalizedAnimalId = normalizeString(animalId);
  if (!normalizedAnimalId || delta === 0) {
    return null;
  }

  const statsRef = animalStatsDocument(normalizedAnimalId);
  const statsSnapshot = await transaction.get(statsRef);
  const currentCount = Number(statsSnapshot.get("globalFindingCount") || 0);
  const nextCount = Math.max(0, currentCount + delta);

  transaction.set(statsRef, {
    animalId: normalizedAnimalId,
    globalFindingCount: nextCount,
    updatedAt: FieldValue.serverTimestamp(),
  });

  return {
    animalId: normalizedAnimalId,
    previousCount: currentCount,
    nextCount,
    delta,
  };
}

async function syncGlobalStatsForFindingWrite(ownerUid, findingId, beforeData, afterData) {
  const normalizedOwnerUid = normalizeString(ownerUid);
  const normalizedFindingId = normalizeString(findingId);
  const contributionId = `${normalizedOwnerUid}_${normalizedFindingId}`;
  const contributionRef = contributionDocument(normalizedOwnerUid, normalizedFindingId);

  const eventType = beforeData && afterData
    ? "update"
    : afterData
      ? "create"
      : beforeData
        ? "delete"
        : "noop";

  return db.runTransaction(async (transaction) => {
    const contributionSnapshot = await transaction.get(contributionRef);
    const contributionExists = contributionSnapshot.exists;
    const contributionAnimalId = normalizeString(contributionSnapshot.get("animalId"));
    const oldAnimalId = contributionAnimalId || normalizeString(beforeData?.animalId);
    const newAnimalId = normalizeString(afterData?.animalId);

    const result = {
      ownerUid: normalizedOwnerUid,
      findingId: normalizedFindingId,
      contributionId,
      eventType,
      oldAnimalId,
      newAnimalId,
      contributionExists,
      action: "noop",
      statsChanges: [],
    };

    if (eventType === "create") {
      if (!newAnimalId) {
        result.action = "skipped_missing_animalId";
        return result;
      }

      if (!contributionExists) {
        const statsChange = await adjustAnimalStatsCount(transaction, newAnimalId, 1);
        if (statsChange) {
          result.statsChanges.push(statsChange);
        }
        transaction.set(
          contributionRef,
          contributionPayload(normalizedOwnerUid, normalizedFindingId, newAnimalId)
        );
        result.action = "created_contribution";
        return result;
      }

      if (contributionAnimalId && contributionAnimalId !== newAnimalId) {
        const decrementChange = await adjustAnimalStatsCount(transaction, contributionAnimalId, -1);
        const incrementChange = await adjustAnimalStatsCount(transaction, newAnimalId, 1);
        if (decrementChange) {
          result.statsChanges.push(decrementChange);
        }
        if (incrementChange) {
          result.statsChanges.push(incrementChange);
        }
        transaction.set(
          contributionRef,
          contributionPayload(normalizedOwnerUid, normalizedFindingId, newAnimalId)
        );
        result.action = "corrected_existing_contribution";
        return result;
      }

      transaction.set(
        contributionRef,
        contributionPayload(normalizedOwnerUid, normalizedFindingId, newAnimalId),
        { merge: true }
      );
      result.action = "existing_contribution_kept";
      return result;
    }

    if (eventType === "delete") {
      if (!contributionExists) {
        result.action = "missing_contribution";
        return result;
      }

      if (oldAnimalId) {
        const statsChange = await adjustAnimalStatsCount(transaction, oldAnimalId, -1);
        if (statsChange) {
          result.statsChanges.push(statsChange);
        }
      }
      transaction.delete(contributionRef);
      result.action = "deleted_contribution";
      return result;
    }

    if (eventType === "update") {
      if (!newAnimalId) {
        result.action = "skipped_missing_new_animalId";
        return result;
      }

      if (!contributionExists) {
        const statsChange = await adjustAnimalStatsCount(transaction, newAnimalId, 1);
        if (statsChange) {
          result.statsChanges.push(statsChange);
        }
        transaction.set(
          contributionRef,
          contributionPayload(normalizedOwnerUid, normalizedFindingId, newAnimalId)
        );
        result.action = "created_missing_contribution";
        return result;
      }

      if (!oldAnimalId || oldAnimalId === newAnimalId) {
        transaction.set(
          contributionRef,
          contributionPayload(normalizedOwnerUid, normalizedFindingId, newAnimalId),
          { merge: true }
        );
        result.action = "updated_same_animal";
        return result;
      }

      const decrementChange = await adjustAnimalStatsCount(transaction, oldAnimalId, -1);
      const incrementChange = await adjustAnimalStatsCount(transaction, newAnimalId, 1);
      if (decrementChange) {
        result.statsChanges.push(decrementChange);
      }
      if (incrementChange) {
        result.statsChanges.push(incrementChange);
      }
      transaction.set(
        contributionRef,
        contributionPayload(normalizedOwnerUid, normalizedFindingId, newAnimalId)
      );
      result.action = "moved_contribution";
      return result;
    }

    return result;
  });
}

async function rebuildGlobalFindingStatsInternal() {
  const findingsSnapshot = await db.collectionGroup("findings").get();
  const contributionEntries = [];
  const animalCountByAnimalId = new Map();
  const ownerUidSet = new Set();

  findingsSnapshot.docs.forEach((documentSnapshot) => {
    const ownerUid = normalizeString(documentSnapshot.ref.parent.parent?.id);
    const findingId = normalizeString(documentSnapshot.id);
    const animalId = normalizeString(documentSnapshot.get("animalId"));

    if (!ownerUid || !findingId || !animalId) {
      logger.warn("GlobalStatsFunction rebuild skipped invalid finding", {
        ownerUid,
        findingId,
        animalId,
        path: documentSnapshot.ref.path,
      });
      return;
    }

    ownerUidSet.add(ownerUid);
    contributionEntries.push({
      contributionId: `${ownerUid}_${findingId}`,
      ownerUid,
      findingId,
      animalId,
    });
    animalCountByAnimalId.set(animalId, (animalCountByAnimalId.get(animalId) || 0) + 1);
  });

  const existingAnimalStatsSnapshot = await db.collection("animalStats").get();
  const existingAnimalIds = existingAnimalStatsSnapshot.docs
    .map((documentSnapshot) => normalizeString(documentSnapshot.get("animalId")))
    .filter(Boolean);
  const staleAnimalIdsWithoutContributions = existingAnimalIds
    .filter((animalId) => !animalCountByAnimalId.has(animalId))
    .sort();

  const contributionWriter = db.bulkWriter();
  contributionEntries.forEach((entry) => {
    contributionWriter.set(
      db.collection("globalFindingContributions").doc(entry.contributionId),
      contributionPayload(entry.ownerUid, entry.findingId, entry.animalId)
    );
  });
  await contributionWriter.close();

  const statsWriter = db.bulkWriter();
  const sortedAnimalEntries = [...animalCountByAnimalId.entries()].sort((left, right) => {
    return left[0].localeCompare(right[0]);
  });
  sortedAnimalEntries.forEach(([animalId, count]) => {
    logger.info("GlobalStatsFunction rebuild animal count", {
      animalId,
      globalFindingCount: count,
    });
    statsWriter.set(animalStatsDocument(animalId), {
      animalId,
      globalFindingCount: count,
      updatedAt: FieldValue.serverTimestamp(),
    });
  });
  await statsWriter.close();

  staleAnimalIdsWithoutContributions.forEach((animalId) => {
    logger.info("GlobalStatsFunction rebuild stale animalStats logged only", {
      animalId,
    });
  });

  return {
    userCount: ownerUidSet.size,
    findingCount: contributionEntries.length,
    contributionWriteCount: contributionEntries.length,
    affectedAnimalCount: sortedAnimalEntries.length,
    animalCounts: Object.fromEntries(sortedAnimalEntries),
    animalStatsWriteCount: sortedAnimalEntries.length,
    staleAnimalIdsWithoutContributions,
  };
}

exports.syncGlobalFindingStatsOnFindingWrite = onDocumentWritten(
  "users/{ownerUid}/findings/{findingId}",
  async (event) => {
    const ownerUid = normalizeString(event.params.ownerUid);
    const findingId = normalizeString(event.params.findingId);
    const beforeData = event.data?.before?.exists ? event.data.before.data() : null;
    const afterData = event.data?.after?.exists ? event.data.after.data() : null;

    try {
      const result = await syncGlobalStatsForFindingWrite(ownerUid, findingId, beforeData, afterData);
      logger.info("GlobalStatsFunction finding write synced", result);
    } catch (error) {
      logger.error("GlobalStatsFunction finding write failed", {
        ownerUid,
        findingId,
        eventType: beforeData && afterData ? "update" : afterData ? "create" : beforeData ? "delete" : "noop",
        error: error instanceof Error ? error.message : String(error),
      });
      throw error;
    }
  }
);

exports.rebuildGlobalFindingStats = onCall(async (request) => {
  const adminUid = normalizeString(process.env.GLOBAL_STATS_ADMIN_UID);
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Authentifizierung erforderlich.");
  }
  if (!adminUid) {
    throw new HttpsError(
      "failed-precondition",
      "GLOBAL_STATS_ADMIN_UID ist nicht gesetzt."
    );
  }
  if (request.auth.uid !== adminUid) {
    throw new HttpsError("permission-denied", "Nur die konfigurierte Admin-UID darf den Rebuild ausführen.");
  }

  try {
    const result = await rebuildGlobalFindingStatsInternal();
    logger.info("GlobalStatsFunction rebuild completed", result);
    return result;
  } catch (error) {
    logger.error("GlobalStatsFunction rebuild failed", {
      error: error instanceof Error ? error.message : String(error),
    });
    throw new HttpsError("internal", "GlobalStats-Rebuild fehlgeschlagen.");
  }
});
