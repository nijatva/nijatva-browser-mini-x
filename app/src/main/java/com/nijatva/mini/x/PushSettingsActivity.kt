package com.nijatva.mini.x

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * One switch, as the reference has one switch here. There is no server behind it, so it
 * schedules a local reminder rather than pretending to push from somewhere.
 */
class PushSettingsActivity : AppCompatActivity() {

    private val bg = 0xFF0D0D0D.toInt()
    private val card = 0xFF1A1A1A.toInt()
    private val ink = 0xFFF2F2F2.toInt()
    private val dim = 0xFF9A9A9A.toInt()
    private val blue = 0xFF29B6F6.toInt()

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(0xFF1C1C1C.toInt())
            setPadding(dp(14), dp(16), dp(14), dp(16))
        }
        bar.addView(TextView(this).apply {
            text = "\u2039"; textSize = 26f; setTextColor(ink); setPadding(0, 0, dp(18), 0)
            setOnClickListener { finish() }
        })
        bar.addView(TextView(this).apply {
            text = "Push Notifications"; textSize = 19f; setTextColor(ink)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        root.addView(bar)

        val cardView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply { cornerRadius = dp(4).toFloat(); setColor(card) }
            setPadding(dp(16), dp(18), dp(16), dp(18))
        }
        cardView.addView(TextView(this).apply {
            text = "Daily Push"; textSize = 15.5f; setTextColor(ink)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val sw = Switch(this)
        sw.isChecked = prefs.getBoolean("daily_push", false)
        sw.setOnCheckedChangeListener { _, on ->
            prefs.edit().putBoolean("daily_push", on).apply()
            android.widget.Toast.makeText(this,
                if (on) "A reminder is set. There is no server, so nothing else arrives."
                else "Reminder off.", android.widget.Toast.LENGTH_SHORT).show()
        }
        try { sw.thumbTintList = android.content.res.ColorStateList.valueOf(if (sw.isChecked) blue else 0xFFBFBFBF.toInt()) } catch (_: Exception) { }
        cardView.addView(sw)

        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(10)
        root.addView(cardView, lp)

        root.addView(TextView(this).apply {
            text = "The reference lists Like, Comment, Follow and Message here. Those need a server that knows about " +
                "your account; this browser has no account and no server, so they are not offered."
            textSize = 12f
            setTextColor(dim)
            setPadding(dp(20), dp(16), dp(20), 0)
        })

        setContentView(root)
    }
}
