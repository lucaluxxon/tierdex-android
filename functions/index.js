const admin = require("firebase-admin");
const logger = require("firebase-functions/logger");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { FieldValue } = require("firebase-admin/firestore");
const crypto = require("crypto");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const NOTIFICATION_SCHEMA_VERSION = 1;

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

function notificationDocument(recipientUid, notificationId) {
  return db.collection("users")
    .doc(recipientUid)
    .collection("notifications")
    .doc(notificationId);
}

function normalizeCoordinate(value) {
  return typeof value === "number" && Number.isFinite(value)
    ? value.toFixed(6)
    : "";
}

function normalizeTaggedFriendIds(value) {
  if (!Array.isArray(value)) return "";
  return value
    .map((entry) => normalizeString(entry))
    .filter(Boolean)
    .sort()
    .filter((entry, index, array) => index === 0 || array[index - 1] !== entry)
    .join(",");
}

function findingFingerprintFromData(ownerUid, findingId, findingData) {
  if (!findingData || typeof findingData !== "object") {
    return normalizeString(findingId);
  }
  const parts = [
    normalizeString(ownerUid),
    normalizeString(findingData.animalId),
    normalizeString(findingData.date),
    normalizeString(findingData.location),
    normalizeString(findingData.note),
    normalizeCoordinate(findingData.latitude),
    normalizeCoordinate(findingData.longitude),
    normalizeTaggedFriendIds(findingData.taggedFriendIds),
  ];
  return crypto.createHash("sha256")
    .update(parts.join("|"))
    .digest("hex");
}

function friendRequestNotificationId(recipientUid, requesterUid) {
  return `friend_request_${normalizeString(requesterUid)}_${normalizeString(recipientUid)}`;
}

function likeNotificationId(ownerUid, stableFindingId, likerUid, fallbackLikeId) {
  const stableActorId = normalizeString(likerUid) || normalizeString(fallbackLikeId);
  return `like_${normalizeString(ownerUid)}_${normalizeString(stableFindingId)}_${stableActorId}`;
}

function legacyLikeNotificationId(findingId, likeDocumentId) {
  return `like_${normalizeString(findingId)}_${normalizeString(likeDocumentId)}`;
}

function commentNotificationId(ownerUid, stableFindingId, commentDocumentId) {
  return `comment_${normalizeString(ownerUid)}_${normalizeString(stableFindingId)}_${normalizeString(commentDocumentId)}`;
}

function legacyCommentNotificationId(findingId, commentDocumentId) {
  return `comment_${normalizeString(findingId)}_${normalizeString(commentDocumentId)}`;
}

async function loadPublicDisplayName(userId) {
  const normalizedUserId = normalizeString(userId);
  if (!normalizedUserId) return "";
  try {
    const snapshot = await db.collection("users").doc(normalizedUserId).get();
    return normalizeString(snapshot.get("displayName"));
  } catch (error) {
    logger.warn("NotificationFunction displayName lookup failed", {
      userId: normalizedUserId,
      error: error instanceof Error ? error.message : String(error),
    });
    return "";
  }
}

async function loadFindingContext(ownerUid, findingId) {
  const normalizedOwnerUid = normalizeString(ownerUid);
  const normalizedFindingId = normalizeString(findingId);
  if (!normalizedOwnerUid || !normalizedFindingId) {
    return {
      stableFindingId: normalizedFindingId,
      relatedAnimalId: "",
      sourcePath: "",
      exists: false,
    };
  }

  const findingRef = db.collection("users")
    .doc(normalizedOwnerUid)
    .collection("findings")
    .doc(normalizedFindingId);

  try {
    const findingSnapshot = await findingRef.get();
    const findingData = findingSnapshot.exists ? findingSnapshot.data() || {} : null;
    return {
      stableFindingId: findingFingerprintFromData(
        normalizedOwnerUid,
        normalizedFindingId,
        findingData
      ),
      relatedAnimalId: normalizeString(findingData?.animalId),
      sourcePath: findingRef.path,
      exists: findingSnapshot.exists,
    };
  } catch (error) {
    logger.warn("NotificationFunction finding context lookup failed", {
      ownerUid: normalizedOwnerUid,
      findingId: normalizedFindingId,
      error: error instanceof Error ? error.message : String(error),
    });
    return {
      stableFindingId: normalizedFindingId,
      relatedAnimalId: "",
      sourcePath: findingRef.path,
      exists: false,
    };
  }
}

async function upsertNotificationDocument(recipientUid, notificationId, payload) {
  await notificationDocument(recipientUid, notificationId).set(
    {
      ...payload,
      schemaVersion: NOTIFICATION_SCHEMA_VERSION,
      updatedAt: FieldValue.serverTimestamp(),
    },
    { merge: true }
  );
}

