const { readFileSync } = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");
const { after, before, beforeEach, test } = require("node:test");
const { initializeTestEnvironment } = require("@firebase/rules-unit-testing");
const { Timestamp, doc, getDoc, setDoc } = require("firebase/firestore");

const PROJECT_ID = "tierdex-rules-test";
const OWNER_UID = "owner-user";
const OTHER_UID = "other-user";
const RULES_PATH = path.resolve(__dirname, "../../firestore.rules");
const RULES = readFileSync(RULES_PATH, "utf8");

let testEnv;

function xpStatePath(uid = OWNER_UID) {
  return `users/${uid}/private/meta/xpState/state`;
}

function questStatePath(uid = OWNER_UID) {
  return `users/${uid}/private/meta/questState/state`;
}

function xpState(overrides = {}) {
  return {
    totalXp: 5,
    awardedXpKeys: ["seed-award"],
    backfillV1Done: false,
    schemaVersion: 1,
    updatedAt: Timestamp.fromDate(new Date("2026-08-05T00:00:00Z")),
    ...overrides
  };
}

function questState(overrides = {}) {
  return {
    socialLikesGivenCount: 1,
    socialCommentsWrittenCount: 2,
    socialLikeQuestFindingKeys: ["finding-1"],
    socialCommentQuestFindingKeys: ["finding-1"],
    dailyAnimalQuestHitFindingIds: ["finding-2"],
    schemaVersion: 1,
    updatedAt: Timestamp.fromDate(new Date("2026-08-05T00:00:00Z")),
    ...overrides
  };
}

function ownerDb() {
  return testEnv.authenticatedContext(OWNER_UID).firestore();
}

function otherDb() {
  return testEnv.authenticatedContext(OTHER_UID).firestore();
}

function anonDb() {
  return testEnv.unauthenticatedContext().firestore();
}

function encodeBase64Url(value) {
  return Buffer.from(value)
    .toString("base64")
    .replace(/=/g, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");
}

function unsignedJwt(uid) {
  const header = encodeBase64Url(JSON.stringify({ alg: "none", typ: "JWT" }));
  const payload = encodeBase64Url(JSON.stringify({ user_id: uid, sub: uid }));
  return `${header}.${payload}.`;
}

function fireValue(value) {
  if (
    value &&
    typeof value === "object" &&
    !Array.isArray(value) &&
    Object.keys(value).some((key) => key.endsWith("Value"))
  ) {
    return value;
  }
  if (typeof value === "string") {
    return { stringValue: value };
  }
  if (typeof value === "number") {
    return { integerValue: String(value) };
  }
  if (typeof value === "boolean") {
    return { booleanValue: value };
  }
  if (value instanceof Date) {
    return { timestampValue: value.toISOString() };
  }
  if (Array.isArray(value)) {
    return { arrayValue: { values: value.map(fireValue) } };
  }
  if (value && typeof value === "object") {
    return {
      mapValue: {
        fields: Object.fromEntries(Object.entries(value).map(([key, innerValue]) => [key, fireValue(innerValue)]))
      }
    };
  }
  throw new Error(`Unsupported Firestore value: ${value}`);
}

function xpStateFields(overrides = {}) {
  const data = {
    totalXp: 5,
    awardedXpKeys: ["seed-award"],
    backfillV1Done: false,
    schemaVersion: 1,
    updatedAt: new Date("2026-08-05T00:00:00Z"),
    ...overrides
  };
  return Object.fromEntries(Object.entries(data).map(([key, value]) => [key, fireValue(value)]));
}

function questStateFields(overrides = {}) {
  const data = {
    socialLikesGivenCount: 1,
    socialCommentsWrittenCount: 2,
    socialLikeQuestFindingKeys: ["finding-1"],
    socialCommentQuestFindingKeys: ["finding-1"],
    dailyAnimalQuestHitFindingIds: ["finding-2"],
    schemaVersion: 1,
    updatedAt: new Date("2026-08-05T00:00:00Z"),
    ...overrides
  };
  return Object.fromEntries(Object.entries(data).map(([key, value]) => [key, fireValue(value)]));
}

async function emulatorDocumentRequest({ method = "GET", path: documentPath, authUid, fields }) {
  const headers = {};
  if (authUid) {
    headers.Authorization = `Bearer ${unsignedJwt(authUid)}`;
  }
  if (fields) {
    headers["Content-Type"] = "application/json";
  }
  const response = await fetch(
    `http://127.0.0.1:8787/v1/projects/${PROJECT_ID}/databases/(default)/documents/${documentPath}`,
    {
      method,
      headers,
      body: fields ? JSON.stringify({ fields }) : undefined
    }
  );
  return {
    status: response.status,
    body: await response.text()
  };
}

function assertStatus(expectedStatus, response) {
  assert.equal(response.status, expectedStatus, `Expected HTTP ${expectedStatus}, got ${response.status}: ${response.body}`);
}

async function seedOwnerDocuments() {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, xpStatePath()), xpState());
    await setDoc(doc(db, questStatePath()), questState());
  });
}

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: "127.0.0.1",
      port: 8787,
      rules: RULES
    }
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  await testEnv.cleanup();
});

