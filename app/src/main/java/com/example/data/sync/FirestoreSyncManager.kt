package com.example.data.sync

import com.example.data.dao.PaymentTransactionDao
import com.example.data.dao.VpnSubscriberDao
import com.example.data.entity.PaymentTransaction
import com.example.data.entity.VpnSubscriber
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncManager {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    val isFirestoreAvailable: Boolean
        get() = firestore != null

    /**
     * Pulls the user's complete dataset from Firestore and populates the local Room cache.
     */
    suspend fun syncUserFromCloud(
        userId: String,
        subscriberDao: VpnSubscriberDao,
        transactionDao: PaymentTransactionDao
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(
            IllegalStateException("Firestore is not initialized")
        )

        try {
            // 1. Fetch Subscribers
            val subSnapshot = db.collection("users")
                .document(userId)
                .collection("subscribers")
                .get()
                .await()

            val cloudSubscribers = subSnapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                    VpnSubscriber(
                        id = id,
                        customerName = doc.getString("customerName") ?: "",
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        vpnPin = doc.getString("vpnPin") ?: "",
                        serviceName = doc.getString("serviceName") ?: "VPN Pro",
                        startDateEpochMs = doc.getLong("startDateEpochMs") ?: System.currentTimeMillis(),
                        expiryDateEpochMs = doc.getLong("expiryDateEpochMs") ?: (System.currentTimeMillis() + 30L * 86400000L),
                        monthlyFee = doc.getDouble("monthlyFee") ?: 0.0,
                        paidAmount = doc.getDouble("paidAmount") ?: 0.0,
                        notes = doc.getString("notes") ?: "",
                        createdAtEpochMs = doc.getLong("createdAtEpochMs") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }

            // 2. Fetch Transactions
            val txSnapshot = db.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .await()

            val cloudTransactions = txSnapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                    PaymentTransaction(
                        id = id,
                        subscriberId = doc.getLong("subscriberId") ?: 0L,
                        customerName = doc.getString("customerName") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        paymentDateEpochMs = doc.getLong("paymentDateEpochMs") ?: System.currentTimeMillis(),
                        paymentMethod = doc.getString("paymentMethod") ?: "bKash",
                        transactionNote = doc.getString("transactionNote") ?: "",
                        trxId = doc.getString("trxId") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }

            // 3. Update local Room database
            subscriberDao.deleteAll()
            transactionDao.deleteAll()

            if (cloudSubscribers.isNotEmpty()) {
                subscriberDao.insertAllSubscribers(cloudSubscribers)
            }
            if (cloudTransactions.isNotEmpty()) {
                transactionDao.insertAllTransactions(cloudTransactions)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Saves or updates a subscriber in the user's Firestore collection.
     */
    suspend fun saveSubscriber(userId: String, subscriber: VpnSubscriber) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val data = hashMapOf(
                "id" to subscriber.id,
                "customerName" to subscriber.customerName,
                "phoneNumber" to subscriber.phoneNumber,
                "vpnPin" to subscriber.vpnPin,
                "serviceName" to subscriber.serviceName,
                "startDateEpochMs" to subscriber.startDateEpochMs,
                "expiryDateEpochMs" to subscriber.expiryDateEpochMs,
                "monthlyFee" to subscriber.monthlyFee,
                "paidAmount" to subscriber.paidAmount,
                "notes" to subscriber.notes,
                "createdAtEpochMs" to subscriber.createdAtEpochMs,
                "updatedAtEpochMs" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(userId)
                .collection("subscribers")
                .document(subscriber.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes a subscriber and related transactions from Firestore.
     */
    suspend fun deleteSubscriber(userId: String, subscriberId: Long) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            // Delete subscriber doc
            db.collection("users")
                .document(userId)
                .collection("subscribers")
                .document(subscriberId.toString())
                .delete()
                .await()

            // Delete associated transactions
            val txDocs = db.collection("users")
                .document(userId)
                .collection("transactions")
                .whereEqualTo("subscriberId", subscriberId)
                .get()
                .await()

            for (doc in txDocs.documents) {
                doc.reference.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Saves a payment transaction to Firestore.
     */
    suspend fun saveTransaction(userId: String, transaction: PaymentTransaction) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val data = hashMapOf(
                "id" to transaction.id,
                "subscriberId" to transaction.subscriberId,
                "customerName" to transaction.customerName,
                "amount" to transaction.amount,
                "paymentDateEpochMs" to transaction.paymentDateEpochMs,
                "paymentMethod" to transaction.paymentMethod,
                "transactionNote" to transaction.transactionNote,
                "trxId" to transaction.trxId
            )

            db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transaction.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clears local cache on sign-out to guarantee user data privacy.
     */
    suspend fun clearLocalData(
        subscriberDao: VpnSubscriberDao,
        transactionDao: PaymentTransactionDao
    ) = withContext(Dispatchers.IO) {
        subscriberDao.deleteAll()
        transactionDao.deleteAll()
    }
}
