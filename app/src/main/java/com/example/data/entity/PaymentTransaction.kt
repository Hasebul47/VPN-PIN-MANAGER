package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_transactions")
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriberId: Long,
    val customerName: String,
    val amount: Double,
    val paymentDateEpochMs: Long = System.currentTimeMillis(),
    val paymentMethod: String = "bKash", // bKash, Nagad, Rocket, Cash, Bank
    val transactionNote: String = "",
    val trxId: String = ""
)
