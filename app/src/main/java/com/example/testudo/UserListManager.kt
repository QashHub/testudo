package com.example.testudo

import android.content.Context
import org.json.JSONArray

/**
 * UserListManager — manages user whitelist and blacklist
 * Stored locally in SharedPreferences — no external dependencies needed
 */
object UserListManager {

    private const val PREFS_NAME    = "testudo_user_lists"
    private const val KEY_WHITELIST = "whitelist"
    private const val KEY_BLACKLIST = "blacklist"

    // ── Whitelist ──────────────────────────────────────────────────────
    fun addToWhitelist(context: Context, packageName: String) {
        val list = getWhitelist(context).toMutableSet()
        list.add(packageName)
        saveList(context, KEY_WHITELIST, list)
        removeFromBlacklist(context, packageName)
    }

    fun removeFromWhitelist(context: Context, packageName: String) {
        val list = getWhitelist(context).toMutableSet()
        list.remove(packageName)
        saveList(context, KEY_WHITELIST, list)
    }

    fun isWhitelisted(context: Context, packageName: String): Boolean =
        packageName in getWhitelist(context)

    fun getWhitelist(context: Context): Set<String> =
        loadList(context, KEY_WHITELIST)

    // ── Blacklist ──────────────────────────────────────────────────────
    fun addToBlacklist(context: Context, packageName: String) {
        val list = getBlacklist(context).toMutableSet()
        list.add(packageName)
        saveList(context, KEY_BLACKLIST, list)
        removeFromWhitelist(context, packageName)
    }

    fun removeFromBlacklist(context: Context, packageName: String) {
        val list = getBlacklist(context).toMutableSet()
        list.remove(packageName)
        saveList(context, KEY_BLACKLIST, list)
    }

    fun isBlacklisted(context: Context, packageName: String): Boolean =
        packageName in getBlacklist(context)

    fun getBlacklist(context: Context): Set<String> =
        loadList(context, KEY_BLACKLIST)

    // ── Helpers ────────────────────────────────────────────────────────
    private fun saveList(context: Context, key: String, list: Set<String>) {
        val json = JSONArray(list).toString()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(key, json).apply()
    }

    private fun loadList(context: Context, key: String): Set<String> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(key, null) ?: return emptySet()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }.toSet()
        } catch (e: Exception) { emptySet() }
    }

    fun clearAll(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
