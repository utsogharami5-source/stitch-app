package com.smartbudge.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.smartbudge.app.data.local.dao.TransactionDao
import com.smartbudge.app.data.local.dao.UserDao
import com.smartbudge.app.data.local.entity.TransactionEntity
import com.smartbudge.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao
) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    suspend fun uploadData(): Result<Unit> {
        return try {
            val user = userDao.getUser(currentUserId).first()
            val transactions = transactionDao.getAllTransactions(currentUserId).first()

            val backupData = hashMapOf(
                "user" to user,
                "transactions" to transactions,
                "last_backup" to System.currentTimeMillis()
            )

            db.collection("backups").document(currentUserId).set(backupData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadData(): Result<Unit> {
        return try {
            val snapshot = db.collection("backups").document(currentUserId).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data ?: return Result.failure(Exception("No data found"))
                
                // Restore User Profile
                val userData = data["user"] as? Map<String, Any>
                if (userData != null) {
                    val user = UserEntity(
                        user_id = userData["user_id"] as? String ?: currentUserId,
                        name = userData["name"] as? String ?: "User",
                        email = userData["email"] as? String ?: "",
                        monthly_budget = (userData["monthly_budget"] as? Number)?.toDouble() ?: 0.0,
                        created_at = (userData["created_at"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                    userDao.insertUser(user)
                }

                // Restore Transactions
                val transactionList = data["transactions"] as? List<Map<String, Any>>
                if (transactionList != null) {
                    val transactions = transactionList.map { t ->
                        TransactionEntity(
                            transaction_id = (t["transaction_id"] as? Number)?.toInt() ?: 0,
                            user_id = t["user_id"] as? String ?: currentUserId,
                            type = t["type"] as? String ?: "Expense",
                            amount = (t["amount"] as? Number)?.toDouble() ?: 0.0,
                            category_id = (t["category_id"] as? Number)?.toInt() ?: 0,
                            date = (t["date"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            note = t["note"] as? String ?: "",
                            receipt_url = t["receipt_url"] as? String
                        )
                    }
                    // In a more complex app, you might want to clear and re-insert or upsert.
                    // For now, we'll upsert via the Dao's insert method (assumed to handle conflict).
                    transactions.forEach { transactionDao.insertTransaction(it) }
                }
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Backup not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
