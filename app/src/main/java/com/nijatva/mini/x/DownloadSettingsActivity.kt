package com.nijatva.mini.x

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Download settings. Everything here changes what the browser actually does with a
 * download; nothing is decoration. "Smart Muxer" and the subtitle picker from the
 * reference are left out rather than faked - this build has no muxer to tune and no
 * per-video subtitle track to choose.
 */
class DownloadSettingsActivity : AppCompatActivity() {

    private val bg = 0xFF0D0D0D.toInt()
    private val card = 0xFF1A1A1A.toInt()
    private val ink = 0xFFF2F2F2.toInt()
    private val dim = 0xFF9A9A9A.toInt()
    private val blue = 0xFF29B6F6.toInt()

    private lateinit var prefs: android.content.SharedPreferences
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        root.addView(bar("Download Settings"))

        val scroll = ScrollView(this)
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(10), dp(14), dp(28)) }
        scroll.addView(page)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val first = block()
        first.addView(twoLine("Download path", path()))
        first.addView(divider())
        first.addView(toggle("Download only on WiFi", "Queue waits for a network it does not have to pay for", "dl_wifi", false))
        first.addView(divider())
        first.addView(pick("Max download tasks", "" + prefs.getInt("dl_tasks", 2), listOf("1", "2", "3", "4"), "dl_tasks"))
        first.addView(divider())
        first.addView(pick("Auto resume", prefs.getString("dl_resume", "Always") ?: "Always",
            listOf("Always", "WiFi only", "Never"), "dl_resume"))
        page.addView(first)

        val second = block()
        second.addView(toggle("Copied link detection", "Offer a download when a link is copied", "dl_clip", true))
        second.addView(divider())
        second.addView(toggle("Download complete notification", "A notification when a file finishes", "dl_notify", true))
        second.addView(divider())
        second.addView(toggle("Delete APK after install", "Remove the file once the installer has run", "dl_del_apk", true))
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(12)
        page.addView(second, lp)

        page.addView(TextView(this).apply {
            text = "Path, WiFi-only and auto-resume are read by the downloader. Task count is a limit on how many " +
                "run at once."
            textSize = 12f
            setTextColor(dim)
            setPadding(dp(6), dp(14), dp(6), 0)
        })

        setContentView(root)
    }

    private fun path(): String {
        val base = try { getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) } catch (_: Exception) { null }
        return base?.absolutePath ?: "/storage/emulated/0/Download"
    }

    private fun bar(title: String): LinearLayout {
        val b = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(0xFF1C1C1C.toInt())
            setPadding(dp(14), dp(16), dp(14), dp(16))
        }
        b.addView(TextView(this).apply {
            text = "\u2039"; textSize = 26f; setTextColor(ink); setPadding(0, 0, dp(18), 0)
            setOnClickListener { finish() }
        })
        b.addView(TextView(this).apply {
            text = title; textSize = 19f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        })
        return b
    }

    private fun block() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = GradientDrawable().apply { cornerRadius = dp(4).toFloat(); setColor(card) }
    }

    private fun divider() = LinearLayout(this).apply {
        setBackgroundColor(0xFF2A2A2A.toInt())
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1))
    }

    private fun twoLine(title: String, value: String): LinearLayout {
        val r = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)) }
        r.addView(TextView(this).apply {
            text = title; textSize = 15.5f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        })
        r.addView(TextView(this).apply {
            text = value; textSize = 12.5f; setTextColor(dim); setPadding(0, dp(3), 0, 0)
        })
        return r
    }

    private fun toggle(title: String, sub: String, key: String, def: Boolean): LinearLayout {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(TextView(this).apply {
            text = title; textSize = 15.5f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        })
        col.addView(TextView(this).apply {
            text = sub; textSize = 12.5f; setTextColor(dim); setPadding(0, dp(3), 0, 0)
        })
        r.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val sw = Switch(this)
        sw.isChecked = prefs.getBoolean(key, def)
        sw.setOnCheckedChangeListener { _, on -> prefs.edit().putBoolean(key, on).apply() }
        try { sw.thumbTintList = android.content.res.ColorStateList.valueOf(if (sw.isChecked) blue else 0xFFBFBFBF.toInt()) } catch (_: Exception) { }
        r.addView(sw)
        return r
    }

    private fun pick(title: String, current: String, options: List<String>, key: String): LinearLayout {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        r.addView(TextView(this).apply {
            text = title; textSize = 15.5f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val v = TextView(this).apply { text = current; textSize = 13.5f; setTextColor(dim) }
        r.addView(v)
        r.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(options.toTypedArray()) { _, which ->
                    val choice = options[which]
                    v.text = choice
                    if (key == "dl_tasks") prefs.edit().putInt(key, choice.toIntOrNull() ?: 2).apply()
                    else prefs.edit().putString(key, choice).apply()
                }
                .show()
        }
        return r
    }
}
