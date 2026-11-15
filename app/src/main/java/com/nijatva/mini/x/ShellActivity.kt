package com.nijatva.mini.x

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.graphics.Bitmap
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The shell the app opens into: four tabs, a search that goes to the browser, and the
 * places people actually go - files, history, settings. Light and red, like the
 * reference; the browser itself keeps its own theme once it is opened.
 */
class ShellActivity : AppCompatActivity() {

    private val bg = 0xFFF4F5F7.toInt()
    private val card = Color.WHITE
    private val ink = 0xFF1A1A1A.toInt()
    private val dim = 0xFF8A8A8A.toInt()
    private val red = 0xFFE53935.toInt()

    private lateinit var body: LinearLayout
    private lateinit var nav: LinearLayout
    private var tab = "home"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val frame = FrameLayout(this)
        root.addView(frame, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        frame.addView(body, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        nav = buildNav()
        root.addView(nav)
        setContentView(root)
        show("home")
    }

    // ---------------------------------------------------------------- chrome

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun pill(bgColor: Int, radius: Int = 26): GradientDrawable =
        GradientDrawable().apply { cornerRadius = dp(radius).toFloat(); setColor(bgColor) }

    private fun label(t: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = t
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
    }

    private fun buildNav(): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(8), dp(8), dp(8), dp(10))
            elevation = dp(8).toFloat()
        }
        for ((key, title) in listOf("home" to "HOME", "music" to "MUSIC", "files" to "MY FILES", "me" to "ME")) {
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(0, dp(6), 0, dp(2))
            }
            val mark = label(if (key == "home") "\u2302" else if (key == "files") "\u2B07" else if (key == "music") "\u266B" else "\u25CF", 20f, dim)
            mark.gravity = Gravity.CENTER
            val t = label(title, 10f, dim, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(3), 0, 0) }
            col.addView(mark)
            col.addView(t)
            col.setOnClickListener { show(key) }
            bar.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            col.tag = key
        }
        return bar
    }

    private fun paintNav() {
        for (i in 0 until nav.childCount) {
            val col = nav.getChildAt(i) as LinearLayout
            val on = col.tag == tab
            (col.getChildAt(0) as TextView).setTextColor(if (on) red else dim)
            (col.getChildAt(1) as TextView).setTextColor(if (on) red else dim)
        }
    }

    private fun show(which: String) {
        tab = which
        paintNav()
        body.removeAllViews()
        val scroll = ScrollView(this)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(24))
        }
        scroll.addView(page)
        body.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        when (which) {
            "home" -> home(page)
            "music" -> music(page)
            "files" -> { startActivity(Intent(this, MiniFilesActivity::class.java)); show("home"); return }
            else -> me(page)
        }
    }

    // ---------------------------------------------------------------- home

    private fun searchBar(page: LinearLayout, placeholder: String, onGo: (String) -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = pill(Color.WHITE)
            setPadding(dp(16), dp(4), dp(8), dp(4))
            elevation = dp(1).toFloat()
        }
        val q = EditText(this).apply {
            hint = placeholder
            textSize = 15f
            setTextColor(ink)
            setHintTextColor(dim)
            setBackgroundColor(Color.TRANSPARENT)
            setSingleLine(true)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(q)
        val go = label("\u2192", 18f, red, true).apply {
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setOnClickListener {
                val text = q.text.toString().trim()
                if (text.isNotEmpty()) onGo(text)
            }
        }
        row.addView(go)
        page.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)))
    }

    private fun chips(page: LinearLayout) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(12), 0, 0) }
        for ((i, t) in listOf("Shorts", "All").withIndex()) {
            val chip = label(t, 14f, if (i == 0) red else Color.WHITE, true).apply {
                setPadding(dp(18), dp(9), dp(18), dp(9))
                background = pill(if (i == 0) Color.WHITE else 0xFF2B2B2B.toInt())
                setTextColor(if (i == 0) red else Color.WHITE)
            }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.rightMargin = dp(8)
            row.addView(chip, lp)
        }
        page.addView(row)
    }

    private fun sites(page: LinearLayout) {
        val row = HorizontalScrollView(this)
        val strip = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(18), 0, dp(6)) }
        val sites = listOf(
            "YouTube" to "https://m.youtube.com",
            "Instagram" to "https://www.instagram.com",
            "Facebook" to "https://m.facebook.com",
            "Google" to "https://www.google.com",
            "Sites" to ""
        )
        val colors = listOf(0xFFFF0000.toInt(), 0xFFE1306C.toInt(), 0xFF1877F2.toInt(),
            0xFF4285F4.toInt(), 0xFF7E57C2.toInt())
        for ((i, s) in sites.withIndex()) {
            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
            val mark = label(s.first.substring(0, 1), 18f, Color.WHITE, true).apply {
                gravity = Gravity.CENTER
                background = pill(colors[i], 14)
            }
            col.addView(mark, LinearLayout.LayoutParams(dp(52), dp(52)))
            col.addView(label(s.first, 11f, dim).apply { gravity = Gravity.CENTER; setPadding(0, dp(6), 0, 0) })
            col.setOnClickListener {
                if (s.second.isNotBlank()) open(s.second) else open("https://vasudev-work.github.io/nijatva-web-store/")
            }
            val lp = LinearLayout.LayoutParams(dp(72), ViewGroup.LayoutParams.WRAP_CONTENT)
            strip.addView(col, lp)
        }
        row.addView(strip)
        page.addView(row)
    }

    private fun home(page: LinearLayout) {
        searchBar(page, "Search or enter url") { open(if (it.contains(" ") || !it.contains(".")) searchUrl(it) else it) }
        chips(page)
        sites(page)

        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(18), 0, dp(10))
        }
        head.addView(label("Videos", 17f, ink, true), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        head.addView(label("My Files", 13f, red).apply {
            setOnClickListener { startActivity(Intent(this@ShellActivity, MiniFilesActivity::class.java)) }
        })
        page.addView(head)

        val holder = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        page.addView(holder)
        val spinner = label("Loading videos...", 13f, dim).apply { setPadding(0, dp(20), 0, dp(20)) }
        holder.addView(spinner)

        CoroutineScope(Dispatchers.Main).launch {
            val cards = withContext(Dispatchers.IO) {
                runCatching { VideoFeed.search("trending videos", 12) }.getOrDefault(emptyList())
            }
            holder.removeAllViews()
            if (cards.isEmpty()) {
                holder.addView(label("Could not load the feed. Pull a link instead.", 13f, dim).apply {
                    setPadding(0, dp(16), 0, dp(10))
                })
                for (r in recentPages().take(6)) holder.addView(recentCard(r.first, r.second))
                return@launch
            }
            for (c in cards) {
                val card = feedCard(c)
                holder.addView(card)
                CoroutineScope(Dispatchers.Main).launch {
                    val bmp = withContext(Dispatchers.IO) { VideoFeed.thumb(c.thumb) }
                    if (bmp != null) card.findViewWithTag<ImageView>("thumb")?.setImageBitmap(bmp)
                }
            }
        }
    }

    /** One video: a picture, how long it runs, who made it, and a way to keep it. */
    private fun feedCard(c: VideoFeed.Card): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = pill(Color.WHITE, 14)
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }

        val frame = FrameLayout(this)
        val pic = ImageView(this).apply {
            tag = "thumb"
            setBackgroundColor(0xFFE6E6E6.toInt())
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        frame.addView(pic, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(180)))
        if (c.duration > 0) {
            val badge = label(VideoFeed.humanDuration(c.duration), 11f, Color.WHITE, true).apply {
                setPadding(dp(6), dp(2), dp(6), dp(2))
                background = pill(0xCC000000.toInt(), 6)
            }
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.END)
            lp.setMargins(0, 0, dp(8), dp(8))
            frame.addView(badge, lp)
        }
        card.addView(frame)

        card.addView(label(c.title, 14.5f, ink, true).apply {
            maxLines = 2
            setPadding(0, dp(10), 0, dp(2))
        })

        val meta = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val views = VideoFeed.humanViews(c.views)
        val line = listOf(c.uploader, views).filter { it.isNotBlank() }.joinToString("  ·  ")
        meta.addView(label(line, 12f, dim).apply { maxLines = 1 },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        meta.addView(label("⬇ Download", 12.5f, red, true).apply {
            setPadding(dp(10), dp(6), dp(4), dp(6))
            setOnClickListener {
                startActivity(Intent(this@ShellActivity, VideoDownloadActivity::class.java)
                    .putExtra("video_url", c.url))
            }
        })
        card.addView(meta)

        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = dp(10)
        card.layoutParams = lp
        return card
    }

    private fun recentCard(title: String, url: String): LinearLayout {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = pill(card, 16)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(label(title.ifBlank { url }, 14.5f, ink, true).apply { maxLines = 2 })
        col.addView(label(hostOf(url), 12f, dim).apply { setPadding(0, dp(3), 0, 0) })
        c.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        c.addView(label("\u25B6", 16f, red, true).apply { setPadding(dp(10), 0, 0, 0) })
        c.setOnClickListener { open(url) }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = dp(10)
        c.layoutParams = lp
        return c
    }

    // ---------------------------------------------------------------- music

    private fun music(page: LinearLayout) {
        page.addView(label("Music", 26f, ink, true).apply { setPadding(0, 0, 0, dp(12)) })
        searchBar(page, "Search Music") { open(searchUrl(it)) }

        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(14), 0, dp(4)) }
        for ((i, t) in listOf("Music", "New releases", "Charts", "Moods & genres").withIndex()) {
            val v = label(t, 13f, if (i == 0) ink else dim, i == 0).apply {
                setPadding(0, dp(8), dp(14), dp(8))
            }
            v.setOnClickListener { moods(page, t) }
            tabs.addView(v)
        }
        page.addView(tabs)
        moods(page, "Music")
    }

    /** The shelves: a mood or a genre opens a search for it, which is the one honest thing a feed-less build can do. */
    private fun moods(page: LinearLayout, which: String) {
        val old = page.findViewWithTag<LinearLayout>("moods")
        if (old != null) page.removeView(old)

        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            tag = "moods"
        }
        val head = label(if (which == "Moods & genres") "Moods & genres" else which, 15f, ink, true)
        head.setPadding(0, dp(16), 0, dp(10))
        wrap.addView(head)

        val items = if (which == "Moods & genres")
            listOf("Chill", "Commute", "Diwali", "Energize", "Feel good", "Focus",
                   "Gaming", "Party", "Romance", "Sad", "Sleep", "Workout")
        else listOf("Quick picks", "India's biggest hits", "Romance right now", "New releases")

        var row: LinearLayout? = null
        for ((i, m) in items.withIndex()) {
            if (i % 3 == 0) {
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                wrap.addView(row)
            }
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = pill(0xFF1C1C1C.toInt(), 14)
                setPadding(dp(8), dp(18), dp(8), dp(18))
            }
            card.addView(label("\u266B", 18f, red, true).apply { gravity = Gravity.CENTER })
            card.addView(label(m, 12.5f, ink).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
            card.setOnClickListener { open(searchUrl("$m songs")) }
            val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            lp.setMargins(dp(4), dp(4), dp(4), dp(4))
            row?.addView(card, lp)
        }
        page.addView(wrap)
    }

    private fun game(page: LinearLayout) {
        page.addView(label("Game", 26f, ink, true).apply { setPadding(0, 0, 0, dp(12)) })
        page.addView(emptyBox("Nothing installed", "This tab is for games the browser can host. Nothing is here yet."))
    }

    // ---------------------------------------------------------------- me

    private fun me(page: LinearLayout) {
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = pill(card, 18)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        top.addView(label("\uD83D\uDC64  Sign in", 15f, ink, true).apply {
            setPadding(dp(18), dp(10), dp(18), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(24).toFloat()
                setStroke(dp(1), ink)
                setColor(Color.TRANSPARENT)
            }
            setOnClickListener { toastNote("Signing in is for the store, not for browsing.") }
        })
        top.addView(LinearLayout(this).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) })
        page.addView(top)

        val actions = listOf(
            Triple("\u2B07", "My Files", Runnable { startActivity(Intent(this@ShellActivity, MiniFilesActivity::class.java)) }),
            Triple("\u25F4", "History", Runnable { startActivity(Intent(this@ShellActivity, MiniHistoryActivity::class.java)) }),
            Triple("\u2630", "Bookmarks", Runnable { startActivity(Intent(this@ShellActivity, BookmarksActivity::class.java)) }),
            Triple("\u2699", "Settings", Runnable { startActivity(Intent(this@ShellActivity, MiniSettingsActivity::class.java)) })
        )
        page.addView(grid(actions))

        val more = listOf(
            Triple("\u2B07", "Download video", Runnable { startActivity(Intent(this@ShellActivity, VideoDownloadActivity::class.java)) }),
            Triple("\u21F1", "Share", Runnable { share() }),
            Triple("\u2605", "Like us", Runnable { open("https://github.com/vasudev-work/nijatva-browser-mini-x") }),
            Triple("\u21BB", "Update", Runnable { open("https://github.com/vasudev-work/nijatva-browser-mini-x/releases") }),
            Triple("\u2709", "Feedback", Runnable { mail() }),
            Triple("\u003F", "Help", Runnable { open("https://vasudev-work.github.io/nijatva-web-store/") }),
            Triple("\u2699", "Settings", Runnable { startActivity(Intent(this@ShellActivity, MiniSettingsActivity::class.java)) }),
            Triple("\u2327", "Junk Clean", Runnable { clean() }),
            Triple("\u26A1", "Boost", Runnable { toastNote("Nothing to boost - the browser keeps no background work.") })
        )
        page.addView(grid(more))
    }

    private fun grid(items: List<Triple<String, String, Runnable>>): LinearLayout {
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = pill(card, 18)
            setPadding(dp(8), dp(14), dp(8), dp(6))
        }
        var row: LinearLayout? = null
        for ((i, item) in items.withIndex()) {
            if (i % 4 == 0) {
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                wrap.addView(row)
            }
            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
            col.addView(label(item.first, 19f, ink).apply { gravity = Gravity.CENTER })
            col.addView(label(item.second, 11.5f, ink).apply { gravity = Gravity.CENTER; setPadding(0, dp(6), 0, 0) })
            col.setOnClickListener { item.third.run() }
            val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            lp.bottomMargin = dp(14)
            row?.addView(col, lp)
        }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(12)
        wrap.layoutParams = lp
        return wrap
    }

    // ---------------------------------------------------------------- bits

    private fun emptyBox(title: String, sub: String): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(40), dp(16), dp(40))
        }
        box.addView(label("\u25A1", 44f, 0xFFD0D0D0.toInt()).apply { gravity = Gravity.CENTER })
        box.addView(label(title, 15f, ink, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(12), 0, 0) })
        box.addView(label(sub, 12.5f, dim).apply { gravity = Gravity.CENTER; setPadding(0, dp(6), 0, 0) })
        return box
    }

    /** The last few pages, in the order the browser already keeps them. */
    private fun recentPages(): List<Pair<String, String>> {
        return try {
            val prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
            prefs.getString("history", "")
                ?.split("\n")
                ?.asReversed()
                ?.mapNotNull { line ->
                    if (line.isBlank()) return@mapNotNull null
                    val p = line.split('\u0001')
                    val title = p.getOrElse(0) { "" }
                    val url = p.getOrElse(1) { "" }
                    if (url.isBlank()) null else title to url
                }
                ?.distinctBy { it.second }
                ?.take(8)
                ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    private fun hostOf(url: String) = try {
        android.net.Uri.parse(url).host?.removePrefix("www.") ?: url
    } catch (_: Exception) { url }

    private fun searchUrl(q: String) = "https://duckduckgo.com/?q=" + android.net.Uri.encode(q)

    private fun open(url: String) {
        val full = if (url.startsWith("http")) url else "https://$url"
        try {
            startActivity(Intent(this, MainActivity::class.java).putExtra("open_url", full))
        } catch (_: Exception) {
            toastNote("Could not open the browser")
        }
    }

    private fun share() {
        try {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_TEXT, "Mini X - a privacy-first browser. https://github.com/vasudev-work/nijatva-browser-mini-x")
            startActivity(Intent.createChooser(i, "Share Mini X"))
        } catch (_: Exception) { }
    }

    private fun mail() {
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:vasudev.work@rediffmail.com")))
        } catch (_: Exception) { toastNote("No mail app found") }
    }

    private fun clean() {
        try {
            android.webkit.WebStorage.getInstance().deleteAllData()
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            toastNote("Site data cleared")
        } catch (_: Exception) { toastNote("Nothing to clear") }
    }

    private fun toastNote(t: String) {
        android.widget.Toast.makeText(this, t, android.widget.Toast.LENGTH_SHORT).show()
    }
}
