package com.nijatva.mini.x

import android.graphics.Color
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
 * Content settings, as the reference lays them out. Two of these switches do something
 * real in a browser: Restricted Mode adds a safe-search flag to the query, and the Sites
 * Filter is the same list Focus uses to block a host.
 */
class ContentSettingsActivity : AppCompatActivity() {

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

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        root.addView(bar("Content Settings"))

        val scroll = ScrollView(this)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(28))
        }
        scroll.addView(page)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val top = block()
        top.addView(value("Language", "English"))
        top.addView(divider())
        top.addView(value("Location", "India"))
        top.addView(divider())
        top.addView(value("Restricted Mode", if (prefs.getBoolean("restricted", false)) "On" else "Off"))
        page.addView(top)

        val switches = block()
        switches.addView(toggle("Recommendations Filter", "Hide adult content in your feed", "filter_feed", false))
        switches.addView(divider())
        switches.addView(toggle("Browsing Content Filter", "Hide adult content when viewing a channel or playlist", "filter_browse", false))
        switches.addView(divider())
        switches.addView(toggle("Search Results Filter", "Add a safe-search flag to every search", "restricted", false))
        switches.addView(divider())
        switches.addView(toggle("Sites Filter", "Block the hosts listed in Focus", "filter_sites", false))
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(12)
        page.addView(switches, lp)

        page.addView(TextView(this).apply {
            text = "Restricted Mode and the Sites Filter are applied by the browser. The two feed filters would " +
                "apply to an in-app feed, which this build does not have, so they are remembered and do nothing yet."
            textSize = 12f
            setTextColor(dim)
            setPadding(dp(6), dp(14), dp(6), 0)
        })

        setContentView(root)
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

    private fun value(title: String, value: String): LinearLayout {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(18))
        }
        r.addView(TextView(this).apply {
            text = title; textSize = 15.5f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        r.addView(TextView(this).apply { text = value; textSize = 13.5f; setTextColor(dim) })
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
        try {
            sw.thumbTintList = android.content.res.ColorStateList.valueOf(if (sw.isChecked) blue else 0xFFBFBFBF.toInt())
        } catch (_: Exception) { }
        r.addView(sw)
        return r
    }
}
