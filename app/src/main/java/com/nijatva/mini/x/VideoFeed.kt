package com.nijatva.mini.x

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/**
 * The feed: what NewPipe's search returns, which is the same list a visitor to the site
 * would see - a title, a thumbnail, how long it runs and how many have watched it.
 */
object VideoFeed {

    data class Card(
        val title: String,
        val url: String,
        val thumb: String,
        val duration: Long,
        val views: Long,
        val uploader: String
    )

    /** One loader for the whole app; NewPipe is a singleton too. */
    fun init() {
        try { NewPipe.init(NpDownloader()) } catch (_: Exception) { }
    }

    fun search(query: String, limit: Int = 20): List<Card> {
        init()
        val extractor = ServiceList.YouTube.getSearchExtractor(query)
        extractor.fetchPage()
        val out = ArrayList<Card>()
        collect(extractor.initialPage.items, out, limit)
        var page = extractor.nextPage
        var guard = 0
        while (out.size < limit && page != null && guard < 2) {
            extractor.fetchPage()
            collect(extractor.initialPage.items, out, limit)
            page = extractor.nextPage
            guard++
        }
        return out.take(limit)
    }

    private fun collect(items: List<org.schabi.newpipe.extractor.InfoItem>, out: MutableList<Card>, limit: Int) {
        for (i in items) {
            if (out.size >= limit) return
            val s = i as? StreamInfoItem ?: continue
            val url = s.url ?: continue
            val thumb = s.thumbnails?.maxByOrNull { it.height }?.url ?: ""
            out.add(Card(
                title = s.name ?: "",
                url = url,
                thumb = thumb,
                duration = s.duration,
                views = s.viewCount,
                uploader = s.uploaderName ?: ""
            ))
        }
    }

    /** A thumbnail, fetched small. No image library: one call, one bitmap. */
    fun thumb(url: String): Bitmap? {
        if (url.isBlank()) return null
        return try {
            val req = okhttp3.Request.Builder().url(url).build()
            client.newCall(req).execute().use { res ->
                val bytes = res.body?.bytes() ?: return null
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            }
        } catch (_: Exception) { null }
    }

    private val client = okhttp3.OkHttpClient()

    private class NpDownloader : Downloader() {
        override fun execute(request: Request): Response {
            val builder = okhttp3.Request.Builder()
                .method(request.httpMethod(), request.dataToSend()?.let {
                    okhttp3.RequestBody.create(null, it)
                })
                .url(request.url())
            for ((k, values) in request.headers()) for (v in values) builder.addHeader(k, v)
            client.newCall(builder.build()).execute().use { res ->
                return Response(res.code, res.message, res.headers.toMultimap(),
                    res.body?.string(), res.request.url.toString())
            }
        }
    }

    fun humanDuration(seconds: Long): String {
        if (seconds <= 0) return ""
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%d:%02d", m, s)
    }

    fun humanViews(v: Long): String = when {
        v >= 10_000_000 -> String.format("%.1f Cr views", v / 10_000_000.0)
        v >= 100_000 -> String.format("%.1f L views", v / 100_000.0)
        v >= 1_000 -> String.format("%.1fK views", v / 1_000.0)
        v > 0 -> "$v views"
        else -> ""
    }
}
