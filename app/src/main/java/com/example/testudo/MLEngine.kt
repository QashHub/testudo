package com.example.testudo

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * MLEngine — AI & ML Component for Testudo
 * Matches SAS Section 5: AI and ML Architectural Specification
 *
 * Input:  FloatArray of 15 behavioral features (see FEATURE_ORDER)
 * Output: MLResult with label (Safe/Suspicious/Malicious) + risk score 0-100
 */
class MLEngine(context: Context) {

    private val interpreter: Interpreter = Interpreter(loadModelFile(context))

    // Must match feature_columns.pkl order exactly
    companion object {
        val FEATURE_ORDER = listOf(
            "cpu_usage_pct",          // features[0]
            "mem_usage_mb",           // features[1]
            "net_bytes_sent_kb",      // features[2]
            "net_bytes_recv_kb",      // features[3]
            "file_write_ops",         // features[4]
            "file_read_ops",          // features[5]
            "syscall_count",          // features[6]
            "permissions_requested",  // features[7]
            "background_wakeups",     // features[8]
            "inter_app_comms",        // features[9]
            "battery_drain_pct_hr",   // features[10]
            "dns_queries",            // features[11]
            "unique_remote_ips",      // features[12]
            "crypto_api_calls",       // features[13]
            "reflection_api_calls"    // features[14]
        )

        // Risk score thresholds (matches SAS Section 5.1)
        const val THRESHOLD_SAFE       = 30f
        const val THRESHOLD_SUSPICIOUS = 60f
    }

    data class MLResult(
        val label: String,       // "Safe", "Suspicious", or "Malicious"
        val riskScore: Float,    // 0-100
        val probSafe: Float,     // 0.0-1.0
        val probSuspicious: Float,
        val probMalicious: Float
    )

    /**
     * Run inference on a feature vector.
     * Call this from your Core App Logic layer.
     *
     * @param features FloatArray of exactly 15 values in FEATURE_ORDER
     * @return MLResult with classification and risk score
     */
    fun predict(features: FloatArray): MLResult {
        require(features.size == 15) {
            "Expected 15 features, got ${features.size}. Check FEATURE_ORDER."
        }

        val input  = arrayOf(features)           // shape [1, 15]
        val output = Array(1) { FloatArray(3) }  // shape [1, 3]

        interpreter.run(input, output)

        val probs         = output[0]
        val probSafe      = probs[0]
        val probSuspicious = probs[1]
        val probMalicious = probs[2]

        // Risk score formula — matches SAS Section 5.1 spec
        val riskScore = (probSuspicious * 50f + probMalicious * 100f)
            .coerceIn(0f, 100f)

        val label = when {
            riskScore < THRESHOLD_SAFE       -> "Safe"
            riskScore < THRESHOLD_SUSPICIOUS -> "Suspicious"
            else                             -> "Malicious"
        }

        return MLResult(
            label          = label,
            riskScore      = riskScore,
            probSafe       = probSafe,
            probSuspicious = probSuspicious,
            probMalicious  = probMalicious
        )
    }

    /**
     * Convenience function — build features from raw telemetry
     * and run prediction in one call.
     */
    fun predictFromTelemetry(
        cpuUsagePct: Float,
        memUsageMb: Float,
        netBytesSentKb: Float,
        netBytesRecvKb: Float,
        fileWriteOps: Float,
        fileReadOps: Float,
        syscallCount: Float,
        permissionsRequested: Float,
        backgroundWakeups: Float,
        interAppComms: Float,
        batteryDrainPctHr: Float,
        dnsQueries: Float,
        uniqueRemoteIps: Float,
        cryptoApiCalls: Float,
        reflectionApiCalls: Float
    ): MLResult {
        val features = floatArrayOf(
            cpuUsagePct, memUsageMb, netBytesSentKb, netBytesRecvKb,
            fileWriteOps, fileReadOps, syscallCount, permissionsRequested,
            backgroundWakeups, interAppComms, batteryDrainPctHr,
            dnsQueries, uniqueRemoteIps, cryptoApiCalls, reflectionApiCalls
        )
        return predict(features)
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fd = context.assets.openFd("testudo_model.tflite")
        val stream = FileInputStream(fd.fileDescriptor)
        return stream.channel.map(
            FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
    }

    fun close() {
        interpreter.close()
    }
}
