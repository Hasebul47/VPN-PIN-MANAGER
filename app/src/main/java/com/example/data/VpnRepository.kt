package com.example.data

import com.example.data.dao.PaymentTransactionDao
import com.example.data.dao.VpnSubscriberDao
import com.example.data.entity.PaymentTransaction
import com.example.data.entity.VpnSubscriber
import kotlinx.coroutines.flow.Flow

class VpnRepository(
    private val subscriberDao: VpnSubscriberDao,
    private val transactionDao: PaymentTransactionDao
) {
    val allSubscribers: Flow<List<VpnSubscriber>> = subscriberDao.getAllSubscribers()
    val allTransactions: Flow<List<PaymentTransaction>> = transactionDao.getAllTransactions()

    fun getTransactionsForSubscriber(subscriberId: Long): Flow<List<PaymentTransaction>> {
        return transactionDao.getTransactionsForSubscriber(subscriberId)
    }

    suspend fun addSubscriber(
        subscriber: VpnSubscriber,
        initialPaymentMethod: String = "bKash",
        trxId: String = ""
    ): Long {
        val id = subscriberDao.insertSubscriber(subscriber)
        if (subscriber.paidAmount > 0) {
            transactionDao.insertTransaction(
                PaymentTransaction(
                    subscriberId = id,
                    customerName = subscriber.customerName,
                    amount = subscriber.paidAmount,
                    paymentDateEpochMs = System.currentTimeMillis(),
                    paymentMethod = initialPaymentMethod,
                    transactionNote = "সাবস্ক্রিপশন শুরু ও প্রাথমিক পেমেন্ট",
                    trxId = trxId
                )
            )
        }
        return id
    }

    suspend fun updateSubscriber(subscriber: VpnSubscriber) {
        subscriberDao.updateSubscriber(subscriber)
    }

    suspend fun deleteSubscriber(subscriber: VpnSubscriber) {
        transactionDao.deleteBySubscriberId(subscriber.id)
        subscriberDao.deleteSubscriber(subscriber)
    }

    suspend fun recordPayment(
        subscriber: VpnSubscriber,
        paymentAmount: Double,
        paymentMethod: String,
        trxId: String,
        note: String
    ) {
        val updatedPaid = subscriber.paidAmount + paymentAmount
        subscriberDao.updateSubscriber(subscriber.copy(paidAmount = updatedPaid))
        transactionDao.insertTransaction(
            PaymentTransaction(
                subscriberId = subscriber.id,
                customerName = subscriber.customerName,
                amount = paymentAmount,
                paymentDateEpochMs = System.currentTimeMillis(),
                paymentMethod = paymentMethod,
                transactionNote = if (note.isBlank()) "বকেয়া বিল পরিশোধ" else note,
                trxId = trxId
            )
        )
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
        // Reset monthly fee and paid amount for new period, carrying over any previous remaining balance or due
        val newMonthlyFee = renewalFee
        val newPaidAmount = paidNow

        subscriberDao.updateSubscriber(
            subscriber.copy(
                startDateEpochMs = System.currentTimeMillis(),
                expiryDateEpochMs = newExpiry,
                monthlyFee = newMonthlyFee,
                paidAmount = newPaidAmount
            )
        )

        if (paidNow > 0) {
            transactionDao.insertTransaction(
                PaymentTransaction(
                    subscriberId = subscriber.id,
                    customerName = subscriber.customerName,
                    amount = paidNow,
                    paymentDateEpochMs = System.currentTimeMillis(),
                    paymentMethod = paymentMethod,
                    transactionNote = "মাসিক সাবস্ক্রিপশন রিনিউয়াল ($additionalDays দিন)",
                    trxId = trxId
                )
            )
        }
    }

    suspend fun populateSampleDataIfEmpty() {
        if (subscriberDao.getSubscriberCount() == 0) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            val sub1 = VpnSubscriber(
                customerName = "হাসান মাহমুদ (Hasan)",
                phoneNumber = "01711223344",
                vpnPin = "STARK-984210",
                serviceName = "Stark VPN",
                startDateEpochMs = now - (28 * dayMs),
                expiryDateEpochMs = now + (2 * dayMs), // Expiring in 2 days!
                monthlyFee = 300.0,
                paidAmount = 200.0, // ৳100 due!
                notes = "দুবাই প্রবাসীকে পিন দেওয়া হয়েছে"
            )

            val sub2 = VpnSubscriber(
                customerName = "তানভীর আহমেদ (Tanvir)",
                phoneNumber = "01822334455",
                vpnPin = "OUTLINE-551982",
                serviceName = "Outline VPN",
                startDateEpochMs = now - (10 * dayMs),
                expiryDateEpochMs = now + (20 * dayMs), // Active 20 days left
                monthlyFee = 250.0,
                paidAmount = 250.0, // Fully paid
                notes = "ইউটিউব ও ব্রাউজিং স্পিড ভালো চায়"
            )

            val sub3 = VpnSubscriber(
                customerName = "সাকিব চৌধুরী (Sakib)",
                phoneNumber = "01933445566",
                vpnPin = "EXPRESS-110482",
                serviceName = "Express VPN",
                startDateEpochMs = now - (32 * dayMs),
                expiryDateEpochMs = now - (1 * dayMs), // Expired yesterday!
                monthlyFee = 350.0,
                paidAmount = 0.0, // Fully due
                notes = "মেয়াদ শেষ, বিল এখনও বকেয়া"
            )

            val sub4 = VpnSubscriber(
                customerName = "রাকিব হাসান (Rakib)",
                phoneNumber = "01644556677",
                vpnPin = "FAST-772911",
                serviceName = "Fast VPN",
                startDateEpochMs = now - (5 * dayMs),
                expiryDateEpochMs = now + (25 * dayMs), // Active
                monthlyFee = 200.0,
                paidAmount = 200.0,
                notes = "নগদে পেমেন্ট করেছে"
            )

            val id1 = subscriberDao.insertSubscriber(sub1)
            val id2 = subscriberDao.insertSubscriber(sub2)
            val id4 = subscriberDao.insertSubscriber(sub4)

            transactionDao.insertTransaction(
                PaymentTransaction(
                    subscriberId = id1,
                    customerName = sub1.customerName,
                    amount = 200.0,
                    paymentDateEpochMs = now - (28 * dayMs),
                    paymentMethod = "bKash",
                    transactionNote = "প্রাথমিক আংশিক পেমেন্ট",
                    trxId = "BK889123"
                )
            )
            transactionDao.insertTransaction(
                PaymentTransaction(
                    subscriberId = id2,
                    customerName = sub2.customerName,
                    amount = 250.0,
                    paymentDateEpochMs = now - (10 * dayMs),
                    paymentMethod = "Nagad",
                    transactionNote = "পূর্ণ মাসিক ফি পরিশোধ",
                    trxId = "NG110944"
                )
            )
            transactionDao.insertTransaction(
                PaymentTransaction(
                    subscriberId = id4,
                    customerName = sub4.customerName,
                    amount = 200.0,
                    paymentDateEpochMs = now - (5 * dayMs),
                    paymentMethod = "Nagad",
                    transactionNote = "সম্পূর্ণ মাসিক বিল পরিশোধ",
                    trxId = "NG992812"
                )
            )
            subscriberDao.insertSubscriber(sub3)
        }
    }
}
