package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.VpnSubscriber
import com.example.ui.FilterType
import com.example.ui.components.ExpiryAlertBanner
import com.example.ui.components.FinancialSummaryRow
import com.example.ui.components.SubscriberCard
import com.example.ui.theme.PolishError
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSuccess
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishWarning

@Composable
fun SubscribersListScreen(
    subscribers: List<VpnSubscriber>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: FilterType,
    onFilterChange: (FilterType) -> Unit,
    totalCollected: Double,
    totalDue: Double,
    activeCount: Int,
    expiringCount: Int,
    expiredCount: Int,
    onRenewClick: (VpnSubscriber) -> Unit,
    onPayClick: (VpnSubscriber) -> Unit,
    onEditClick: (VpnSubscriber) -> Unit,
    onDeleteClick: (VpnSubscriber) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("subscribers_list_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Financial Metrics Row (Polish Style)
        item {
            FinancialSummaryRow(
                totalCollected = totalCollected,
                totalDue = totalDue,
                activeCount = activeCount,
                expiringCount = expiringCount
            )
        }

        // Expiry Alert Banner
        item {
            ExpiryAlertBanner(
                expiringCount = expiringCount,
                expiredCount = expiredCount,
                onViewAlertsClick = {
                    onFilterChange(FilterType.EXPIRING_SOON)
                }
            )
        }

        // Search Bar (Pill shaped M3 search)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subscriber_search_bar"),
                    placeholder = {
                        Text(
                            "নাম, মোবাইল নম্বর বা ভিপিএন পিন খুঁজুন...",
                            fontSize = 13.sp,
                            color = PolishTextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PolishTextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = PolishTextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishOutline
                    )
                )
            }
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterType.values().forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val chipAccent = when (filter) {
                        FilterType.ALL -> PolishPrimary
                        FilterType.EXPIRING_SOON -> PolishWarning
                        FilterType.EXPIRED -> PolishError
                        FilterType.DUE_BILLS -> PolishError
                        FilterType.PAID -> PolishSuccess
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        shape = CircleShape,
                        label = {
                            Text(
                                text = filter.labelBn,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PolishPrimaryContainer,
                            selectedLabelColor = PolishPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = PolishTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) PolishPrimary else PolishOutline
                        ),
                        modifier = Modifier.testTag("filter_chip_${filter.name}")
                    )
                }
            }
        }

        // Section Title: Matches the HTML "গ্রাহক তালিকা ও স্থিতি"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "গ্রাহক তালিকা ও স্থিতি (${subscribers.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = PolishTextSecondary
                    )
                )
                Text(
                    text = selectedFilter.labelBn,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PolishPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // Empty State
        if (subscribers.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(PolishPrimaryContainer.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "কোনো গ্রাহক পাওয়া যায়নি",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = PolishTextPrimary
                        )
                    )
                    Text(
                        text = "ফিল্টার পরিবর্তন করুন অথবা + বাটনে নতুন গ্রাহক যোগ করুন",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            // Subscriber Cards
            items(subscribers, key = { it.id }) { sub ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    SubscriberCard(
                        subscriber = sub,
                        onRenewClick = onRenewClick,
                        onPayClick = onPayClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick
                    )
                }
            }
        }
    }
}
