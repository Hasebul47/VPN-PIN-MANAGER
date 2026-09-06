package com.example.data

import com.example.data.dao.PaymentTransactionDao
import com.example.data.dao.VpnSubscriberDao
import com.example.data.entity.PaymentTransaction
import com.example.data.entity.VpnSubscriber
import com.example.data.sync.FirestoreSyncManager
import kotlinx.coroutines.flow.Flow

class VpnRepository(
    private val subscriberDao: VpnSubscriberDao,
    private val transactionDao: PaymentTransactionDao,
    private val syncManager: FirestoreSyncManager = FirestoreSyncManager()
) {
    @Volatile
    var currentUserId: String? = null

    val allSubscribers: Flow<List<VpnSubscriber>> = subscriberDao.getAllSubscribers()
    val allTransactions: Flow<List<PaymentTransaction>> = transactionDao.getAllTransactions()

    fun getTransactionsForSubscriber(subscriberId: Long): Flow<List<PaymentTransaction>> {
        return transactionDao.getTransactionsForSubscriber(subscriberId)
    }

    suspend fun syncFromCloud(userId: String): Result<Unit> {
        currentUserId = userId
        return syncManager.syncUserFromCloud(userId, subscriberDao, transactionDao)
    }

    suspend fun clearLocalData() {
        currentUserId = null
        syncManager.clearLocalData(subscriberDao, transactionDao)
    }

    suspend fun addSubscriber(
        subscriber: VpnSubscriber,
        initialPaymentMethod: String = "bKash",
        trxId: String = ""
    ): Long {
        val id = subscriberDao.insertSubscriber(subscriber)
        val savedSubscriber = subscriber.copy(id = id)

        currentUserId?.let { uid ->
            syncManager.saveSubscriber(uid, savedSubscriber)
        }

        if (subscriber.paidAmount > 0) {
            val initialTx = PaymentTransaction(
                subscriberId = id,
                customerName = subscriber.customerName,
                amount = subscriber.paidAmount,
                paymentDateEpochMs = System.currentTimeMillis(),
                paymentMethod = initialPaymentMethod,
                transactionNote = "সাবস্ক্রিপশন শুরু ও প্রাথমিক পেমেন্ট",
                trxId = trxId
            )
            val txId = transactionDao.insertTransaction(initialTx)
            currentUserId?.let { uid ->
                syncManager.saveTransaction(uid, initialTx.copy(id = txId))
            }
        }
        return id
    }

    suspend fun updateSubscriber(subscriber: VpnSubscriber) {
        subscriberDao.updateSubscriber(subscriber)
        currentUserId?.let { uid ->
            syncManager.saveSubscriber(uid, subscriber)
        }
    }

    suspend fun deleteSubscriber(subscriber: VpnSubscriber) {
        transactionDao.deleteBySubscriberId(subscriber.id)
        subscriberDao.deleteSubscriber(subscriber)
        currentUserId?.let { uid ->
            syncManager.deleteSubscriber(uid, subscriber.id)
        }
    }

    suspend fun recordPayment(
        subscriber: VpnSubscriber,
        paymentAmount: Double,
        paymentMethod: String,
        trxId: String,
        note: String
    ) {
        val updatedSub = subscriber.copy(paidAmount = subscriber.paidAmount + paymentAmount)
        subscriberDao.updateSubscriber(updatedSub)
        currentUserId?.let { uid ->
            syncManager.saveSubscriber(uid, updatedSub)
        }

        val tx = PaymentTransaction(
            subscriberId = subscriber.id,
            customerName = subscriber.customerName,
            amount = paymentAmount,
            paymentDateEpochMs = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            transactionNote = if (note.isBlank()) "বকেয়া বিল পরিশোধ" else note,
            trxId = trxId
        )
        val txId = transactionDao.insertTransaction(tx)
        currentUserId?.let { uid ->
            syncManager.saveTransaction(uid, tx.copy(id = txId))
        }
    }

    suspend fun renewSubscription(
        subscriber: VpnSubscriber,
        additionalDays: Int = 30,
        renewalFee: Double,
        paidNow: Double,
        paymentMethod: String = "bKash",
        trxId: String = ""
    ) {
        val baseTime = if (subscriber.expiryDateEpochMs > System.currentTimeMillis()) {
            subscriber.expiryDateEpochMs
        } else {
            System.currentTimeMillis()
        }
        val newExpiry = baseTime + (additionalDays.toLong() * 24 * 60 * 60 * 1000L)
        val updatedSub = subscriber.copy(
            startDateEpochMs = System.currentTimeMillis(),
            expiryDateEpochMs = newExpiry,
            monthlyFee = renewalFee,
            paidAmount = paidNow
        )

        subscriberDao.updateSubscriber(updatedSub)
        currentUserId?.let { uid ->
            syncManager.saveSubscriber(uid, updatedSub)
        }

        if (paidNow > 0) {
            val tx = PaymentTransaction(
                subscriberId = subscriber.id,
                customerName = subscriber.customerName,
                amount = paidNow,
                paymentDateEpochMs = System.currentTimeMillis(),
                paymentMethod = paymentMethod,
                transactionNote = "মাসিক সাবস্ক্রিপশন রিনিউয়াল ($additionalDays দিন)",
                trxId = trxId
            )
            val txId = transactionDao.insertTransaction(tx)
            currentUserId?.let { uid ->
                syncManager.saveTransaction(uid, tx.copy(id = txId))
            }
        }
    }
}
