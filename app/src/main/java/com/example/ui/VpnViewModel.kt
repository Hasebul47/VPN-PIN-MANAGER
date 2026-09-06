package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.VpnRepository
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.entity.PaymentTransaction
import com.example.data.entity.VpnSubscriber
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class FilterType(val labelBn: String, val labelEn: String) {
    ALL("সকল গ্রাহক", "All"),
    EXPIRING_SOON("মেয়াদ শেষ হচ্ছে (৩ দিন)", "Expiring Soon"),
    EXPIRED("মেয়াদ শেষ", "Expired"),
    DUE_BILLS("বকেয়া বিল", "Due Bills"),
    PAID("পরিশোধিত", "Paid")
}

enum class NavigationTab(val labelBn: String, val labelEn: String) {
    SUBSCRIBERS("গ্রাহক ও পিন", "Subscribers"),
    PAYMENTS("পেমেন্ট ট্র্যাকিং", "Payments"),
    FINANCIAL_OVERVIEW("আর্থিক হিসাব", "Overview")
}

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VpnRepository
    val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VpnRepository(db.vpnSubscriberDao(), db.paymentTransactionDao())
        viewModelScope.launch {
            repository.populateSampleDataIfEmpty()
        }
    }

    val allSubscribers: StateFlow<List<VpnSubscriber>> = repository.allSubscribers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<PaymentTransaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow(FilterType.ALL)
    val currentTab = MutableStateFlow(NavigationTab.SUBSCRIBERS)

    val filteredSubscribers: StateFlow<List<VpnSubscriber>> = combine(
        allSubscribers,
        searchQuery,
        selectedFilter
    ) { list, query, filter ->
        val now = System.currentTimeMillis()
        list.filter { sub ->
            val matchesQuery = query.isBlank() ||
                    sub.customerName.contains(query, ignoreCase = true) ||
                    sub.phoneNumber.contains(query, ignoreCase = true) ||
                    sub.vpnPin.contains(query, ignoreCase = true) ||
                    sub.serviceName.contains(query, ignoreCase = true)

            if (!matchesQuery) return@filter false

            when (filter) {
                FilterType.ALL -> true
                FilterType.EXPIRING_SOON -> sub.isExpiringSoon(now)
                FilterType.EXPIRED -> sub.isExpired(now)
                FilterType.DUE_BILLS -> sub.dueAmount > 0.0
                FilterType.PAID -> sub.isPaid
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: FilterType) {
        selectedFilter.value = filter
    }

    fun onTabSelected(tab: NavigationTab) {
        currentTab.value = tab
    }

    fun addSubscriber(
        name: String,
        phone: String,
        pin: String,
        service: String,
        durationDays: Int,
        monthlyFee: Double,
        paidAmount: Double,
        paymentMethod: String,
        trxId: String,
        notes: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val expiry = now + (durationDays.toLong() * 24 * 60 * 60 * 1000L)
            val subscriber = VpnSubscriber(
                customerName = name.trim(),
                phoneNumber = phone.trim(),
                vpnPin = pin.trim(),
                serviceName = if (service.isBlank()) "Standard VPN" else service.trim(),
                startDateEpochMs = now,
                expiryDateEpochMs = expiry,
                monthlyFee = monthlyFee,
                paidAmount = paidAmount,
                notes = notes.trim()
            )
            repository.addSubscriber(subscriber, paymentMethod, trxId)
        }
    }

    fun updateSubscriber(subscriber: VpnSubscriber) {
        viewModelScope.launch {
            repository.updateSubscriber(subscriber)
        }
    }

    fun deleteSubscriber(subscriber: VpnSubscriber) {
        viewModelScope.launch {
            repository.deleteSubscriber(subscriber)
        }
    }

    fun recordPayment(
        subscriber: VpnSubscriber,
        amount: Double,
        method: String,
        trxId: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.recordPayment(subscriber, amount, method, trxId, note)
        }
    }

    fun renewSubscription(
        subscriber: VpnSubscriber,
        days: Int,
        renewalFee: Double,
        paidNow: Double,
        method: String,
        trxId: String
    ) {
        viewModelScope.launch {
            repository.renewSubscription(subscriber, days, renewalFee, paidNow, method, trxId)
        }
    }

    fun generateRandomPin(serviceName: String = "VPN"): String {
        val cleanService = serviceName.filter { it.isLetter() }.take(4).uppercase(Locale.ROOT)
            .ifEmpty { "VPN" }
        val randomDigits = (100000..999999).random()
        return "$cleanService-$randomDigits"
    }

    fun exportBackupJson(): String {
        val subList = allSubscribers.value
        val txList = allTransactions.value

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportTimeEpochMs", System.currentTimeMillis())

        val subArray = JSONArray()
        subList.forEach { sub ->
            val obj = JSONObject()
            obj.put("id", sub.id)
            obj.put("customerName", sub.customerName)
            obj.put("phoneNumber", sub.phoneNumber)
            obj.put("vpnPin", sub.vpnPin)
            obj.put("serviceName", sub.serviceName)
            obj.put("startDateEpochMs", sub.startDateEpochMs)
            obj.put("expiryDateEpochMs", sub.expiryDateEpochMs)
            obj.put("monthlyFee", sub.monthlyFee)
            obj.put("paidAmount", sub.paidAmount)
            obj.put("notes", sub.notes)
            subArray.put(obj)
        }
        root.put("subscribers", subArray)

        val txArray = JSONArray()
        txList.forEach { tx ->
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("subscriberId", tx.subscriberId)
            obj.put("customerName", tx.customerName)
            obj.put("amount", tx.amount)
            obj.put("paymentDateEpochMs", tx.paymentDateEpochMs)
            obj.put("paymentMethod", tx.paymentMethod)
            obj.put("transactionNote", tx.transactionNote)
            obj.put("trxId", tx.trxId)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        return root.toString(2)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val subArray = root.getJSONArray("subscribers")
            viewModelScope.launch {
                for (i in 0 until subArray.length()) {
                    val obj = subArray.getJSONObject(i)
                    val sub = VpnSubscriber(
                        customerName = obj.optString("customerName", "Customer"),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        vpnPin = obj.optString("vpnPin", ""),
                        serviceName = obj.optString("serviceName", "VPN Pro"),
                        startDateEpochMs = obj.optLong("startDateEpochMs", System.currentTimeMillis()),
                        expiryDateEpochMs = obj.optLong("expiryDateEpochMs", System.currentTimeMillis() + 30L * 86400000L),
                        monthlyFee = obj.optDouble("monthlyFee", 0.0),
                        paidAmount = obj.optDouble("paidAmount", 0.0),
                        notes = obj.optString("notes", "")
                    )
                    repository.updateSubscriber(sub)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

        fun formatDate(epochMs: Long): String {
            return dateFormat.format(Date(epochMs))
        }

        fun formatDateTime(epochMs: Long): String {
            return timeFormat.format(Date(epochMs))
        }
    }
}
