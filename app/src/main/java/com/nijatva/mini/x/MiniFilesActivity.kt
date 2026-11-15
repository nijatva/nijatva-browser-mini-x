package com.nijatva.mini.x

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** My Files, with the four shelves the reference has. Each tab shows what is really there. */
class MiniFilesActivity : AppCompatActivity() {

    private val bg = 0xFF0D0D0D.toInt()
    private val ink = 0xFFF2F2F2.toInt()
    private val dim = 0xFF8A8A8A.toInt()
    private val red = 0xFFE53935.toInt()

    private lateinit var body: LinearLayout
    private var tab = "Downloads"

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
            text = "My Files"; textSize = 20f; setTextColor(ink); setTypeface(null, android.graphics.Typeface.BOLD)
        })
        root.addView(bar)

        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(dp(12), 0, dp(12), dp(8)) }
        for (t in listOf("Downloads", "Videos", "Music", "Private")) {
            val v = TextView(this).apply {
                text = t; textSize = 14f; gravity = Gravity.CENTER
                setPadding(dp(6), dp(10), dp(6), dp(10))
                setTextColor(if (t == tab) red else dim)
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

        val files = files()
        if (files.isEmpty()) {
            page.addView(emptyState())
        } else {
            for (f in files) page.addView(fileRow(f.first, f.second))
        }
    }

    /** Whatever is on disk under this app's downloads folder. */
    private fun files(): List<Pair<String, Long>> {
        return try {
            val dir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: return emptyList()
            dir.listFiles()?.filter { it.isFile }
                ?.sortedByDescending { it.lastModified() }
                ?.map { it.name to it.length() }
                ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    private fun emptyState(): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, dp(90), 0, 0)
        }
        box.addView(TextView(this).apply {
            text = "\u25A1"; textSize = 54f; setTextColor(0xFF2E2E2E.toInt()); gravity = Gravity.CENTER
        })
        box.addView(TextView(this).apply {
            text = "No download task"; textSize = 15f; setTextColor(dim); gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, 0)
        })
        return box
    }

    private fun fileRow(name: String, size: Long): LinearLayout {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(14), dp(4), dp(14))
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(TextView(this).apply { text = name; textSize = 14.5f; setTextColor(ink); maxLines = 2 })
        col.addView(TextView(this).apply {
            text = human(size); textSize = 12f; setTextColor(dim); setPadding(0, dp(3), 0, 0)
        })
        r.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        return r
    }

    private fun human(b: Long): String = when {
        b > 1_000_000 -> String.format("%.1f MB", b / 1_000_000.0)
        b > 1_000 -> String.format("%.0f KB", b / 1_000.0)
        else -> "$b B"
    }
}
