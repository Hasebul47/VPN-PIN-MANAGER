package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.VpnSubscriber
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnSubscriberDao {
    @Query("SELECT * FROM vpn_subscribers ORDER BY expiryDateEpochMs ASC")
    fun getAllSubscribers(): Flow<List<VpnSubscriber>>

    @Query("SELECT * FROM vpn_subscribers WHERE id = :id")
    suspend fun getSubscriberById(id: Long): VpnSubscriber?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriber(subscriber: VpnSubscriber): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSubscribers(subscribers: List<VpnSubscriber>)

    @Update
    suspend fun updateSubscriber(subscriber: VpnSubscriber)

    @Delete
    suspend fun deleteSubscriber(subscriber: VpnSubscriber)

    @Query("DELETE FROM vpn_subscribers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE vpn_subscribers SET paidAmount = paidAmount + :amount WHERE id = :subscriberId")
    suspend fun addPayment(subscriberId: Long, amount: Double)

    @Query("UPDATE vpn_subscribers SET expiryDateEpochMs = :newExpiryDateMs, paidAmount = :newPaidAmount, monthlyFee = :newMonthlyFee WHERE id = :subscriberId")
    suspend fun renewSubscription(subscriberId: Long, newExpiryDateMs: Long, newPaidAmount: Double, newMonthlyFee: Double)

    @Query("SELECT COUNT(*) FROM vpn_subscribers")
    suspend fun getSubscriberCount(): Int

    @Query("DELETE FROM vpn_subscribers")
    suspend fun deleteAll()
}
