package com.nijatva.mini.x

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Channels followed on this device, and what they posted last. */
class SubscriptionsActivity : AppCompatActivity() {

    private val bg = 0xFF0D0D0D.toInt()
    private val ink = 0xFFF2F2F2.toInt()
    private val dim = 0xFF8A8A8A.toInt()
    private val red = 0xFFE53935.toInt()

    private lateinit var body: LinearLayout
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
            text = "Subscriptions"; textSize = 20f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        })
        root.addView(bar)

        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), 0, dp(16), dp(24)) }
        val scroll = ScrollView(this)
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)
        rebuild()
    }

    private fun rebuild() {
        body.removeAllViews()
        val channels = Subs.all(this)
        if (channels.isEmpty()) {
            body.addView(TextView(this).apply {
                text = "No channels yet.\nTap \u2606 on a video to follow its channel."
                setTextColor(dim); textSize = 14f; setPadding(0, dp(60), 0, 0); gravity = Gravity.CENTER
            })
            return
        }
        for (c in channels) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(14), 0, dp(14))
            }
            row.addView(TextView(this).apply {
                text = c.name; textSize = 15f; setTextColor(ink)
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(TextView(this).apply {
                text = "Videos"; textSize = 13f; setTextColor(red)
                setPadding(0, 0, dp(14), 0)
                setOnClickListener { load(c) }
            })
            row.addView(TextView(this).apply {
                text = "\u2715"; textSize = 14f; setTextColor(dim)
                setOnClickListener { Subs.remove(this@SubscriptionsActivity, c.url); rebuild() }
            })
            body.addView(row)
        }
    }

    private fun load(c: Subs.Channel) {
        body.removeAllViews()
        val head = TextView(this).apply {
            text = c.name; textSize = 16f; setTextColor(ink)
            setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, dp(10), 0, dp(8))
        }
        body.addView(head)
        val note = TextView(this).apply {
            text = "Loading..."; textSize = 13f; setTextColor(dim)
        }
        body.addView(note)

        CoroutineScope(Dispatchers.Main).launch {
            val items = withContext(Dispatchers.IO) {
                runCatching { VideoFeed.channelVideos(c.url, 15) }.getOrDefault(emptyList())
            }
            note.visibility = android.view.View.GONE
            if (items.isEmpty()) {
                body.addView(TextView(this@SubscriptionsActivity).apply {
                    text = "Could not read this channel."; setTextColor(dim); textSize = 13f
                })
                return@launch
            }
            for (v in items) body.addView(videoRow(v))
        }
    }

    private fun videoRow(v: VideoFeed.Card): LinearLayout {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, dp(12))
        }
        r.addView(TextView(this).apply { text = v.title; textSize = 14.5f; setTextColor(ink); maxLines = 2 })
        val meta = listOf(VideoFeed.humanDuration(v.duration), VideoFeed.humanViews(v.views))
            .filter { it.isNotBlank() }.joinToString("  \u00B7  ")
        r.addView(TextView(this).apply { text = meta; textSize = 12f; setTextColor(dim) })
        r.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java).putExtra("video_url", v.url))
        }
        return r
    }
}
