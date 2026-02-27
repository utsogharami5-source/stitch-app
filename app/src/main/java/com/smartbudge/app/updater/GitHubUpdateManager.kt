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
    data class ReleaseInfo(val versionName: String, val downloadUrl: String, val releaseNotes: String)

    /**
     * Checks the GitHub API for the latest release.
     * Compares the remote version with the currently installed version.
     * Returns a ReleaseInfo object if a newer version is available, null otherwise.
     */
    suspend fun checkForUpdates(currentVersion: String): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)

                var tagName = jsonObject.getString("tag_name")
                // Remove 'v' prefix if present (e.g., 'v1.0.1' -> '1.0.1')
                if (tagName.startsWith("v", ignoreCase = true)) {
                    tagName = tagName.substring(1)
                }
                
                val releaseNotes = jsonObject.optString("body", "No release notes provided.")

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
                    return@withContext ReleaseInfo(tagName, downloadUrl, releaseNotes)
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
     * Helper logic to compare semantic version strings (e.g., "1.0.0" < "1.0.1")
     */
    private fun isNewerVersion(current: String, remote: String): Boolean {
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }

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
     * Downloads the APK using Android's built-in DownloadManager
     * and automatically triggers the installation intent when finished.
     */
    fun downloadAndInstallUpdate(releaseInfo: ReleaseInfo) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(releaseInfo.downloadUrl)
            
            val fileName = "stitch_update_v${releaseInfo.versionName}.apk"
            
            // Delete previous download if it exists to avoid FileUriExposedException on some devices
            val existingFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (existingFile.exists()) {
                existingFile.delete()
            }

            val request = DownloadManager.Request(uri)
                .setTitle("Downloading Stitch Update")
                .setDescription("Version ${releaseInfo.versionName}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadId = downloadManager.enqueue(request)
            
            Log.d(TAG, "Download enqueued with ID: $downloadId")

            // Register a receiver to know when the download is complete
            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downloadId == id) {
                        Log.d(TAG, "Download complete. Starting installation.")
                        installApk(fileName)
                        context.unregisterReceiver(this)
                    }
                }
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start download", e)
        }
    }

    private fun installApk(fileName: String) {
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (!file.exists()) {
                Log.e(TAG, "APK file not found for installation: ${file.absolutePath}")
                return
            }

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                ),
                "application/vnd.android.package-archive"
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to construct install intent", e)
        }
    }
}
