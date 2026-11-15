package com.nijatva.mini.x

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Widget settings, in the reference's shape: one card per widget, a sketch of what it
 * would look like, and an Add button. The Add button is honest - this build ships no
 * AppWidgetProvider, so it explains that rather than failing silently.
 */
class WidgetSettingsActivity : AppCompatActivity() {

    private val bg = 0xFF0D0D0D.toInt()
    private val card = 0xFF1A1A1A.toInt()
    private val ink = 0xFFF2F2F2.toInt()
    private val dim = 0xFF9A9A9A.toInt()
    private val red = 0xFFE53935.toInt()

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        root.addView(bar("Widget Settings"))

        val scroll = ScrollView(this)
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(28)) }
        scroll.addView(page)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        page.addView(widgetCard("Quick Actions",
            "One-click access to the sites you use, from the home screen",
            listOf("Hot Video", "Music", "Game", "Site")))
        page.addView(widgetCard("Music Player",
            "Playback controls and the track that is playing",
            listOf("\u23EE", "\u25B6", "\u23ED")))
        page.addView(widgetCard("Hot Video",
            "A few recent pages, side by side",
            listOf("\u25A3", "\u25A3", "\u25A3")))

        setContentView(root)
    }

    private fun widgetCard(title: String, sub: String, marks: List<String>): LinearLayout {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply { cornerRadius = dp(14).toFloat(); setColor(card) }
            setPadding(dp(16), dp(18), dp(16), dp(16))
        }

        // a sketch of the widget: the marks it would show, on a darker plate
        val plate = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(0xFF101010.toInt())
                setStroke(dp(1), 0xFF2E2E2E.toInt())
            }
            setPadding(dp(14), dp(18), dp(14), dp(18))
        }
        for (m in marks) {
            plate.addView(TextView(this).apply {
                text = m
                textSize = 13f
                setTextColor(ink)
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    cornerRadius = dp(16).toFloat()
                    setColor(0xFF1F1F1F.toInt())
                }
                setPadding(dp(12), dp(8), dp(12), dp(8))
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                rightMargin = dp(8)
            })
        }
        outer.addView(plate, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(18), 0, 0)
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(TextView(this).apply {
            text = title; textSize = 16f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        })
        col.addView(TextView(this).apply {
            text = sub; textSize = 12.5f; setTextColor(dim); setPadding(0, dp(4), 0, 0)
        })
        row.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        row.addView(TextView(this).apply {
            text = "Add"
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply { cornerRadius = dp(8).toFloat(); setColor(red) }
            setPadding(dp(22), dp(10), dp(22), dp(10))
            setOnClickListener {
                android.widget.Toast.makeText(this@WidgetSettingsActivity,
                    "This build ships no home-screen widget provider yet, so there is nothing to place.",
                    android.widget.Toast.LENGTH_LONG).show()
            }
        })
        outer.addView(row)

        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = dp(12)
        outer.layoutParams = lp
        return outer
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
}
