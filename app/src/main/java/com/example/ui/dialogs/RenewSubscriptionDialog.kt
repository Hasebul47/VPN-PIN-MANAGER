package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.VpnSubscriber
import com.example.ui.VpnViewModel
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RenewSubscriptionDialog(
    subscriber: VpnSubscriber,
    onDismiss: () -> Unit,
    onConfirmRenew: (days: Int, fee: Double, paidNow: Double, method: String, trxId: String) -> Unit
) {
    var selectedDays by remember { mutableStateOf(30) }
    var renewalFeeStr by remember { mutableStateOf(subscriber.monthlyFee.toInt().toString()) }
    var paidNowStr by remember { mutableStateOf(subscriber.monthlyFee.toInt().toString()) }
    var paymentMethod by remember { mutableStateOf("bKash") }
    var trxId by remember { mutableStateOf("") }

    val daysOptions = listOf(30 to "১ মাস (৩০ দিন)", 60 to "২ মাস (৬০ দিন)", 90 to "৩ মাস (৯০ দিন)")
    val methods = listOf("bKash", "Nagad", "Rocket", "Cash")

    val now = System.currentTimeMillis()
    val baseTime = if (subscriber.expiryDateEpochMs > now) subscriber.expiryDateEpochMs else now
    val projectedExpiry = baseTime + (selectedDays.toLong() * 24 * 60 * 60 * 1000L)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("renew_subscription_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "সাবস্ক্রিপশন রিনিউ করুন",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = PolishTextPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PolishTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PolishPrimaryContainer.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(0.5.dp, PolishPrimary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = subscriber.customerName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = PolishTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "বর্তমান মেয়াদ: ${VpnViewModel.formatDate(subscriber.expiryDateEpochMs)}",
                            fontSize = 12.sp,
                            color = PolishTextSecondary
                        )
                        Text(
                            text = "নতুন মেয়াদ উত্তীর্ণ: ${VpnViewModel.formatDate(projectedExpiry)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "রিনিউয়ালের মেয়াদ বৃদ্ধি",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = PolishTextSecondary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    daysOptions.forEach { (days, label) ->
                        FilterChip(
                            selected = selectedDays == days,
                            onClick = {
                                selectedDays = days
                                val multiplier = days / 30
                                val baseFee = subscriber.monthlyFee
                                renewalFeeStr = (baseFee * multiplier).toInt().toString()
                                paidNowStr = (baseFee * multiplier).toInt().toString()
                            },
                            shape = CircleShape,
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolishPrimaryContainer,
                                selectedLabelColor = PolishPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = renewalFeeStr,
                        onValueChange = { renewalFeeStr = it },
                        label = { Text("রিনিউয়াল ফি (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = paidNowStr,
                        onValueChange = { paidNowStr = it },
                        label = { Text("পরিশোধ (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "পেমেন্ট মাধ্যম",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = PolishTextSecondary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    methods.forEach { m ->
                        FilterChip(
                            selected = paymentMethod == m,
                            onClick = { paymentMethod = m },
                            shape = CircleShape,
                            label = { Text(m, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolishPrimaryContainer,
                                selectedLabelColor = PolishPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = trxId,
                    onValueChange = { trxId = it },
                    label = { Text("ট্রানজেকশন আইডি (TrxID - ঐচ্ছিক)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape
                    ) {
                        Text("বাতিল", color = PolishTextSecondary)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val fee = renewalFeeStr.toDoubleOrNull() ?: subscriber.monthlyFee
                            val paid = paidNowStr.toDoubleOrNull() ?: 0.0
                            onConfirmRenew(selectedDays, fee, paid, paymentMethod, trxId.trim())
                            onDismiss()
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        modifier = Modifier.testTag("confirm_renew_button")
                    ) {
                        Text("রিনিউ নিশ্চিত করুন", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