test("Read access: owner can read own XP state", async () => {
  await seedOwnerDocuments();
  assertStatus(200, await emulatorDocumentRequest({ path: xpStatePath(), authUid: OWNER_UID }));
});

test("Read access: owner can read own quest state", async () => {
  await seedOwnerDocuments();
  assertStatus(200, await emulatorDocumentRequest({ path: questStatePath(), authUid: OWNER_UID }));
});

test("Read access: other authenticated user cannot read owner's XP state", async () => {
  await seedOwnerDocuments();
  assertStatus(403, await emulatorDocumentRequest({ path: xpStatePath(), authUid: OTHER_UID }));
});

test("Read access: other authenticated user cannot read owner's quest state", async () => {
  await seedOwnerDocuments();
  assertStatus(403, await emulatorDocumentRequest({ path: questStatePath(), authUid: OTHER_UID }));
});

test("Read access: unauthenticated user cannot read XP or quest state", async () => {
  await seedOwnerDocuments();
  assertStatus(403, await emulatorDocumentRequest({ path: xpStatePath() }));
  assertStatus(403, await emulatorDocumentRequest({ path: questStatePath() }));
});

test("Anonymous writes: unauthenticated user cannot create or replace XP state", async () => {
  assertStatus(403, await emulatorDocumentRequest({ method: "PATCH", path: xpStatePath(), fields: xpStateFields() }));
});

test("Anonymous writes: unauthenticated user cannot create or replace quest state", async () => {
  assertStatus(403, await emulatorDocumentRequest({ method: "PATCH", path: questStatePath(), fields: questStateFields() }));
});

test("BASELINE VULNERABILITY: owner can freely overwrite totalXp", async () => {
  await seedOwnerDocuments();
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: xpStatePath(),
      authUid: OWNER_UID,
      fields: xpStateFields({ totalXp: 9999999 })
    })
  );
});

test("BASELINE VULNERABILITY: owner can freely replace awardedXpKeys", async () => {
  await seedOwnerDocuments();
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: xpStatePath(),
      authUid: OWNER_UID,
      fields: xpStateFields({ awardedXpKeys: ["manual-award-1", "manual-award-2"] })
    })
  );
});

test("BASELINE VULNERABILITY: owner can freely overwrite quest counters", async () => {
  await seedOwnerDocuments();
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: questStatePath(),
      authUid: OWNER_UID,
      fields: questStateFields({
        socialLikesGivenCount: 999,
        socialCommentsWrittenCount: 777
      })
    })
  );
});

test("BASELINE VULNERABILITY: owner can freely overwrite quest lists", async () => {
  await seedOwnerDocuments();
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: questStatePath(),
      authUid: OWNER_UID,
      fields: questStateFields({
        socialLikeQuestFindingKeys: ["finding-a", "finding-b"],
        socialCommentQuestFindingKeys: ["finding-c"]
      })
    })
  );
});

test("BASELINE VULNERABILITY: owner can freely rewrite quest progress lists that functionally model completions", async () => {
  await seedOwnerDocuments();
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: questStatePath(),
      authUid: OWNER_UID,
      fields: questStateFields({
        dailyAnimalQuestHitFindingIds: ["finding-10", "finding-11", "finding-12"]
      })
    })
  );
});

test("BASELINE VULNERABILITY: rules currently accept totalXp with the wrong type", async () => {
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: xpStatePath(),
      authUid: OWNER_UID,
      fields: xpStateFields({ totalXp: { stringValue: "5" } })
    })
  );
});

test("BASELINE VULNERABILITY: rules currently accept out-of-range totalXp values", async () => {
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: xpStatePath(),
      authUid: OWNER_UID,
      fields: xpStateFields({ totalXp: -1 })
    })
  );
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: xpStatePath(),
      authUid: OWNER_UID,
      fields: xpStateFields({ totalXp: 10000001 })
    })
  );
});

test("BASELINE VULNERABILITY: rules currently accept unknown XP fields", async () => {
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: xpStatePath(),
      authUid: OWNER_UID,
      fields: {
        ...xpStateFields(),
        debugOnly: { booleanValue: true }
      }
    })
  );
});

test("BASELINE VULNERABILITY: rules currently accept invalid quest payloads", async () => {
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: questStatePath(),
      authUid: OWNER_UID,
      fields: questStateFields({
        socialLikesGivenCount: -1
      })
    })
  );
  assertStatus(
    200,
    await emulatorDocumentRequest({
      method: "PATCH",
      path: questStatePath(),
      authUid: OWNER_UID,
      fields: {
        ...questStateFields(),
        extraQuestField: { stringValue: "not-allowed" }
      }
    })
  );
});
