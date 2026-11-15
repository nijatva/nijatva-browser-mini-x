package com.nijatva.mini.x

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * The settings hub, in the shape of the reference: five groups and About, each with a
 * mark, a name and a line that says what is inside, so nothing needs opening to be
 * understood.
 */
class MiniSettingsActivity : AppCompatActivity() {

    private val bg = 0xFF0D0D0D.toInt()
    private val card = 0xFF1A1A1A.toInt()
    private val ink = 0xFFF2F2F2.toInt()
    private val dim = 0xFF9A9A9A.toInt()
    private val red = 0xFFE53935.toInt()

    private data class Row(val mark: String, val title: String, val sub: String?, val tap: () -> Unit)

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        root.addView(topBar("Settings"))

        val scroll = ScrollView(this)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(8), dp(14), dp(28))
        }
        scroll.addView(page)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val group = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(card, 4)
        }
        val rows = listOf(
            Row("\u25C9", "Content", "Language, Location, Restricted Mode") { go(ContentSettingsActivity::class.java) },
            Row("\u2B07", "Download", "Path, WiFi, Subtitle, etc.") { go(DownloadSettingsActivity::class.java) },
            Row("\u2691", "Push Notifications", "Daily push, Like, Comment, Follow, Message") { go(PushSettingsActivity::class.java) },
            Row("\u25A6", "Widgets", "Quick Actions, Music Player, Hot Video") { note() },
            Row("\u2699", "Others", "Player, Update, Cache") { go(SettingsActivity::class.java) }
        )
        for ((i, r) in rows.withIndex()) {
            if (i > 0) group.addView(divider())
            group.addView(row(r.mark, r.title, r.sub, r.tap))
        }
        page.addView(group)

        val about = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(card, 4)
        }
        about.addView(row("\u2139", "About Us", null) { about() })
        val alp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        alp.topMargin = dp(14)
        page.addView(about, alp)

        setContentView(root)
    }

    private fun topBar(title: String): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(0xFF1C1C1C.toInt())
            setPadding(dp(14), dp(16), dp(14), dp(16))
        }
        bar.addView(TextView(this).apply {
            text = "\u2039"
            textSize = 26f
            setTextColor(ink)
            setPadding(0, 0, dp(18), 0)
            setOnClickListener { finish() }
        })
        bar.addView(TextView(this).apply {
            text = title
            textSize = 19f
            setTextColor(ink)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        return bar
    }

    private fun row(mark: String, title: String, sub: String?, tap: () -> Unit): LinearLayout {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        r.addView(TextView(this).apply {
            text = mark
            textSize = 17f
            setTextColor(ink)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(34), ViewGroup.LayoutParams.WRAP_CONTENT))
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(TextView(this).apply {
            text = title
            textSize = 15.5f
            setTextColor(ink)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        if (sub != null) col.addView(TextView(this).apply {
            text = sub
            textSize = 12.5f
            setTextColor(dim)
            setPadding(0, dp(3), 0, 0)
        })
        r.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        r.addView(TextView(this).apply { text = "\u203A"; textSize = 20f; setTextColor(dim) })
        r.setOnClickListener { tap() }
        return r
    }

    private fun divider() = LinearLayout(this).apply {
        setBackgroundColor(0xFF2A2A2A.toInt())
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)).apply {
            leftMargin = dp(16)
        }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        cornerRadius = dp(radius).toFloat()
        setColor(color)
    }

    private fun go(cls: Class<*>) = startActivity(Intent(this, cls))

    private fun note() = android.widget.Toast.makeText(this,
        "Widgets would need a home-screen host this build does not ship.", android.widget.Toast.LENGTH_LONG).show()

    private fun about() = android.app.AlertDialog.Builder(this)
        .setTitle("Mini X")
        .setMessage("A privacy-first Android browser on the system WebView.\n\n" +
            "No accounts, no ads, no analytics, no Google Play Services.\n" +
            "Package: com.nijatva.mini.x\n\n" +
            "Extensions come from the same signed catalogue as Nijatva.")
        .setPositiveButton(android.R.string.ok, null)
        .show()
}
