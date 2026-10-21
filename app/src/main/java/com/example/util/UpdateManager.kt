package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.data.model.GitHubAsset
import com.example.data.model.GitHubRelease
import com.example.data.remote.GitHubApiService
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

sealed class UpdateCheckResult {
    data class UpdateAvailable(
        val release: GitHubRelease,
        val apkAsset: GitHubAsset?,
        val currentVersion: String,
        val newVersion: String,
        val changelog: String
    ) : UpdateCheckResult()

    data class UpToDate(
        val currentVersion: String,
        val latestTag: String
    ) : UpdateCheckResult()

    data class Error(
        val message: String
    ) : UpdateCheckResult()
}

object UpdateManager {

    private const val TAG = "UpdateManager"
    private val apiService = GitHubApiService.create()

    private val downloadClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    /**
     * Checks if a new release exists on GitHub Releases.
     * Enforces the hardcoded allowed repo from BuildConfig and ignores user-editable values.
     */
    suspend fun checkForUpdate(
        ignoredUserRepo: String? = null,
        currentVersionName: String
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        val repoFullName = BuildConfig.ALLOWED_UPDATE_REPO
        val trimmed = repoFullName.trim().removePrefix("https://github.com/").removePrefix("github.com/").trim('/')
        val parts = trimmed.split("/")
        if (parts.size < 2) {
            return@withContext UpdateCheckResult.Error("نام مخزن مجاز نامعتبر است")
        }
        val owner = parts[0]
        val repo = parts[1]

        try {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Checking update for $owner/$repo against current version $currentVersionName")
            }
            val release = apiService.getLatestRelease(owner, repo)
            val remoteTag = release.tagName.ifBlank { release.name ?: "" }

            if (isNewerVersion(currentVersionName, remoteTag)) {
                val apk = release.apkAsset
                val changelog = release.body ?: "بهینه‌سازی و رفع باگ‌های گزارش‌شده"
                UpdateCheckResult.UpdateAvailable(
                    release = release,
                    apkAsset = apk,
                    currentVersion = currentVersionName,
                    newVersion = release.displayVersion.ifBlank { remoteTag },
                    changelog = changelog
                )
            } else {
                UpdateCheckResult.UpToDate(
                    currentVersion = currentVersionName,
                    latestTag = remoteTag
                )
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Update check failed: ${e.message}")
            }
            val msg = when {
                e.message?.contains("404") == true -> "هنوز هیچ انتشار رسمی (Release) در مخزن ثبت نشده است"
                e.message?.contains("403") == true -> "محدودیت نرخ دسترسی به گیت‌هاب؛ لطفاً کمی بعد تلاش کنید"
                else -> e.localizedMessage ?: "خطا در اتصال به سرور گیت‌هاب"
            }
            UpdateCheckResult.Error(msg)
        }
    }

    /**
     * Compares semantic versions (e.g. 1.2.0 vs 1.3.0 or v1.2.1)
     */
    fun isNewerVersion(current: String, remote: String): Boolean {
        val cleanCurrent = cleanVersion(current)
        val cleanRemote = cleanVersion(remote)

        if (cleanCurrent.isBlank() || cleanRemote.isBlank()) return false
        if (cleanCurrent == cleanRemote) return false

        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(currentParts.size, remoteParts.size)
        for (i in 0 until maxLen) {
            val c = currentParts.getOrElse(i) { 0 }
            val r = remoteParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    private fun cleanVersion(version: String): String {
        return version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .split("-")[0]
            .split("+")[0]
    }

    /**
     * Validates download domain to only permit official GitHub release distribution hosts
     */
    fun isAllowedDownloadUrl(url: String): Boolean {
        return try {
            val parsed = Uri.parse(url)
            val scheme = parsed.scheme?.lowercase()
            val host = parsed.host?.lowercase()
            scheme == "https" && (host == "github.com" || host == "objects.githubusercontent.com" || host?.endsWith(".githubusercontent.com") == true)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Calculates SHA-256 of downloaded file
     */
    fun calculateFileSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies that the APK SHA-256 matches the release checksums
     */
    suspend fun verifyApkSha256(apkFile: File, release: GitHubRelease): Result<Unit> = withContext(Dispatchers.IO) {
        val calculated = calculateFileSha256(apkFile).lowercase()
        val sumsAsset = release.assets.firstOrNull { it.name.equals("SHA256SUMS.txt", ignoreCase = true) }
        var verified = false

        if (sumsAsset != null && isAllowedDownloadUrl(sumsAsset.downloadUrl)) {
            try {
                val req = Request.Builder()
                    .url(sumsAsset.downloadUrl)
                    .header("User-Agent", "SamadFoodQR-Android")
                    .build()
                val resp = downloadClient.newCall(req).execute()
                if (resp.isSuccessful) {
                    val text = resp.body?.string() ?: ""
                    val hashes = Regex("""[a-fA-F0-9]{64}""").findAll(text).map { it.value.lowercase() }.toSet()
                    if (hashes.contains(calculated)) {
                        verified = true
                    }
                }
            } catch (_: Exception) {}
        }

        if (!verified) {
            val body = release.body ?: ""
            val hashes = Regex("""[a-fA-F0-9]{64}""").findAll(body).map { it.value.lowercase() }.toSet()
            if (hashes.contains(calculated)) {
                verified = true
            }
        }

        if (verified) {
            Result.success(Unit)
        } else {
            Result.failure(SecurityException("خطای امنیتی: فایل دانلود شده فاقد هش SHA-256 معتبر است یا با مقدار منتشرشده مطابقت ندارد."))
        }
    }

    /**
     * Downloads APK with progress callback into app's updates directory
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        fileName: String = "update.apk",
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        if (!isAllowedDownloadUrl(downloadUrl)) {
            return@withContext Result.failure(SecurityException("دانلود فایل فقط از سرورهای رسمی GitHub مجاز است."))
        }

        try {
            val updatesDir = File(context.filesDir, "updates")
            if (!updatesDir.exists()) {
                updatesDir.mkdirs()
            }
            val destinationFile = File(updatesDir, fileName)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "SamadFoodQR-Android")
                .header("Accept", "application/octet-stream")
                .build()

            val response = downloadClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("دانلود ناموفق بود (کد: ${response.code})"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("محتوای فایل خالی است"))
            val totalBytes = body.contentLength()

            body.byteStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            val progress = (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                    output.flush()
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(1f)
            }
            Result.success(destinationFile)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "APK download failed", e)
            }
            Result.failure(e)
        }
    }

    /**
     * Prompts Android package installer via FileProvider.
     * Handles missing REQUEST_INSTALL_PACKAGES permission by directing user to app install settings.
     */
    fun installApk(context: Context, apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                return Result.failure(Exception("فایل نصبی یافت نشد"))
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    return Result.failure(SecurityException("لطفاً در صفحه تنظیمات بازشده، اجازه نصب برنامه را فعال نمایید."))
                }
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Install launch failed", e)
            }
            Result.failure(e)
        }
    }

    /**
     * Opens release or repository in external browser
     */
    fun openInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Open browser failed", e)
            }
        }
    }

    /**
     * Shows notification to alert user of available update
     */
    fun notifyUserOfUpdate(context: Context, release: GitHubRelease) {
        NotificationHelper.showAppUpdateNotification(
            context = context,
            version = release.displayVersion,
            changelog = release.body,
            downloadUrl = release.apkAsset?.downloadUrl ?: release.htmlUrl
        )
    }
}
