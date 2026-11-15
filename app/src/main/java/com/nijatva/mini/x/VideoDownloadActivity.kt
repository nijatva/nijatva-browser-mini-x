package com.nijatva.mini.x

import android.app.DownloadManager
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.stream.StreamInfo

/**
 * Paste a link, see the streams that exist for it, pick one, and hand it to the system
 * downloader. The extraction is NewPipe's, which reads the page the way a browser does
 * rather than pretending to be an API client.
 */
class VideoDownloadActivity : AppCompatActivity() {

    private val bg = 0xFF0D0D0D.toInt()
    private val ink = 0xFFF2F2F2.toInt()
    private val dim = 0xFF8A8A8A.toInt()
    private val red = 0xFFE53935.toInt()
    private val green = 0xFF2ECC71.toInt()

    private lateinit var list: LinearLayout
    private lateinit var status: TextView

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(12))
        }
        bar.addView(TextView(this).apply {
            text = "\u2039"; textSize = 26f; setTextColor(ink); setPadding(0, 0, dp(16), 0)
            setOnClickListener { finish() }
        })
        bar.addView(TextView(this).apply {
            text = "Download video"; textSize = 20f; setTextColor(ink)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        root.addView(bar)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(4), dp(16), dp(12))
        }
        val field = EditText(this).apply {
            hint = "Paste a video link"
            textSize = 14f
            setTextColor(ink)
            setHintTextColor(dim)
            setSingleLine(true)
            background = GradientDrawable().apply {
                cornerRadius = dp(22).toFloat()
                setColor(0xFF1A1A1A.toInt())
                setStroke(dp(1), 0xFF2A2A2A.toInt())
            }
            setPadding(dp(16), dp(10), dp(16), dp(10))
        }
        row.addView(field, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(TextView(this).apply {
            text = "Fetch"
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            background = GradientDrawable().apply { cornerRadius = dp(20).toFloat(); setColor(red) }
            setPadding(dp(18), dp(10), dp(18), dp(10))
            setOnClickListener { fetch(field.text.toString().trim()) }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            leftMargin = dp(8)
        })
        root.addView(row)

        status = TextView(this).apply {
            textSize = 13f
            setTextColor(dim)
            setPadding(dp(20), 0, dp(20), dp(8))
        }
        root.addView(status)

        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), 0, dp(16), dp(24)) }
        val scroll = ScrollView(this)
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)

        // a link handed over by the browser lands here ready to fetch
        intent?.getStringExtra("video_url")?.let {
            field.setText(it)
            fetch(it)
        }
    }

    private fun fetch(url: String) {
        if (url.isBlank()) { status.text = "Paste a link first."; return }
        status.text = "Reading the page..."
        list.removeAllViews()

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) { runCatching { extract(url) } }
            result.onSuccess { streams ->
                if (streams.isEmpty()) {
                    status.text = "No streams found for that link."
                    return@onSuccess
                }
                status.text = "${streams.size} streams"
                for (s in streams) list.addView(streamRow(s))
            }.onFailure { e ->
                status.text = "Could not read that page: " + (e.message ?: "unknown error")
            }
        }
    }

    private data class Pick(val label: String, val url: String, val size: String)

    private fun extract(url: String): List<Pick> {
        NewPipe.init(NpDownloader())
        val info = StreamInfo.getInfo(ServiceList.YouTube, url)
        val out = ArrayList<Pick>()
        for (v in info.videoStreams) {
            val label = v.resolution ?: "video"
            val size = if (v.itagItem != null) "" else ""
            out.add(Pick(label + "  " + v.format.name, v.url, size))
        }
        for (a in info.audioStreams) {
            out.add(Pick("audio  " + a.format.name, a.url, ""))
        }
        return out.sortedBy { it.label }
    }

    private fun streamRow(p: Pick): LinearLayout {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(14), dp(4), dp(14))
        }
        r.addView(TextView(this).apply {
            text = p.label; textSize = 14.5f; setTextColor(ink)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        r.addView(TextView(this).apply {
            text = "Download"; textSize = 13.5f; setTextColor(green)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        r.setOnClickListener { save(p) }
        return r
    }

    private fun save(p: Pick) {
        try {
            val name = "minix-" + System.currentTimeMillis() + ".mp4"
            val req = DownloadManager.Request(Uri.parse(p.url))
                .setTitle(name)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, name)
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(req)
            status.text = "Queued. It appears under My Files when it finishes."
        } catch (e: Exception) {
            status.text = "Could not start the download: " + (e.message ?: "")
        }
    }

    /** NewPipe asks for a Downloader; this one is OkHttp and nothing else. */
    private class NpDownloader : Downloader() {
        private val client = okhttp3.OkHttpClient()

        override fun execute(request: Request): Response {
            val builder = okhttp3.Request.Builder()
                .method(request.httpMethod(), request.dataToSend()?.let {
                    okhttp3.RequestBody.create(null, it)
                })
                .url(request.url())
            for ((k, v) in request.headers()) builder.addHeader(k, v)
            client.newCall(builder.build()).execute().use { res ->
                return Response(res.code, res.message, res.headers.toMultimap(),
                    res.body?.string(), res.request.url.toString())
            }
        }
    }
}