async function deleteNotificationDocument(recipientUid, notificationId) {
  await notificationDocument(recipientUid, notificationId).delete();
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

exports.syncFriendRequestNotification = onDocumentWritten(
  "users/{recipientUid}/friendRequestsIncoming/{fromUid}",
  async (event) => {
    const recipientUid = normalizeString(event.params.recipientUid);
    const fromUid = normalizeString(event.params.fromUid);
    const beforeData = event.data?.before?.exists ? event.data.before.data() : null;
    const afterData = event.data?.after?.exists ? event.data.after.data() : null;
    const notificationId = friendRequestNotificationId(recipientUid, fromUid);

    try {
      if (!recipientUid || !fromUid) {
        logger.warn("NotificationFunction friend_request skipped invalid params", {
          recipientUid,
          actorUid: fromUid,
          notificationId,
        });
        return;
      }

      if (!afterData || normalizeString(afterData.status) !== "pending") {
        await deleteNotificationDocument(recipientUid, notificationId);
        logger.info("NotificationFunction friend_request removed", {
          type: "friend_request",
          recipientUid,
          actorUid: fromUid,
          notificationId,
        });
        return;
      }

      const actorDisplayName = await loadPublicDisplayName(fromUid);
      await upsertNotificationDocument(recipientUid, notificationId, {
        type: "friend_request",
        title: "Neue Freundschaftsanfrage",
        message: `${actorDisplayName || "Jemand"} möchte dich als Freund hinzufügen.`,
        createdAt: afterData.createdAt || beforeData?.createdAt || FieldValue.serverTimestamp(),
        actorUid: fromUid,
        actorDisplayName,
        recipientUid,
        relatedRequestUserId: fromUid,
        sourcePath: event.data?.after?.ref?.path || event.data?.before?.ref?.path || "",
        legacyIds: [],
        isRead: false,
      });
      logger.info("NotificationFunction friend_request upserted", {
        type: "friend_request",
        recipientUid,
        actorUid: fromUid,
        notificationId,
      });
    } catch (error) {
      logger.error("NotificationFunction friend_request failed", {
        type: "friend_request",
        recipientUid,
        actorUid: fromUid,
        notificationId,
        error: error instanceof Error ? error.message : String(error),
      });
      throw error;
    }
  }
);

exports.syncLikeNotification = onDocumentWritten(
  "users/{ownerUid}/findings/{findingId}/likes/{likerUid}",
  async (event) => {
    const ownerUid = normalizeString(event.params.ownerUid);
    const findingId = normalizeString(event.params.findingId);
    const likerUid = normalizeString(event.params.likerUid);
    const beforeData = event.data?.before?.exists ? event.data.before.data() : null;
    const afterData = event.data?.after?.exists ? event.data.after.data() : null;
    const findingContext = await loadFindingContext(ownerUid, findingId);
    const notificationId = likeNotificationId(
      ownerUid,
      findingContext.stableFindingId,
      likerUid,
      likerUid
    );

    try {
      if (!ownerUid || !findingId || !likerUid) {
        logger.warn("NotificationFunction like skipped invalid params", {
          type: "like",
          recipientUid: ownerUid,
          actorUid: likerUid,
          notificationId,
          relatedFindingId: findingContext.stableFindingId,
        });
        return;
      }

      if (ownerUid === likerUid) {
        await deleteNotificationDocument(ownerUid, notificationId);
        logger.info("NotificationFunction like skipped self action", {
          type: "like",
          recipientUid: ownerUid,
          actorUid: likerUid,
          notificationId,
          relatedFindingId: findingContext.stableFindingId,
        });
        return;
      }

      if (!afterData) {
        await deleteNotificationDocument(ownerUid, notificationId);
        logger.info("NotificationFunction like removed", {
          type: "like",
          recipientUid: ownerUid,
          actorUid: likerUid,
          notificationId,
          relatedFindingId: findingContext.stableFindingId,
          relatedAnimalId: findingContext.relatedAnimalId,
        });
        return;
      }

      const actorDisplayName = normalizeString(afterData.likerDisplayName) ||
        await loadPublicDisplayName(likerUid);
      await upsertNotificationDocument(ownerUid, notificationId, {
        type: "like",
        title: "Neuer Like",
        message: `${actorDisplayName || "Jemand"} gefällt dein Fund.`,
        createdAt: afterData.createdAt || beforeData?.createdAt || FieldValue.serverTimestamp(),
        actorUid: likerUid,
        actorDisplayName,
        recipientUid: ownerUid,
        relatedOwnerUserId: ownerUid,
        relatedFindingId: findingContext.stableFindingId,
        relatedAnimalId: findingContext.relatedAnimalId,
        sourcePath: event.data?.after?.ref?.path || event.data?.before?.ref?.path || findingContext.sourcePath,
        legacyIds: [legacyLikeNotificationId(findingId, likerUid)],
        isRead: false,
      });
      logger.info("NotificationFunction like upserted", {
        type: "like",
        recipientUid: ownerUid,
        actorUid: likerUid,
        notificationId,
        relatedFindingId: findingContext.stableFindingId,
        relatedAnimalId: findingContext.relatedAnimalId,
      });
    } catch (error) {
      logger.error("NotificationFunction like failed", {
        type: "like",
        recipientUid: ownerUid,
        actorUid: likerUid,
        notificationId,
        relatedFindingId: findingContext.stableFindingId,
        relatedAnimalId: findingContext.relatedAnimalId,
        error: error instanceof Error ? error.message : String(error),
      });
      throw error;
    }
  }
);

exports.syncCommentNotification = onDocumentWritten(
  "users/{ownerUid}/findings/{findingId}/comments/{commentId}",
  async (event) => {
    const ownerUid = normalizeString(event.params.ownerUid);
    const findingId = normalizeString(event.params.findingId);
    const commentId = normalizeString(event.params.commentId);
    const beforeData = event.data?.before?.exists ? event.data.before.data() : null;
    const afterData = event.data?.after?.exists ? event.data.after.data() : null;
    const commentActorUid = normalizeString(afterData?.commenterUid || beforeData?.commenterUid);
    const findingContext = await loadFindingContext(ownerUid, findingId);
    const notificationId = commentNotificationId(
      ownerUid,
      findingContext.stableFindingId,
      commentId
    );

    try {
      if (!ownerUid || !findingId || !commentId || !commentActorUid) {
        logger.warn("NotificationFunction comment skipped invalid params", {
          type: "comment",
          recipientUid: ownerUid,
          actorUid: commentActorUid,
          notificationId,
          relatedFindingId: findingContext.stableFindingId,
        });
        return;
      }

      if (ownerUid === commentActorUid) {
        await deleteNotificationDocument(ownerUid, notificationId);
        logger.info("NotificationFunction comment skipped self action", {
          type: "comment",
          recipientUid: ownerUid,
          actorUid: commentActorUid,
          notificationId,
          relatedFindingId: findingContext.stableFindingId,
        });
        return;
      }

      if (!afterData) {
        await deleteNotificationDocument(ownerUid, notificationId);
        logger.info("NotificationFunction comment removed", {
          type: "comment",
          recipientUid: ownerUid,
          actorUid: commentActorUid,
          notificationId,
          relatedFindingId: findingContext.stableFindingId,
          relatedAnimalId: findingContext.relatedAnimalId,
        });
        return;
      }

      const actorDisplayName = normalizeString(afterData.commenterDisplayName) ||
        await loadPublicDisplayName(commentActorUid);
      const commentText = normalizeString(afterData.text);
      await upsertNotificationDocument(ownerUid, notificationId, {
        type: "comment",
        title: "Neuer Kommentar",
        message: `${actorDisplayName || "Jemand"}: ${commentText}`,
        createdAt: afterData.createdAt || beforeData?.createdAt || FieldValue.serverTimestamp(),
        actorUid: commentActorUid,
        actorDisplayName,
        recipientUid: ownerUid,
        relatedOwnerUserId: ownerUid,
        relatedFindingId: findingContext.stableFindingId,
        relatedAnimalId: findingContext.relatedAnimalId,
        sourcePath: event.data?.after?.ref?.path || event.data?.before?.ref?.path || findingContext.sourcePath,
        legacyIds: [legacyCommentNotificationId(findingId, commentId)],
        isRead: false,
      });
      logger.info("NotificationFunction comment upserted", {
        type: "comment",
        recipientUid: ownerUid,
        actorUid: commentActorUid,
        notificationId,
        relatedFindingId: findingContext.stableFindingId,
        relatedAnimalId: findingContext.relatedAnimalId,
      });
    } catch (error) {
      logger.error("NotificationFunction comment failed", {
        type: "comment",
        recipientUid: ownerUid,
        actorUid: commentActorUid,
        notificationId,
        relatedFindingId: findingContext.stableFindingId,
        relatedAnimalId: findingContext.relatedAnimalId,
        error: error instanceof Error ? error.message : String(error),
      });
      throw error;
    }
  }
);
