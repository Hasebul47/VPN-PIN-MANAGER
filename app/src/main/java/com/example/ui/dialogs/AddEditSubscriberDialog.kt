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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditSubscriberDialog(
    subscriber: VpnSubscriber? = null,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit,
    onUpdateExisting: ((VpnSubscriber) -> Unit)? = null
) {
    var name by remember { mutableStateOf(subscriber?.customerName ?: "") }
    var phone by remember { mutableStateOf(subscriber?.phoneNumber ?: "") }
    var pin by remember { mutableStateOf(subscriber?.vpnPin ?: "") }
    var service by remember { mutableStateOf(subscriber?.serviceName ?: "Stark VPN") }
    var durationDays by remember { mutableStateOf(30) }
    var monthlyFeeStr by remember { mutableStateOf(subscriber?.monthlyFee?.toInt()?.toString() ?: "300") }
    var paidAmountStr by remember { mutableStateOf(subscriber?.paidAmount?.toInt()?.toString() ?: "300") }
    var paymentMethod by remember { mutableStateOf("bKash") }
    var trxId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(subscriber?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }

    val isEditing = subscriber != null

    val services = listOf("Stark VPN", "Outline VPN", "Express VPN", "OpenVPN", "Fast VPN", "V2Ray", "অন্যান্য")
    val durations = listOf(30 to "১ মাস (৩০ দিন)", 60 to "২ মাস (৬০ দিন)", 90 to "৩ মাস (৯০ দিন)")
    val paymentMethods = listOf("bKash", "Nagad", "Rocket", "Cash", "Bank")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("add_edit_subscriber_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEditing) "গ্রাহকের তথ্য পরিবর্তন" else "নতুন গ্রাহক ও ভিপিএন পিন",
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

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("গ্রাহকের নাম *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimary) },
                    isError = nameError,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name")
                )
                if (nameError) {
                    Text("গ্রাহকের নাম আবশ্যক", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = false
                    },
                    label = { Text("ফোন নম্বর *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PolishPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_phone")
                )
                if (phoneError) {
                    Text("ফোন নম্বর আবশ্যক", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // VPN Service selector
                Text(
                    text = "ভিপিএন সার্ভিস নির্বাচন করুন",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = PolishTextSecondary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    services.forEach { s ->
                        FilterChip(
                            selected = service == s,
                            onClick = { service = s },
                            shape = CircleShape,
                            label = { Text(s, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolishPrimaryContainer,
                                selectedLabelColor = PolishPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // VPN PIN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            pin = it
                            pinError = false
                        },
                        label = { Text("ভিপিএন পিন / কোড *") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = PolishPrimary) },
                        isError = pinError,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vpn_pin")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val cleanService = service.filter { it.isLetter() }.take(4).uppercase()
                                .ifEmpty { "VPN" }
                            val randomDigits = (100000..999999).random()
                            pin = "$cleanService-$randomDigits"
                            pinError = false
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        modifier = Modifier.testTag("generate_pin_button")
                    ) {
                        Text("অটো পিন", fontSize = 12.sp)
                    }
                }
                if (pinError) {
                    Text("ভিপিএন পিন আবশ্যক", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                if (!isEditing) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "সাবস্ক্রিপশনের মেয়াদ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = PolishTextSecondary
                        )
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durations.forEach { (days, label) ->
                            FilterChip(
                                selected = durationDays == days,
                                onClick = { durationDays = days },
                                shape = CircleShape,
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PolishPrimaryContainer,
                                    selectedLabelColor = PolishPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = monthlyFeeStr,
                        onValueChange = { monthlyFeeStr = it },
                        label = { Text("মাসিক ফি (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_monthly_fee")
                    )

                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        label = { Text("জমা পরিশোধ (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_paid_amount")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                    paymentMethods.forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            shape = CircleShape,
                            label = { Text(method, fontSize = 12.sp) },
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("নোট / মন্তব্য (ঐচ্ছিক)") },
                    singleLine = false,
                    maxLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (Pill shaped)
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
                            var hasError = false
                            if (name.isBlank()) {
                                nameError = true
                                hasError = true
                            }
                            if (phone.isBlank()) {
                                phoneError = true
                                hasError = true
                            }
                            if (pin.isBlank()) {
                                pinError = true
                                hasError = true
                            }
                            if (hasError) return@Button

                            val fee = monthlyFeeStr.toDoubleOrNull() ?: 0.0
                            val paid = paidAmountStr.toDoubleOrNull() ?: 0.0

                            if (isEditing && subscriber != null && onUpdateExisting != null) {
                                onUpdateExisting(
                                    subscriber.copy(
                                        customerName = name.trim(),
                                        phoneNumber = phone.trim(),
                                        vpnPin = pin.trim(),
                                        serviceName = service.trim(),
                                        monthlyFee = fee,
                                        paidAmount = paid,
                                        notes = notes.trim()
                                    )
                                )
                            } else {
                                onSave(
                                    name,
                                    phone,
                                    pin,
                                    service,
                                    durationDays,
                                    fee,
                                    paid,
                                    paymentMethod,
                                    trxId,
                                    notes
                                )
                            }
                            onDismiss()
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        modifier = Modifier.testTag("save_subscriber_button")
                    ) {
                        Text(if (isEditing) "আপডেট করুন" else "সংরক্ষণ করুন", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
