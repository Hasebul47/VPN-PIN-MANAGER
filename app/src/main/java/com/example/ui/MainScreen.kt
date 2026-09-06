package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.VpnSubscriber
import com.example.ui.components.VpnHeader
import com.example.ui.dialogs.AddEditSubscriberDialog
import com.example.ui.dialogs.CloudSyncBackupDialog
import com.example.ui.dialogs.RecordPaymentDialog
import com.example.ui.dialogs.RenewSubscriptionDialog
import com.example.ui.screens.FinancialOverviewScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.SubscribersListScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.ui.dialogs.UpdateDialog
import com.example.util.AppUpdateManager
import com.example.util.UpdateState
import com.example.ui.theme.PolishBg
import com.example.ui.theme.PolishError
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun MainScreen(
    viewModel: VpnViewModel,
    modifier: Modifier = Modifier
) {
    val allSubscribers by viewModel.allSubscribers.collectAsStateWithLifecycle()
    val filteredSubscribers by viewModel.filteredSubscribers.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    val now = System.currentTimeMillis()
    val totalCollected = allSubscribers.sumOf { it.paidAmount }
    val totalDue = allSubscribers.sumOf { it.dueAmount }
    val activeCount = allSubscribers.count { !it.isExpired(now) }
    val expiringCount = allSubscribers.count { it.isExpiringSoon(now) }
    val expiredCount = allSubscribers.count { it.isExpired(now) }

    // Dialog States
    var showAddDialog by remember { mutableStateOf(false) }
    var subscriberToEdit by remember { mutableStateOf<VpnSubscriber?>(null) }
    var subscriberToRenew by remember { mutableStateOf<VpnSubscriber?>(null) }
    var subscriberToPay by remember { mutableStateOf<VpnSubscriber?>(null) }
    var subscriberToDelete by remember { mutableStateOf<VpnSubscriber?>(null) }
    var showCloudBackupDialog by remember { mutableStateOf(false) }

    // In-App Auto Update State
    val updateState by AppUpdateManager.updateState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AppUpdateManager.checkForUpdates()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PolishBg,
        topBar = {
            VpnHeader(
                onOpenCloudBackup = { showCloudBackupDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PolishSurfaceVariant,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.SUBSCRIBERS,
                    onClick = { viewModel.onTabSelected(NavigationTab.SUBSCRIBERS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Subscribers",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = NavigationTab.SUBSCRIBERS.labelBn,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == NavigationTab.SUBSCRIBERS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PolishPrimary,
                        selectedTextColor = PolishPrimary,
                        unselectedIconColor = PolishTextSecondary,
                        unselectedTextColor = PolishTextSecondary,
                        indicatorColor = PolishPrimaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_subscribers")
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.PAYMENTS,
                    onClick = { viewModel.onTabSelected(NavigationTab.PAYMENTS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Payments",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = NavigationTab.PAYMENTS.labelBn,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == NavigationTab.PAYMENTS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PolishPrimary,
                        selectedTextColor = PolishPrimary,
                        unselectedIconColor = PolishTextSecondary,
                        unselectedTextColor = PolishTextSecondary,
                        indicatorColor = PolishPrimaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_payments")
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.FINANCIAL_OVERVIEW,
                    onClick = { viewModel.onTabSelected(NavigationTab.FINANCIAL_OVERVIEW) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Overview",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = NavigationTab.FINANCIAL_OVERVIEW.labelBn,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == NavigationTab.FINANCIAL_OVERVIEW) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PolishPrimary,
                        selectedTextColor = PolishPrimary,
                        unselectedIconColor = PolishTextSecondary,
                        unselectedTextColor = PolishTextSecondary,
                        indicatorColor = PolishPrimaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_overview")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == NavigationTab.SUBSCRIBERS) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PolishPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_subscriber")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Subscriber",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.SUBSCRIBERS -> {
                    SubscribersListScreen(
                        subscribers = filteredSubscribers,
                        searchQuery = searchQuery,
                        onSearchChange = viewModel::onSearchQueryChanged,
                        selectedFilter = selectedFilter,
                        onFilterChange = viewModel::onFilterSelected,
                        totalCollected = totalCollected,
                        totalDue = totalDue,
                        activeCount = activeCount,
                        expiringCount = expiringCount,
                        expiredCount = expiredCount,
                        onRenewClick = { subscriberToRenew = it },
                        onPayClick = { subscriberToPay = it },
                        onEditClick = { subscriberToEdit = it },
                        onDeleteClick = { subscriberToDelete = it }
                    )
                }

                NavigationTab.PAYMENTS -> {
                    PaymentsScreen(
                        transactions = allTransactions,
                        totalCollected = totalCollected,
                        onRecordNewPaymentClick = {
                            val dueSub = allSubscribers.firstOrNull { it.dueAmount > 0 } ?: allSubscribers.firstOrNull()
                            if (dueSub != null) {
                                subscriberToPay = dueSub
                            } else {
                                showAddDialog = true
                            }
                        }
                    )
                }

                NavigationTab.FINANCIAL_OVERVIEW -> {
                    FinancialOverviewScreen(
                        subscribers = allSubscribers,
                        transactions = allTransactions,
                        onCollectPaymentClick = { subscriberToPay = it }
                    )
                }
            }
        }
    }

    // Add New Subscriber Dialog
    if (showAddDialog) {
        AddEditSubscriberDialog(
            subscriber = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, pin, service, durationDays, fee, paid, method, trxId, notes ->
                viewModel.addSubscriber(
                    name = name,
                    phone = phone,
                    pin = pin,
                    service = service,
                    durationDays = durationDays,
                    monthlyFee = fee,
                    paidAmount = paid,
                    paymentMethod = method,
                    trxId = trxId,
                    notes = notes
                )
            }
        )
    }

    // Edit Existing Subscriber Dialog
    subscriberToEdit?.let { sub ->
        AddEditSubscriberDialog(
            subscriber = sub,
            onDismiss = { subscriberToEdit = null },
            onSave = { _, _, _, _, _, _, _, _, _, _ -> },
            onUpdateExisting = { updated ->
                viewModel.updateSubscriber(updated)
            }
        )
    }

    // Record Payment Dialog
    subscriberToPay?.let { sub ->
        RecordPaymentDialog(
            subscriber = sub,
            onDismiss = { subscriberToPay = null },
            onConfirmPayment = { amount, method, trxId, note ->
                viewModel.recordPayment(sub, amount, method, trxId, note)
            }
        )
    }

    // Renew Subscription Dialog
    subscriberToRenew?.let { sub ->
        RenewSubscriptionDialog(
            subscriber = sub,
            onDismiss = { subscriberToRenew = null },
            onConfirmRenew = { days, fee, paidNow, method, trxId ->
                viewModel.renewSubscription(sub, days, fee, paidNow, method, trxId)
            }
        )
    }

    // Delete Confirmation Dialog
    subscriberToDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { subscriberToDelete = null },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    "গ্রাহক মুছুন",
                    fontWeight = FontWeight.SemiBold,
                    color = PolishTextPrimary
                )
            },
            text = {
                Text(
                    "${sub.customerName}-কে এবং তার সমস্ত তথ্য মুছে ফেলতে চান?",
                    color = PolishTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSubscriber(sub)
                        subscriberToDelete = null
                    }
                ) {
                    Text("মুছুন", color = PolishError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { subscriberToDelete = null }) {
                    Text("বাতিল", color = PolishPrimary)
                }
            }
        )
    }

    // Cloud Sync & Backup Dialog
    if (showCloudBackupDialog) {
        CloudSyncBackupDialog(
            onDismiss = { showCloudBackupDialog = false },
            onExportJson = { viewModel.exportBackupJson() },
            onImportJson = { json -> viewModel.importBackupJson(json) }
        )
    }

    // In-App Auto Update Dialog
    UpdateDialog(
        updateState = updateState,
        onDismiss = { AppUpdateManager.resetState() },
        onStartDownload = { url ->
            scope.launch {
                AppUpdateManager.downloadAndInstallApk(context, url)
            }
        }
    )
}
