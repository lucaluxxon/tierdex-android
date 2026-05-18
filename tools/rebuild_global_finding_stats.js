#!/usr/bin/env node
/**
 * Rebuild global finding stats from existing cloud findings.
 *
 * Usage:
 *   1. Preferred: use Application Default Credentials
 *      - e.g. `gcloud auth application-default login`
 *      - then run:
 *        `node .\tools\rebuild_global_finding_stats.js`
 *
 *   2. Optional: use a local service account JSON that is NOT committed
 *      - place it outside the repo if possible, or as a local-only file such as:
 *        `C:\Users\...\Tierdex APP\05 App\serviceAccountKey.json`
 *      - set one of:
 *        `GOOGLE_APPLICATION_CREDENTIALS=<absolute-path>`
 *        or
 *        `SERVICE_ACCOUNT_KEY_PATH=<absolute-path>`
 *      - then run:
 *        `node .\tools\rebuild_global_finding_stats.js`
 *
 * Important:
 * - This script only counts cloud findings that already exist under `users/{uid}/findings`.
 * - Local findings that were never synchronized to Firestore are NOT included.
 * - This script does NOT delete findings, photos, likes, comments, or contributions.
 * - It overwrites `globalFindingContributions/{uid}_{findingId}` and
 *   `animalStats/{animalId}.globalFindingCount` with deterministically rebuilt values.
 */

const fs = require("fs");
const path = require("path");

function requireFirebaseAdmin() {
  try {
    return require("firebase-admin");
  } catch (_error) {
    return require(path.join(__dirname, "..", "functions", "node_modules", "firebase-admin"));
  }
}

const admin = requireFirebaseAdmin();

function normalizeString(value) {
  return typeof value === "string" ? value.trim() : "";
}

function resolveCredential() {
  const explicitServiceAccountPath = normalizeString(process.env.SERVICE_ACCOUNT_KEY_PATH);
  if (explicitServiceAccountPath) {
    const resolvedPath = path.resolve(explicitServiceAccountPath);
    if (!fs.existsSync(resolvedPath)) {
      throw new Error(`SERVICE_ACCOUNT_KEY_PATH not found: ${resolvedPath}`);
    }
    const serviceAccount = JSON.parse(fs.readFileSync(resolvedPath, "utf8"));
    return admin.credential.cert(serviceAccount);
  }

  return admin.credential.applicationDefault();
}

async function main() {
  admin.initializeApp({
    credential: resolveCredential(),
  });

  const db = admin.firestore();
  const serverTimestamp = admin.firestore.FieldValue.serverTimestamp();

  const usersSnapshot = await db.collection("users").get();
  const userIds = usersSnapshot.docs.map((doc) => normalizeString(doc.id)).filter(Boolean);

  let findingCount = 0;
  const contributionEntries = [];
  const animalCountByAnimalId = new Map();

  for (const userId of userIds) {
    const findingsSnapshot = await db.collection("users").doc(userId).collection("findings").get();

    findingsSnapshot.docs.forEach((findingDoc) => {
      const findingId = normalizeString(findingDoc.id);
      const animalId = normalizeString(findingDoc.get("animalId"));

      if (!findingId || !animalId) {
        console.warn(
          `[GlobalStatsRebuild] skipped invalid finding userId=${userId} findingId=${findingDoc.id} animalId=${String(findingDoc.get("animalId") ?? "")}`
        );
        return;
      }

      findingCount += 1;
      const contributionId = `${userId}_${findingId}`;
      contributionEntries.push({
        contributionId,
        ownerUid: userId,
        findingId,
        animalId,
      });
      animalCountByAnimalId.set(animalId, (animalCountByAnimalId.get(animalId) || 0) + 1);
    });
  }

  const animalStatsSnapshot = await db.collection("animalStats").get();
  const previousAnimalStatsByAnimalId = new Map(
    animalStatsSnapshot.docs
      .map((doc) => [normalizeString(doc.get("animalId")), Number(doc.get("globalFindingCount") || 0)])
      .filter(([animalId]) => Boolean(animalId))
  );

  const staleAnimalIdsWithoutContributions = [...previousAnimalStatsByAnimalId.keys()]
    .filter((animalId) => !animalCountByAnimalId.has(animalId))
    .sort();

  const contributionWriter = db.bulkWriter();
  contributionEntries.forEach((entry) => {
    contributionWriter.set(
      db.collection("globalFindingContributions").doc(entry.contributionId),
      {
        ownerUid: entry.ownerUid,
        findingId: entry.findingId,
        animalId: entry.animalId,
        updatedAt: serverTimestamp,
      }
    );
  });
  await contributionWriter.close();

  const sortedAnimalEntries = [...animalCountByAnimalId.entries()].sort((left, right) => {
    return left[0].localeCompare(right[0]);
  });

  const statsWriter = db.bulkWriter();
  sortedAnimalEntries.forEach(([animalId, globalFindingCount]) => {
    statsWriter.set(
      db.collection("animalStats").doc(animalId),
      {
        animalId,
        globalFindingCount,
        updatedAt: serverTimestamp,
      }
    );
  });
  await statsWriter.close();

  console.log("");
  console.log("=== Global Finding Stats Rebuild Summary ===");
  console.log(`Users read: ${userIds.length}`);
  console.log(`Cloud findings read: ${findingCount}`);
  console.log(`Contributions written: ${contributionEntries.length}`);
  console.log(`Affected animalIds: ${sortedAnimalEntries.length}`);
  console.log("");
  console.log("Counts by animalId:");
  sortedAnimalEntries.forEach(([animalId, count]) => {
    const previousCount = previousAnimalStatsByAnimalId.has(animalId)
      ? previousAnimalStatsByAnimalId.get(animalId)
      : "unknown";
    console.log(`- ${animalId}: ${count} (previous animalStats=${previousCount})`);
  });

  if (staleAnimalIdsWithoutContributions.length > 0) {
    console.log("");
    console.log("animalStats without contributions (logged only, not deleted or zeroed):");
    staleAnimalIdsWithoutContributions.forEach((animalId) => {
      console.log(`- ${animalId} (previous animalStats=${previousAnimalStatsByAnimalId.get(animalId)})`);
    });
  }

  console.log("");
  console.log("Done. No findings, photos, likes, comments, or contributions were deleted.");
}

main().catch((error) => {
  console.error("[GlobalStatsRebuild] failed:", error);
  process.exitCode = 1;
});
