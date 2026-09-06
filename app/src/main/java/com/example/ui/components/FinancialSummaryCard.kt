package com.example.ui.components

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
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishError
import com.example.ui.theme.PolishErrorContainer
import com.example.ui.theme.PolishOnErrorContainer
import com.example.ui.theme.PolishOnPrimaryContainer
import com.example.ui.theme.PolishOnSuccessContainer
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSuccess
import com.example.ui.theme.PolishSuccessContainer
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishWarning
import com.example.ui.theme.PolishWarningContainer

@Composable
fun FinancialSummaryRow(
    totalCollected: Double,
    totalDue: Double,
    activeCount: Int,
    expiringCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Grid Row 1: Active PINs & Due Bill (as featured in design HTML)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PolishMetricBox(
                label = "সক্রিয় পিন",
                value = "$activeCount",
                containerColor = PolishPrimaryContainer,
                valueColor = PolishOnPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            PolishMetricBox(
                label = "বকেয়া বিল",
                value = "৳ ${totalDue.toInt()}",
                containerColor = if (totalDue > 0) PolishErrorContainer else PolishSuccessContainer,
                valueColor = if (totalDue > 0) PolishOnErrorContainer else PolishOnSuccessContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid Row 2: Collected Revenue & Expiring Soon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PolishMetricBox(
                label = "মোট আদায়কৃত বিল",
                value = "৳ ${totalCollected.toInt()}",
                containerColor = PolishSuccessContainer.copy(alpha = 0.65f),
                valueColor = PolishOnSuccessContainer,
                modifier = Modifier.weight(1f)
            )

            PolishMetricBox(
                label = "মেয়াদ শেষ হচ্ছে",
                value = "$expiringCount জন",
                containerColor = if (expiringCount > 0) PolishWarningContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant,
                valueColor = if (expiringCount > 0) PolishWarning else PolishTextSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PolishMetricBox(
    label: String,
    value: String,
    containerColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = PolishTextSecondary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = valueColor
                )
            )
        }
    }
}

@Composable
fun ExpiryAlertBanner(
    expiringCount: Int,
    expiredCount: Int,
    onViewAlertsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (expiringCount == 0 && expiredCount == 0) return

    val isUrgent = expiredCount > 0
    val containerColor = if (isUrgent) PolishErrorContainer else PolishWarningContainer.copy(alpha = 0.5f)
    val contentColor = if (isUrgent) PolishOnErrorContainer else PolishWarning

    val title = if (isUrgent) {
        "সতর্কতা: $expiredCount জনের মেয়াদ শেষ, $expiringCount জনের মেয়াদ আসন্ন!"
    } else {
        "নোটিফিকেশন: $expiringCount জন গ্রাহকের মেয়াদ ৩ দিনের মধ্যে শেষ হবে!"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("expiry_alert_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alert",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = contentColor
                    )
                )
                Text(
                    text = "গ্রাহককে হোয়াটসঅ্যাপ বা মেসেজে দ্রুত রিনিউয়াল অ্যালার্ট পাঠান।",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = PolishTextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onViewAlertsClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 14.dp,
                    vertical = 6.dp
                ),
                modifier = Modifier.testTag("view_alert_button")
            ) {
                Text(
                    text = "দেখুন",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
