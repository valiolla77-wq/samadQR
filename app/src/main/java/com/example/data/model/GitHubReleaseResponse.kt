package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubRelease(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "tag_name") val tagName: String = "",
    @Json(name = "name") val name: String? = null,
    @Json(name = "body") val body: String? = null,
    @Json(name = "html_url") val htmlUrl: String = "",
    @Json(name = "published_at") val publishedAt: String? = null,
    @Json(name = "draft") val draft: Boolean = false,
    @Json(name = "prerelease") val prerelease: Boolean = false,
    @Json(name = "assets") val assets: List<GitHubAsset> = emptyList()
) {
    val apkAsset: GitHubAsset?
        get() = getBestApkAsset()

    fun getBestApkAsset(): GitHubAsset? {
        val apkList = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        if (apkList.isEmpty()) return assets.firstOrNull()
        if (apkList.size == 1) return apkList.first()

        // Match device supported ABIs in order of preference (e.g. Samsung M12 on 32-bit prefers armeabi-v7a)
        val supportedAbis = android.os.Build.SUPPORTED_ABIS.map { it.lowercase() }
        for (abi in supportedAbis) {
            val matching = apkList.firstOrNull { it.name.lowercase().contains(abi) }
            if (matching != null) return matching
        }

        // If no exact ABI match found, look for universal APK
        val universal = apkList.firstOrNull { it.name.contains("universal", ignoreCase = true) }
        if (universal != null) return universal

        // Fallback to any APK
        return apkList.first()
    }

    val displayVersion: String
        get() = tagName.removePrefix("v").removePrefix("V")
}

@JsonClass(generateAdapter = true)
data class GitHubAsset(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "name") val name: String = "",
    @Json(name = "size") val size: Long = 0L,
    @Json(name = "download_count") val downloadCount: Int = 0,
    @Json(name = "browser_download_url") val downloadUrl: String = "",
    @Json(name = "content_type") val contentType: String? = null
) {
    val formattedSize: String
        get() {
            if (size <= 0) return ""
            val mb = size.toDouble() / (1024.0 * 1024.0)
            return "%.1f مگابایت".format(mb)
        }
}
