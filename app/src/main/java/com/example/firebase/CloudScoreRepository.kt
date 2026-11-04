package com.example.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class CloudUserProfile(
    val userId: String = "",
    val displayName: String = "",
    val highScore: Int = 0,
    val maxLength: Int = 0,
    val totalKills: Int = 0,
    val totalGames: Int = 0,
    val favoriteSkin: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class CloudLeaderboardEntry(
    val scoreId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val score: Int = 0,
    val maxLength: Int = 0,
    val kills: Int = 0,
    val skinName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

class CloudScoreRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        val databaseId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(databaseId)
    }

    suspend fun syncMatchResult(
        userId: String,
        displayName: String,
        score: Int,
        maxLength: Int,
        kills: Int,
        survivalSeconds: Int,
        skinName: String
    ): Boolean {
        return try {
            val userRef = db.collection("users").document(userId)
            val recordId = "rec_${System.currentTimeMillis()}"
            val recordRef = userRef.collection("records").document(recordId)
            val leaderboardRef = db.collection("leaderboard").document(userId)

            val now = Timestamp.now()

            // 1. Save individual match record
            val recordData = hashMapOf<String, Any>(
                "recordId" to recordId,
                "userId" to userId,
                "score" to score,
                "maxLength" to maxLength,
                "kills" to kills,
                "survivalSeconds" to survivalSeconds,
                "skinName" to skinName,
                "createdAt" to now
            )
            recordRef.set(recordData).await()

            // 2. Fetch or update user profile
            val userSnap = userRef.get().await()
            if (!userSnap.exists()) {
                val newProfile = hashMapOf<String, Any>(
                    "userId" to userId,
                    "displayName" to displayName,
                    "highScore" to score,
                    "maxLength" to maxLength,
                    "totalKills" to kills,
                    "totalGames" to 1,
                    "favoriteSkin" to skinName,
                    "createdAt" to now,
                    "updatedAt" to now
                )
                userRef.set(newProfile).await()
            } else {
                val currentHighScore = (userSnap.getLong("highScore") ?: 0L).toInt()
                val currentMaxLength = (userSnap.getLong("maxLength") ?: 0L).toInt()
                val currentTotalKills = (userSnap.getLong("totalKills") ?: 0L).toInt()
                val currentTotalGames = (userSnap.getLong("totalGames") ?: 0L).toInt()

                val newHigh = maxOf(currentHighScore, score)
                val newMaxLen = maxOf(currentMaxLength, maxLength)

                val updates = hashMapOf<String, Any>(
                    "displayName" to displayName,
                    "highScore" to newHigh,
                    "maxLength" to newMaxLen,
                    "totalKills" to (currentTotalKills + kills),
                    "totalGames" to (currentTotalGames + 1),
                    "favoriteSkin" to skinName,
                    "updatedAt" to now
                )
                userRef.update(updates).await()
            }

            // 3. Update public leaderboard entry if score is personal best
            val boardSnap = leaderboardRef.get().await()
            if (!boardSnap.exists()) {
                val newBoardEntry = hashMapOf<String, Any>(
                    "scoreId" to userId,
                    "userId" to userId,
                    "displayName" to displayName,
                    "score" to score,
                    "maxLength" to maxLength,
                    "kills" to kills,
                    "skinName" to skinName,
                    "createdAt" to now,
                    "updatedAt" to now
                )
                leaderboardRef.set(newBoardEntry).await()
            } else {
                val currentBest = (boardSnap.getLong("score") ?: 0L).toInt()
                if (score >= currentBest) {
                    val boardUpdates = hashMapOf<String, Any>(
                        "displayName" to displayName,
                        "score" to score,
                        "maxLength" to maxLength,
                        "kills" to kills,
                        "skinName" to skinName,
                        "updatedAt" to now
                    )
                    leaderboardRef.update(boardUpdates).await()
                }
            }
            true
        } catch (e: Exception) {
            Log.e("CloudScoreRepo", "Failed to sync match result: ${e.message}", e)
            false
        }
    }

    fun observeGlobalLeaderboard(): Flow<List<CloudLeaderboardEntry>> = callbackFlow {
        val query = db.collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(20)

        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("CloudScoreRepo", "Leaderboard listen failed", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val entries = snapshot.documents.mapNotNull { doc ->
                    try {
                        val scoreId = doc.getString("scoreId") ?: doc.id
                        val userId = doc.getString("userId") ?: ""
                        val name = doc.getString("displayName") ?: "حبار النيون"
                        val score = doc.getLong("score")?.toInt() ?: 0
                        val maxLength = doc.getLong("maxLength")?.toInt() ?: 0
                        val kills = doc.getLong("kills")?.toInt() ?: 0
                        val skin = doc.getString("skinName") ?: ""
                        val created = doc.getTimestamp("createdAt") ?: Timestamp.now()
                        val updated = doc.getTimestamp("updatedAt") ?: Timestamp.now()

                        CloudLeaderboardEntry(
                            scoreId = scoreId,
                            userId = userId,
                            displayName = name,
                            score = score,
                            maxLength = maxLength,
                            kills = kills,
                            skinName = skin,
                            createdAt = created,
                            updatedAt = updated
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(entries)
            }
        }
        awaitClose { registration.remove() }
    }
}
