package com.example.ui.dialogs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishError
import com.example.ui.theme.PolishErrorContainer
import com.example.ui.theme.PolishOnErrorContainer
import com.example.ui.theme.PolishOnPrimary
import com.example.ui.theme.PolishOnPrimaryContainer
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.util.AppUpdateManager
import com.example.util.UpdateState

@Composable
fun UpdateDialog(
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onStartDownload: (String) -> Unit
) {
    val context = LocalContext.current

    when (updateState) {
        is UpdateState.UpdateAvailable -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PolishPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NewReleases,
                                contentDescription = "Update Available",
                                tint = PolishPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "নতুন আপডেট পাওয়া গেছে!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = "Update Available (${updateState.latestVersion})",
                                style = MaterialTheme.typography.bodySmall,
                                color = PolishTextSecondary
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PolishPrimaryContainer
                            ) {
                                Text(
                                    text = updateState.latestVersion,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishOnPrimaryContainer
                                )
                            }
                            if (updateState.apkSizeMb > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PolishSurfaceVariant
                                ) {
                                    Text(
                                        text = String.format("%.1f MB", updateState.apkSizeMb),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PolishTextSecondary
                                    )
                                }
                            }
                        }

                        Text(
                            text = "নতুন ফিচার ও বাগ ফিক্স পেতে অ্যাপটি এখনই আপডেট করুন। (Update now for latest features & fixes.)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PolishTextSecondary
                        )

                        if (updateState.releaseNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PolishSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "আপডেটের বিবরণ (Changelog):",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = updateState.releaseNotes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PolishTextSecondary
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onStartDownload(updateState.apkUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("আপডেট করুন (Update Now)", color = PolishOnPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("পরে (Later)", color = PolishTextSecondary)
                    }
                }
            )
        }

        is UpdateState.Downloading -> {
            AlertDialog(
                onDismissRequest = { /* Non-dismissible while downloading */ },
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PolishPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Downloading",
                                tint = PolishPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ডাউনলোড হচ্ছে...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = "Downloading update (${updateState.progressPercent}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = PolishTextSecondary
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { updateState.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PolishPrimary,
                            trackColor = PolishPrimaryContainer
                        )
                        Text(
                            text = "ডাউনলোড সম্পন্ন হলে ইন্সটলেশন স্বয়ংক্রিয়ভাবে শুরু হবে। অনুগ্রহ করে অপেক্ষা করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = PolishTextSecondary
                        )
                    }
                },
                confirmButton = {}
            )
        }

        is UpdateState.ReadyToInstall -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = "ডাউনলোড সম্পন্ন!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                },
                text = {
                    Text(
                        text = "নতুন সংস্করণ ইন্সটল করতে নিচের বাটনে চাপ দিন। (Tap button below to install new version.)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PolishTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { AppUpdateManager.installApk(context, updateState.apkFile) },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ইন্সটল করুন (Install Now)", color = PolishOnPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল (Cancel)", color = PolishTextSecondary)
                    }
                }
            )
        }

        is UpdateState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = "আপডেট ত্রুটি (Update Error)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishError
                    )
                },
                text = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PolishErrorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = updateState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = PolishOnErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ঠিক আছে (OK)", color = PolishOnPrimary)
                    }
                }
            )
        }

        else -> {
            // Idle, Checking, NoUpdate - no popup needed
        }
    }
}
