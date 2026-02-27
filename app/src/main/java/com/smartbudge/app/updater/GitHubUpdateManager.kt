package com.smartbudge.app.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class GitHubUpdateManager(private val context: Context) {

    private val REPO_OWNER = "utsogharami5-source"
    private val REPO_NAME = "stitch-app"
    private val TAG = "GitHubUpdateManager"

    // Data class to hold release information
    data class ReleaseInfo(
        val versionName: String,
        val downloadUrl: String,
        val releaseNotes: String,
        val releaseUrl: String
    )

    /**
     * Checks if the device has an active internet connection.
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    /**
     * Checks the GitHub API for the latest release.
     * Compares the remote version with the currently installed version.
     * Returns a ReleaseInfo object if a newer version is available, null otherwise.
     */
    suspend fun checkForUpdates(currentVersion: String): ReleaseInfo? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "No internet connection available for update check.")
            return@withContext null
        }

        try {
            val url = URL("https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)

                var tagName = jsonObject.getString("tag_name")
                // Remove 'v' prefix if present (e.g., 'v1.0.1' -> '1.0.1')
                if (tagName.startsWith("v", ignoreCase = true)) {
                    tagName = tagName.substring(1)
                }
                
                val releaseNotes = jsonObject.optString("body", "No release notes provided.")
                val releaseUrl = jsonObject.getString("html_url")

                val assetsArray = jsonObject.getJSONArray("assets")
                var downloadUrl: String? = null
                
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk")) {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }

                if (downloadUrl != null && isNewerVersion(currentVersion, tagName)) {
                    Log.d(TAG, "Update available: $tagName")
                    return@withContext ReleaseInfo(tagName, downloadUrl, releaseNotes, releaseUrl)
                } else {
                    Log.d(TAG, "App is up to date. Current: $currentVersion, Remote: $tagName")
                }
            } else {
                Log.e(TAG, "Failed to check for updates. HTTP Code: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
        }
        return@withContext null
    }

    /**
     * Opens a URL in the system browser.
     */
    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open URL: $url", e)
        }
    }

    /**
     * Helper logic to compare semantic version strings (e.g., "1.0.0" < "1.0.1")
     */
    private fun isNewerVersion(current: String, remote: String): Boolean {
        // Trim to avoid comparison issues with whitespace or unexpected characters
        val curr = current.trim()
        val rem = remote.trim()
        
        val currentParts = curr.split(".").mapNotNull { it.toIntOrNull() }
        val remoteParts = rem.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(currentParts.size, remoteParts.size)
        for (i in 0 until length) {
            val c = currentParts.getOrElse(i) { 0 }
            val r = remoteParts.getOrElse(i) { 0 }
            if (c < r) return true
            if (c > r) return false
        }
        return false // Exactly the same
    }

    /**
     * Checks if the app has permission to install other apps (unknown sources).
     */
    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // Pre-Oreo doesn't have this specific request-based permission
        }
    }

    /**
     * Opens the system settings to allow the user to grant "Install unknown apps" permission.
     */
    fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Checks if storage permission is granted for legacy Android versions.
     */
    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Scoped storage on newer versions
        }
    }

    /**
     * Downloads the APK using Android's built-in DownloadManager
     * and automatically triggers the installation intent when finished.
     */
    fun downloadAndInstallUpdate(releaseInfo: ReleaseInfo) {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No internet connection available for download.")
            return
        }

        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(releaseInfo.downloadUrl)
            
            val fileName = "stitch_update.apk"
            
            // Files in app-specific directory are safer
            val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = DownloadManager.Request(uri)
                .setTitle("Downloading SmartBudge Update")
                .setDescription("Version ${releaseInfo.versionName}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setMimeType("application/vnd.android.package-archive")

            val downloadId = downloadManager.enqueue(request)
            Log.d(TAG, "Download enqueued with ID: $downloadId")

            // Register receiver
            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (downloadId == id) {
                        Log.d(TAG, "Download complete event received. Verifying status...")
                        
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = downloadManager.query(query)
                        
                        if (cursor != null && cursor.moveToFirst()) {
                            val statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val status = if (statusColumnIndex != -1) cursor.getInt(statusColumnIndex) else -1
                            
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                Log.d(TAG, "Download successful. Triggering installation.")
                                context.unregisterReceiver(this)
                                installApk(destinationFile)
                            } else {
                                val reasonColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                                val reason = if (reasonColumnIndex != -1) cursor.getInt(reasonColumnIndex) else -1
                                Log.e(TAG, "Download failed or remains incomplete. Status: $status, Reason: $reason")
                                // Optional: Context could be used to show a toast or notification about the failure
                            }
                        } else {
                            Log.e(TAG, "Failed to query download status.")
                        }
                        cursor?.close()
                    }
                }
            }
            
            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(onComplete, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(onComplete, filter)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start download", e)
        }
    }

    private fun installApk(file: File) {
        try {
            if (!file.exists()) {
                Log.e(TAG, "APK file not found: ${file.absolutePath}")
                return
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // For modern Android, explicit package name can help
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
            
            Log.d(TAG, "Starting installation intent for: $apkUri")
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start APK installation", e)
        }
    }
}
