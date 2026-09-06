package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.VpnSubscriber
import com.example.ui.VpnViewModel
import com.example.ui.theme.PolishError
import com.example.ui.theme.PolishErrorContainer
import com.example.ui.theme.PolishOnErrorContainer
import com.example.ui.theme.PolishOnPrimaryContainer
import com.example.ui.theme.PolishOnSuccessContainer
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishOutlineVariant
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSuccess
import com.example.ui.theme.PolishSuccessContainer
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishWarning
import com.example.ui.theme.PolishWarningContainer

@Composable
fun SubscriberCard(
    subscriber: VpnSubscriber,
    onRenewClick: (VpnSubscriber) -> Unit,
    onPayClick: (VpnSubscriber) -> Unit,
    onEditClick: (VpnSubscriber) -> Unit,
    onDeleteClick: (VpnSubscriber) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()
    val isExpired = subscriber.isExpired(now)
    val isExpiring = subscriber.isExpiringSoon(now)
    val daysLeft = subscriber.getDaysRemaining(now)

    val cardAlpha = if (isExpired) 0.85f else 1.0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .testTag("subscriber_card_${subscriber.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, PolishOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Name, Phone & Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscriber.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = PolishTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subscriber.phoneNumber,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextSecondary,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "সার্ভিস: ${subscriber.serviceName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                }

                // Right Column: Expiry & Due info
                Column(horizontalAlignment = Alignment.End) {
                    when {
                        isExpired -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "মেয়াদ শেষ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTextSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                        isExpiring -> {
                            Surface(
                                shape = CircleShape,
                                color = PolishErrorContainer
                            ) {
                                Text(
                                    text = "মেয়াদ শেষ: $daysLeft দিন",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishOnErrorContainer
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                        else -> {
                            Surface(
                                shape = CircleShape,
                                color = PolishSuccessContainer
                            ) {
                                Text(
                                    text = "মেয়াদ: $daysLeft দিন বাকি",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PolishOnSuccessContainer
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (subscriber.dueAmount > 0) {
                        Text(
                            text = "বাকি: ৳ ${subscriber.dueAmount.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishError,
                                fontSize = 11.sp
                            )
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = PolishSuccessContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "পরিশোধিত",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PolishSuccess
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // VPN PIN Box with Copy Action
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PolishPrimaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(0.5.dp, PolishPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PIN: ${subscriber.vpnPin}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = PolishOnPrimaryContainer
                            )
                        )
                    }

                    Surface(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("VPN PIN", subscriber.vpnPin)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "পিন কপি করা হয়েছে: ${subscriber.vpnPin}", Toast.LENGTH_SHORT).show()
                        },
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy PIN",
                                tint = PolishPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "কপি",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PolishPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial & Expiry Detail Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "মেয়াদকাল: ${VpnViewModel.formatDate(subscriber.startDateEpochMs)} - ${VpnViewModel.formatDate(subscriber.expiryDateEpochMs)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PolishTextSecondary,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "বিল: ৳${subscriber.monthlyFee.toInt()} | জমা: ৳${subscriber.paidAmount.toInt()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PolishTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 0.5.dp,
                color = PolishOutlineVariant
            )

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Communication shortcuts (Call & WhatsApp/SMS reminder)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${subscriber.phoneNumber}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Customer",
                            tint = PolishPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            sendWhatsAppExpiryReminder(context, subscriber, daysLeft, isExpired)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Alert",
                            tint = if (isExpiring || isExpired) PolishError else PolishPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onEditClick(subscriber) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = PolishTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDeleteClick(subscriber) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = PolishTextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Action Buttons: Pay & Renew
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (subscriber.dueAmount > 0) {
                        OutlinedButton(
                            onClick = { onPayClick(subscriber) },
                            shape = CircleShape,
                            border = BorderStroke(1.dp, PolishError),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),
                            modifier = Modifier.testTag("pay_button_${subscriber.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = "Pay",
                                tint = PolishError,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "টাকা জমা",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishError
                            )
                        }
                    }

                    Button(
                        onClick = { onRenewClick(subscriber) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishPrimary
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 14.dp,
                            vertical = 6.dp
                        ),
                        modifier = Modifier.testTag("renew_button_${subscriber.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Renew",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "রিনিউ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun sendWhatsAppExpiryReminder(
    context: Context,
    subscriber: VpnSubscriber,
    daysLeft: Int,
    isExpired: Boolean
) {
    val message = if (isExpired) {
        "আসসালামু আলাইকুম ${subscriber.customerName}, আপনার ${subscriber.serviceName} ভিপিএন পিনের মেয়াদ শেষ হয়ে গেছে। নিরবচ্ছিন্ন ইন্টারনেট সেবা পেতে অনুগ্রহ করে রিনিউ করুন। পিন: ${subscriber.vpnPin}। ধন্যবাদ!"
    } else {
        "আসসালামু আলাইকুম ${subscriber.customerName}, আপনার ${subscriber.serviceName} ভিপিএন পিনের মেয়াদ আর মাত্র $daysLeft দিন বাকি আছে। নিরবচ্ছিন্ন সেবা অব্যাহত রাখতে আপনার সাবস্ক্রিপশনটি দ্রুত রিনিউ করুন। পিন: ${subscriber.vpnPin}। ধন্যবাদ!"
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(sendIntent, "${subscriber.customerName} কে ভিপিএন অ্যালার্ট পাঠান"))
}
