package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PaymentTransaction
import com.example.data.entity.VpnSubscriber
import com.example.ui.theme.PolishError
import com.example.ui.theme.PolishErrorContainer
import com.example.ui.theme.PolishOnErrorContainer
import com.example.ui.theme.PolishOnPrimaryContainer
import com.example.ui.theme.PolishOnSuccessContainer
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSuccess
import com.example.ui.theme.PolishSuccessContainer
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun FinancialOverviewScreen(
    subscribers: List<VpnSubscriber>,
    transactions: List<PaymentTransaction>,
    onCollectPaymentClick: (VpnSubscriber) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalBilled = subscribers.sumOf { it.monthlyFee }
    val totalCollected = subscribers.sumOf { it.paidAmount }
    val totalDue = subscribers.sumOf { it.dueAmount }

    val dueSubscribers = subscribers.filter { it.dueAmount > 0 }
    val collectionRate = if (totalBilled > 0) ((totalCollected / totalBilled) * 100).toInt() else 100

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("financial_overview_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Financial Overview Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                border = BorderStroke(1.dp, PolishOutline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PolishPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = PolishPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "বিলিং ও আর্থিক সারসংক্ষেপ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                color = PolishTextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Metric Boxes (Polish Style)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialMetricBox(
                            title = "ধার্যকৃত বিল",
                            value = "৳ ${totalBilled.toInt()}",
                            containerColor = PolishPrimaryContainer,
                            textColor = PolishOnPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricBox(
                            title = "আদায়",
                            value = "৳ ${totalCollected.toInt()}",
                            containerColor = PolishSuccessContainer,
                            textColor = PolishOnSuccessContainer,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricBox(
                            title = "বকেয়া",
                            value = "৳ ${totalDue.toInt()}",
                            containerColor = if (totalDue > 0) PolishErrorContainer else PolishSuccessContainer.copy(alpha = 0.5f),
                            textColor = if (totalDue > 0) PolishOnErrorContainer else PolishOnSuccessContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Collection Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "বিল আদায়ের হার: $collectionRate%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = PolishTextPrimary
                            )
                        )
                        Text(
                            text = "${subscribers.size - dueSubscribers.size}/${subscribers.size} জন পরিশোধিত",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PolishTextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (collectionRate / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = PolishPrimary,
                        trackColor = PolishPrimaryContainer
                    )
                }
            }
        }

        // Section: Due Bills Alert Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (dueSubscribers.isNotEmpty()) PolishErrorContainer else PolishSuccessContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                (if (dueSubscribers.isNotEmpty()) PolishOnErrorContainer else PolishOnSuccessContainer).copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (dueSubscribers.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (dueSubscribers.isNotEmpty()) PolishOnErrorContainer else PolishOnSuccessContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (dueSubscribers.isNotEmpty()) {
                                "বকেয়া বিল সতর্কতা: ${dueSubscribers.size} জনের কাছে ৳${totalDue.toInt()} পাওনা!"
                            } else {
                                "কোনো বকেয়া বিল নেই, সব বিল পরিশোধিত।"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (dueSubscribers.isNotEmpty()) PolishOnErrorContainer else PolishOnSuccessContainer
                            )
                        )
                        if (dueSubscribers.isNotEmpty()) {
                            Text(
                                text = "নিচের তালিকা থেকে গ্রাহকদের সরাসরি বকেয়া বিল তাগাদা পাঠান।",
                                fontSize = 11.sp,
                                color = PolishTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Due Subscribers
        if (dueSubscribers.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MoneyOff,
                        contentDescription = null,
                        tint = PolishError,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "বকেয়া গ্রাহকদের তাগাদা তালিকা (${dueSubscribers.size} জন)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = PolishTextSecondary,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            items(dueSubscribers, key = { it.id }) { sub ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    DueSubscriberCard(
                        subscriber = sub,
                        onCollectPaymentClick = { onCollectPaymentClick(sub) },
                        onSendReminderClick = {
                            sendDueAlertMessage(context, sub)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FinancialMetricBox(
    title: String,
    value: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = PolishTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun DueSubscriberCard(
    subscriber: VpnSubscriber,
    onCollectPaymentClick: () -> Unit,
    onSendReminderClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, PolishError.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = subscriber.customerName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = PolishTextPrimary
                        )
                    )
                    Text(
                        text = "মোবাইল: ${subscriber.phoneNumber} | ভিপিএন: ${subscriber.serviceName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "বকেয়া: ৳ ${subscriber.dueAmount.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishError,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "বিল ৳${subscriber.monthlyFee.toInt()} | জমা ৳${subscriber.paidAmount.toInt()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = PolishTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onSendReminderClick,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, PolishError),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Reminder",
                        tint = PolishError,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("তাগাদা পাঠান", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PolishError)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onCollectPaymentClick,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = "Collect",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("টাকা জমা", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun sendDueAlertMessage(context: Context, subscriber: VpnSubscriber) {
    val message = "আসসালামু আলাইকুম ${subscriber.customerName}, আপনার ${subscriber.serviceName} ভিপিএন মাসিক বিল বাবদ ৳${subscriber.dueAmount.toInt()} বকেয়া রয়েছে। নিরবচ্ছিন্ন সেবা চালু রাখতে অনুগ্রহ করে বকেয়া বিলটি পরিশোধ করুন। ধন্যবাদ!"
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(sendIntent, "${subscriber.customerName} কে বকেয়া বিলের তাগাদা পাঠান"))
}
