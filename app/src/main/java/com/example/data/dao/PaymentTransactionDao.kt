package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.PaymentTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentTransactionDao {
    @Query("SELECT * FROM payment_transactions ORDER BY paymentDateEpochMs DESC")
    fun getAllTransactions(): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM payment_transactions WHERE subscriberId = :subscriberId ORDER BY paymentDateEpochMs DESC")
    fun getTransactionsForSubscriber(subscriberId: Long): Flow<List<PaymentTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PaymentTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<PaymentTransaction>)

    @Delete
    suspend fun deleteTransaction(transaction: PaymentTransaction)

    @Query("DELETE FROM payment_transactions WHERE subscriberId = :subscriberId")
    suspend fun deleteBySubscriberId(subscriberId: Long)

    @Query("DELETE FROM payment_transactions")
    suspend fun deleteAll()
}
