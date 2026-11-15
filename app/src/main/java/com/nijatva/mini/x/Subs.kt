package com.nijatva.mini.x

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Channels you follow, kept on this device. No account, no server, no sync - the same
 * bargain NewPipe makes, and the reason its subscriptions keep working offline.
 */
object Subs {

    data class Channel(val name: String, val url: String)

    private const val KEY = "subs"
    private const val PREFS = "mini_x_subs"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun all(c: Context): List<Channel> {
        val raw = prefs(c).getString(KEY, "[]") ?: "[]"
        val arr = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()
        val out = ArrayList<Channel>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val u = o.optString("u", "")
            if (u.isBlank()) continue
            out.add(Channel(o.optString("n", u), u))
        }
        return out
    }

    fun has(c: Context, url: String): Boolean = all(c).any { it.url == url }

    fun add(c: Context, name: String, url: String) {
        if (url.isBlank() || has(c, url)) return
        val list = all(c).toMutableList()
        list.add(Channel(name, url))
        save(c, list)
    }

    fun remove(c: Context, url: String) {
        save(c, all(c).filterNot { it.url == url })
    }

    private fun save(c: Context, list: List<Channel>) {
        val arr = JSONArray()
        for (ch in list) {
            arr.put(JSONObject().put("n", ch.name).put("u", ch.url))
        }
        prefs(c).edit().putString(KEY, arr.toString()).apply()
    }
}
