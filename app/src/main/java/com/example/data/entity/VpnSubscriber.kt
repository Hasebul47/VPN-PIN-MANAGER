package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.ceil

@Entity(tableName = "vpn_subscribers")
data class VpnSubscriber(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val phoneNumber: String,
    val vpnPin: String,
    val serviceName: String = "VPN Pro",
    val startDateEpochMs: Long,
    val expiryDateEpochMs: Long,
    val monthlyFee: Double = 0.0,
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val createdAtEpochMs: Long = System.currentTimeMillis()
) {
    val dueAmount: Double
        get() = maxOf(0.0, monthlyFee - paidAmount)

    fun getDaysRemaining(currentTimeMs: Long = System.currentTimeMillis()): Int {
        val diffMs = expiryDateEpochMs - currentTimeMs
        return if (diffMs <= 0) 0 else ceil(diffMs.toDouble() / (1000 * 60 * 60 * 24)).toInt()
    }

    fun isExpired(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        return expiryDateEpochMs <= currentTimeMs
    }

    fun isExpiringSoon(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        val days = getDaysRemaining(currentTimeMs)
        return !isExpired(currentTimeMs) && days <= 3
    }

    val isPaid: Boolean
        get() = dueAmount <= 0.0

    val isPartial: Boolean
        get() = paidAmount > 0.0 && dueAmount > 0.0

    val isUnpaid: Boolean
        get() = paidAmount <= 0.0 && monthlyFee > 0.0
}
