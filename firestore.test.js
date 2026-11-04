import { test, before, after, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { readFileSync } from 'node:fs';

const PROJECT_ID = 'demo-no-project';
const DATABASE_ID = 'ai-studio-android-7b40ceb6-b39e-4f53-9a8d-be4e7b04572e';

let testEnv;

before(async () => {
  const rules = readFileSync('firestore.rules', 'utf8');
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: '127.0.0.1',
      port: 8085,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test('Unauthenticated user cannot read or write user profiles', async () => {
  const unauthed = testEnv.unauthenticatedContext().firestore({ databaseId: DATABASE_ID });
  const profileRef = unauthed.doc('users/alice');

  await assertFails(profileRef.get());
  await assertFails(profileRef.set({
    userId: 'alice',
    displayName: 'Alice',
    highScore: 100,
    createdAt: new Date(),
  }));
});

test('Authenticated user can create and read their own user profile', async () => {
  const alice = testEnv.authenticatedContext('alice').firestore({ databaseId: DATABASE_ID });
  const profileRef = alice.doc('users/alice');

  await assertSucceeds(profileRef.set({
    userId: 'alice',
    displayName: 'Alice',
    highScore: 100,
    maxLength: 15,
    totalKills: 2,
    totalGames: 1,
    favoriteSkin: 'Neon Cyan',
    createdAt: new Date(),
  }));

  await assertSucceeds(profileRef.get());
});

test('Authenticated user cannot read or write another users profile', async () => {
  const alice = testEnv.authenticatedContext('alice').firestore({ databaseId: DATABASE_ID });
  const bobProfileRef = alice.doc('users/bob');

  await assertFails(bobProfileRef.get());
  await assertFails(bobProfileRef.set({
    userId: 'bob',
    displayName: 'Bob',
    highScore: 50,
    createdAt: new Date(),
  }));
});

test('Authenticated user can add match records to their own collection', async () => {
  const alice = testEnv.authenticatedContext('alice').firestore({ databaseId: DATABASE_ID });
  const recordRef = alice.doc('users/alice/records/rec_001');

  await assertSucceeds(recordRef.set({
    recordId: 'rec_001',
    userId: 'alice',
    score: 1250,
    maxLength: 24,
    kills: 4,
    survivalSeconds: 95,
    skinName: 'Aqua Viper',
    createdAt: new Date(),
  }));

  // Match records are immutable
  await assertFails(recordRef.update({
    score: 9999,
  }));
});

test('Authenticated users can read leaderboard entries and create their own entry', async () => {
  const alice = testEnv.authenticatedContext('alice').firestore({ databaseId: DATABASE_ID });
  const bob = testEnv.authenticatedContext('bob').firestore({ databaseId: DATABASE_ID });

  const aliceScoreRef = alice.doc('leaderboard/alice');
  await assertSucceeds(aliceScoreRef.set({
    scoreId: 'alice',
    userId: 'alice',
    displayName: 'Alice Glow',
    score: 3000,
    maxLength: 35,
    kills: 6,
    skinName: 'Neon Cyan',
    createdAt: new Date(),
  }));

  // Bob can read Alice's score
  const bobViewingAlice = bob.doc('leaderboard/alice');
  await assertSucceeds(bobViewingAlice.get());

  // Bob cannot modify Alice's score
  await assertFails(bobViewingAlice.update({ score: 10 }));
});
