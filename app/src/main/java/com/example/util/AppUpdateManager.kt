package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    object NoUpdate : UpdateState
    data class UpdateAvailable(
        val latestVersion: String,
        val releaseNotes: String,
        val apkUrl: String,
        val apkSizeMb: Double
    ) : UpdateState
    data class Downloading(val progressPercent: Int) : UpdateState
    data class ReadyToInstall(val apkFile: File) : UpdateState
    data class Error(val message: String) : UpdateState
}

object AppUpdateManager {
    private const val GITHUB_REPO = "Hasebul47/VPN-PIN-MANAGER"
    private const val RELEASES_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun resetState() {
        _updateState.value = UpdateState.Idle
    }

    suspend fun checkForUpdates(currentVersion: String = BuildConfig.VERSION_NAME) {
        withContext(Dispatchers.IO) {
            _updateState.value = UpdateState.Checking
            try {
                val url = URL(RELEASES_API_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "VPN-Pin-Manager-App")
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    _updateState.value = UpdateState.NoUpdate
                    return@withContext
                }

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    _updateState.value = UpdateState.Error("Server returned code ${connection.responseCode}")
                    return@withContext
                }

                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseBody)

                val tagName = json.optString("tag_name", "")
                val releaseNotes = json.optString("body", "নতুন আপডেট এবং ফিচার যোগ করা হয়েছে।")
                val assets = json.optJSONArray("assets")

                var apkDownloadUrl = ""
                var apkSizeBytes = 0L

                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url", "")
                            apkSizeBytes = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                if (apkDownloadUrl.isNotBlank() && isNewerVersion(tagName, currentVersion)) {
                    val sizeMb = if (apkSizeBytes > 0) apkSizeBytes / (1024.0 * 1024.0) else 0.0
                    _updateState.value = UpdateState.UpdateAvailable(
                        latestVersion = tagName,
                        releaseNotes = releaseNotes,
                        apkUrl = apkDownloadUrl,
                        apkSizeMb = sizeMb
                    )
                } else {
                    _updateState.value = UpdateState.NoUpdate
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Update check failed")
            }
        }
    }

    suspend fun downloadAndInstallApk(context: Context, apkUrl: String) {
        withContext(Dispatchers.IO) {
            _updateState.value = UpdateState.Downloading(0)
            try {
                val destinationFile = File(context.cacheDir, "vpn_pin_manager_update.apk")
                if (destinationFile.exists()) {
                    destinationFile.delete()
                }

                val url = URL(apkUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "VPN-Pin-Manager-App")
                }

                val totalSize = connection.contentLength
                var downloadedBytes = 0L

                val inputStream: InputStream = connection.inputStream
                val outputStream = FileOutputStream(destinationFile)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var lastReportedPercent = 0

                outputStream.use { out ->
                    inputStream.use { input ->
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            out.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalSize > 0) {
                                val currentPercent = ((downloadedBytes * 100) / totalSize).toInt()
                                if (currentPercent != lastReportedPercent) {
                                    lastReportedPercent = currentPercent
                                    _updateState.value = UpdateState.Downloading(currentPercent)
                                }
                            }
                        }
                    }
                }

                _updateState.value = UpdateState.ReadyToInstall(destinationFile)
                withContext(Dispatchers.Main) {
                    installApk(context, destinationFile)
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Download failed: ${e.message}")
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(permissionIntent)
                    return
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Installation launch failed: ${e.message}")
        }
    }

    fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
        val cleanRemote = remoteVersion.trimStart('v', 'V').trim()
        val cleanCurrent = currentVersion.trimStart('v', 'V').trim()

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
