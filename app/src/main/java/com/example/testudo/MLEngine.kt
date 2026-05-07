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

    private val interpreter: Interpreter = Interpreter(
        loadModelFile(context),
        Interpreter.Options().apply {
            setNumThreads(2)
            setUseXNNPACK(false)
        }
    )

    companion object {


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
        require(features.size == 50) {
            "Expected 50 features, got ${features.size}. Check FEATURE_ORDER."
        }

        val inputBuffer = Array(1) { features }
        val outputBuffer = Array(1) { FloatArray(2) }
        interpreter.run(inputBuffer, outputBuffer)

        val probs = outputBuffer[0]
        val probBenign  = probs[0]
        val probMalware = probs[1]

        val riskScore = (probMalware * 100f).coerceIn(0f, 100f)

        val label = when {
            riskScore < 30f  -> "Safe"
            riskScore < 60f  -> "Suspicious"
            else             -> "Malicious"
        }

        return MLResult(
            label          = label,
            riskScore      = riskScore,
            probSafe       = probBenign,
            probSuspicious = probMalware * 0.5f,
            probMalicious  = probMalware
        )
    }

    /**
     * Convenience function — build features from raw telemetry
     * and run prediction in one call.
     */


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
