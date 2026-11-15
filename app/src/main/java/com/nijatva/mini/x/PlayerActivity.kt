package com.nijatva.mini.x

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Plays a video in the app rather than in a page. The stream url comes from the same
 * extractor the feed uses, so a card that plays is a card that downloads.
 */
class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        root.setBackgroundColor(0xFF000000.toInt())

        val view = PlayerView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setShowNextButton(false)
            setShowPreviousButton(false)
        }
        root.addView(view)

        val status = TextView(this).apply {
            text = "Finding the stream..."
            setTextColor(0xFFCCCCCC.toInt())
            textSize = 14f
            gravity = Gravity.CENTER
        }
        root.addView(status, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER))

        val spin = ProgressBar(this)
        root.addView(spin, FrameLayout.LayoutParams(dp(36), dp(36), Gravity.CENTER).apply {
            bottomMargin = dp(60)
        })

        setContentView(root)

        val url = intent?.getStringExtra("video_url") ?: ""
        if (url.isBlank()) { status.text = "Nothing to play."; return }

        CoroutineScope(Dispatchers.Main).launch {
            val stream = withContext(Dispatchers.IO) {
                runCatching { VideoFeed.streamUrl(url) }.getOrNull()
            }
            if (stream.isNullOrBlank()) {
                status.text = "This video has no playable stream."
                spin.visibility = android.view.View.GONE
                return@launch
            }
            status.visibility = android.view.View.GONE
            spin.visibility = android.view.View.GONE
            player = ExoPlayer.Builder(this@PlayerActivity).build().also { p ->
                view.player = p
                p.setMediaItem(MediaItem.fromUri(stream))
                p.prepare()
                p.playWhenReady = true
            }
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
