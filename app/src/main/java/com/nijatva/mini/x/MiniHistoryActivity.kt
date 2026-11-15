package com.nijatva.mini.x

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** History, split the way the reference splits it: what this app opened, and the two sites. */
class MiniHistoryActivity : AppCompatActivity() {

    private val bg = 0xFF0D0D0D.toInt()
    private val ink = 0xFFF2F2F2.toInt()
    private val dim = 0xFF8A8A8A.toInt()
    private val red = 0xFFE53935.toInt()

    private lateinit var body: LinearLayout
    private var tab = "Local"

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
            text = "History"; textSize = 20f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        })
        root.addView(bar)

        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(dp(12), 0, dp(12), dp(6)) }
        for (t in listOf("Local", "YouTube", "Website")) {
            val v = TextView(this).apply {
                text = t; textSize = 14f; gravity = Gravity.CENTER
                setPadding(dp(6), dp(10), dp(6), dp(10))
                setTextColor(if (t == tab) ink else dim)
                setTypeface(null, if (t == tab) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                setOnClickListener { tab = t; rebuild() }
            }
            tabs.addView(v, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        root.addView(tabs)

        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this)
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)
        rebuild()
    }

    private fun rebuild() {
        body.removeAllViews()
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(24))
        }
        body.addView(page)

        if (tab == "Local") {
            page.addView(TextView(this).apply {
                text = "Local history"; textSize = 15f; setTextColor(ink)
                setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, dp(6), 0, dp(10))
            })
            val rows = recent()
            if (rows.isEmpty()) page.addView(emptyState("No Local History"))
            else for (r in rows) page.addView(row(r.first, r.second))
        } else {
            val label = if (tab == "YouTube") "YouTube History" else "Website History"
            val rows = recent().filter {
                if (tab == "YouTube") it.second.contains("youtu") else !it.second.contains("youtu")
            }
            page.addView(navRow(label))
            if (rows.isEmpty()) page.addView(emptyState("No ${tab} History"))
            else for (r in rows) page.addView(row(r.first, r.second))
        }

        val prompt = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(26), dp(4), 0)
        }
        prompt.addView(TextView(this).apply {
            text = "Sign in to keep a history across devices"; textSize = 13f; setTextColor(dim)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        prompt.addView(TextView(this).apply {
            text = "Sign in"; textSize = 13.5f; setTextColor(red)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setOnClickListener {
                android.widget.Toast.makeText(this@MiniHistoryActivity,
                    "Signing in needs a server this browser does not have.", android.widget.Toast.LENGTH_LONG).show()
            }
        })
        page.addView(prompt)
    }

    private fun recent(): List<Pair<String, String>> = try {
        val prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
        prefs.getString("history", "")?.split("\n")?.asReversed()?.mapNotNull { line ->
            if (line.isBlank()) return@mapNotNull null
            val p = line.split('\u0001')
            val t = p.getOrElse(0) { "" }
            val u = p.getOrElse(1) { "" }
            if (u.isBlank()) null else t to u
        }?.take(40) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    private fun emptyState(text: String): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(0, dp(70), 0, 0)
        }
        box.addView(TextView(this).apply {
            text = "\u25A1"; textSize = 54f; setTextColor(0xFF2E2E2E.toInt()); gravity = Gravity.CENTER
        })
        box.addView(TextView(this).apply {
            text = text; textSize = 15f; setTextColor(dim); gravity = Gravity.CENTER; setPadding(0, dp(14), 0, 0)
        })
        return box
    }

    private fun row(title: String, url: String): LinearLayout {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(4), dp(12), dp(4), dp(12))
        }
        r.addView(TextView(this).apply { text = title.ifBlank { url }; textSize = 14.5f; setTextColor(ink); maxLines = 1 })
        r.addView(TextView(this).apply {
            text = url; textSize = 12f; setTextColor(dim); maxLines = 1; setPadding(0, dp(3), 0, 0)
        })
        r.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).putExtra("open_url", url))
        }
        return r
    }

    private fun navRow(title: String): LinearLayout {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(14), dp(4), dp(14))
        }
        r.addView(TextView(this).apply { text = title; textSize = 15f; setTextColor(ink) },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        r.addView(TextView(this).apply { text = "\u203A"; textSize = 18f; setTextColor(dim) })
        return r
    }
}
