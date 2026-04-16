package com.example.testudo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * OnDeviceLearning — model learns from user corrections
 *
 * When a user marks an app as Safe or Malicious,
 * that feedback is stored as a training example.
 * The stored examples can be used to retrain the model.
 *
 * Matches SAS Section 5.1 — Continuous Learning Support
 */
object OnDeviceLearning {

    private const val PREFS_NAME    = "testudo_learning"
    private const val KEY_EXAMPLES  = "training_examples"
    private const val MAX_EXAMPLES  = 500  // max stored examples

    data class TrainingExample(
        val packageName: String,
        val features: FloatArray,
        val userLabel: Int,       // 0=Safe, 1=Suspicious, 2=Malicious
        val timestamp: Long
    )

    /**
     * Record user feedback as a training example
     * Called when user marks app as safe or malicious
     */
    fun recordFeedback(
        context: Context,
        packageName: String,
        features: FloatArray,
        userLabel: Int  // 0=Safe, 2=Malicious
    ) {
        val examples = getExamples(context).toMutableList()

        // Remove existing example for this package if any
        examples.removeAll { it.packageName == packageName }

        // Add new example
        examples.add(TrainingExample(
            packageName = packageName,
            features    = features,
            userLabel   = userLabel,
            timestamp   = System.currentTimeMillis()
        ))

        // Keep only most recent MAX_EXAMPLES
        val trimmed = if (examples.size > MAX_EXAMPLES) {
            examples.sortedByDescending { it.timestamp }.take(MAX_EXAMPLES)
        } else examples

        saveExamples(context, trimmed)
    }

    /**
     * Get all stored training examples
     */
    fun getExamples(context: Context): List<TrainingExample> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EXAMPLES, null) ?: return emptyList()

        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val featArr = obj.getJSONArray("features")
                val features = FloatArray(featArr.length()) {
                    featArr.getDouble(it).toFloat()
                }
                TrainingExample(
                    packageName = obj.getString("packageName"),
                    features    = features,
                    userLabel   = obj.getInt("userLabel"),
                    timestamp   = obj.getLong("timestamp")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    /**
     * Get count of stored examples
     */
    fun getExampleCount(context: Context): Int = getExamples(context).size

    /**
     * Get summary of what the model has learned
     */
    fun getLearningStats(context: Context): String {
        val examples = getExamples(context)
        if (examples.isEmpty()) return "No learning data yet"
        val safe     = examples.count { it.userLabel == 0 }
        val malicious = examples.count { it.userLabel == 2 }
        return "$safe safe examples, $malicious malicious examples collected"
    }

    /**
     * Apply learning to adjust risk score
     * If user previously marked this app, use their label
     */
    fun getAdjustedScore(
        context: Context,
        packageName: String,
        baseScore: Int
    ): Pair<Int, String?> {
        val examples = getExamples(context)
        val example  = examples.find { it.packageName == packageName }
            ?: return Pair(baseScore, null)

        return when (example.userLabel) {
            0    -> Pair(0,   "Score adjusted: you previously marked this as safe")
            2    -> Pair(100, "Score adjusted: you previously marked this as malicious")
            else -> Pair(baseScore, null)
        }
    }

    /**
     * Clear all learning data
     */
    fun clearAll(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    private fun saveExamples(context: Context, examples: List<TrainingExample>) {
        val arr = JSONArray()
        examples.forEach { ex ->
            val obj = JSONObject()
            obj.put("packageName", ex.packageName)
            obj.put("userLabel",   ex.userLabel)
            obj.put("timestamp",   ex.timestamp)
            val featArr = JSONArray()
            ex.features.forEach { featArr.put(it.toDouble()) }
            obj.put("features", featArr)
            arr.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_EXAMPLES, arr.toString()).apply()
    }
}
