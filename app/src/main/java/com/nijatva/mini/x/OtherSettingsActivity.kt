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
 * Other settings, as the reference lays them out. The quality picker, the two play-with
 * rows and the switches all read something the browser already knows; the two that would
 * need a player this build does not ship say so instead of pretending.
 */
class OtherSettingsActivity : AppCompatActivity() {

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
        root.addView(bar("Other Settings"))

        val scroll = ScrollView(this)
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(10), dp(14), dp(28)) }
        scroll.addView(page)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val first = block()
        first.addView(pick("Online Video Quality", prefs.getString("video_quality", "360p") ?: "360p",
            listOf("144p", "240p", "360p", "480p", "720p", "1080p"), "video_quality"))
        first.addView(divider())
        first.addView(twoLine("Play Videos with", "Built-in player", "The browser plays video in the page it came from."))
        first.addView(divider())
        first.addView(twoLine("Play Audio with", "Built-in player", "No external player is bundled, and none is required."))
        first.addView(divider())
        first.addView(toggle("Auto Play Videos", "Videos play automatically on the page", "autoplay_videos", false))
        first.addView(divider())
        first.addView(toggle("Auto Pop-up Play Videos", "Picture-in-picture when leaving a video", "autoplay_popup", false))
        first.addView(divider())
        first.addView(toggle("Quick Search", "Search from the address bar as you type", "quick_search", true))
        page.addView(first)

        val second = block()
        second.addView(navRow("Adjust Brightness", "Adjust day and night brightness") {
            android.widget.Toast.makeText(this, "Use the screen filter in Appearance for this.",
                android.widget.Toast.LENGTH_SHORT).show()
        })
        second.addView(divider())
        second.addView(navRow("Reset Private Password", null) {
            prefs.edit().remove("private_password").apply()
            android.widget.Toast.makeText(this, "Private password cleared.", android.widget.Toast.LENGTH_SHORT).show()
        })
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(12)
        page.addView(second, lp)

        val third = block()
        third.addView(navRow("Clear Cache", null) { clear() })
        val lp2 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp2.topMargin = dp(12)
        page.addView(third, lp2)

        setContentView(root)
    }

    private fun clear() {
        try {
            android.webkit.WebStorage.getInstance().deleteAllData()
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            android.widget.Toast.makeText(this, "Cache and site data cleared.", android.widget.Toast.LENGTH_SHORT).show()
        } catch (_: Exception) { }
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

    private fun twoLine(title: String, value: String, sub: String): LinearLayout {
        val r = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)) }
        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(TextView(this).apply {
            text = title; textSize = 15.5f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        head.addView(TextView(this).apply { text = value; textSize = 13.5f; setTextColor(dim) })
        r.addView(head)
        r.addView(TextView(this).apply {
            text = sub; textSize = 12.5f; setTextColor(dim); setPadding(0, dp(3), 0, 0)
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

    private fun navRow(title: String, sub: String?, tap: () -> Unit): LinearLayout {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(TextView(this).apply {
            text = title; textSize = 15.5f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        })
        if (sub != null) col.addView(TextView(this).apply {
            text = sub; textSize = 12.5f; setTextColor(dim); setPadding(0, dp(3), 0, 0)
        })
        r.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        r.setOnClickListener { tap() }
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
            android.app.AlertDialog.Builder(this).setTitle(title)
                .setItems(options.toTypedArray()) { _, which ->
                    v.text = options[which]
                    prefs.edit().putString(key, options[which]).apply()
                }.show()
        }
        return r
    }
}
