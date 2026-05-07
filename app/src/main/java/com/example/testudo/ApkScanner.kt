package com.example.testudo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile

/**
 * ApkScanner — Static APK analysis
 * Scans the APK file itself for malicious indicators
 * Does NOT require root — works on any Android device
 */
object ApkScanner {

    data class ApkScanResult(
        val packageName: String,
        val appName: String,
        val riskScore: Int,
        val threats: List<ThreatDetail>,
        val certificate: CertificateInfo,
        val permissions: PermissionAnalysis,
        val apkHash: String
    )

    data class ThreatDetail(
        val title: String,
        val description: String,
        val severity: Severity,
        val recommendation: String
    )

    data class CertificateInfo(
        val issuer: String,
        val isSelfSigned: Boolean,
        val isDebugSigned: Boolean,
        val fingerprint: String
    )

    data class PermissionAnalysis(
        val total: Int,
        val dangerous: Int,
        val dangerousNames: List<String>,
        val combosFound: List<String>
    )

    enum class Severity { CRITICAL, HIGH, MEDIUM, LOW, INFO }

    // Known malicious API patterns in DEX code
    private val MALICIOUS_API_PATTERNS = listOf(
        Triple("sendTextMessage", Severity.HIGH,
            "App can send SMS messages — common in SMS fraud malware"),
        Triple("getDeviceId", Severity.MEDIUM,
            "App reads device IMEI — used for device fingerprinting"),
        Triple("execShell", Severity.CRITICAL,
            "App executes shell commands — possible root exploit"),
        Triple("Runtime.exec", Severity.CRITICAL,
            "App executes system commands — possible malware dropper"),
        Triple("DexClassLoader", Severity.HIGH,
            "App loads code dynamically — possible code injection"),
        Triple("PathClassLoader", Severity.MEDIUM,
            "App loads external classes — possible plugin malware"),
        Triple("getSubscriberId", Severity.MEDIUM,
            "App reads SIM serial number — device tracking"),
        Triple("getLine1Number", Severity.MEDIUM,
            "App reads phone number without justification"),
        Triple("abortBroadcast", Severity.HIGH,
            "App intercepts system broadcasts — possible SMS hijacking"),
        Triple("forName", Severity.LOW,
            "App uses Java reflection — can hide malicious code"),
    )

    // Dangerous permission combinations
    private val DANGEROUS_COMBOS = listOf(
        Pair(
            listOf("android.permission.READ_SMS", "android.permission.SEND_SMS"),
            "SMS fraud pattern — reads and sends SMS messages"
        ),
        Pair(
            listOf("android.permission.RECORD_AUDIO", "android.permission.INTERNET"),
            "Remote audio surveillance — records and uploads audio"
        ),
        Pair(
            listOf("android.permission.ACCESS_FINE_LOCATION", "android.permission.INTERNET"),
            "Location tracking — sends precise GPS location to remote server"
        ),
        Pair(
            listOf("android.permission.READ_CONTACTS", "android.permission.INTERNET"),
            "Contact harvesting — uploads your contact list"
        ),
        Pair(
            listOf("android.permission.CAMERA", "android.permission.INTERNET"),
            "Remote camera access — may take photos silently"
        ),
        Pair(
            listOf("android.permission.BIND_ACCESSIBILITY_SERVICE", "android.permission.INTERNET"),
            "Credential theft — accessibility service can read screen content"
        ),
        Pair(
            listOf("android.permission.INSTALL_PACKAGES", "android.permission.INTERNET"),
            "Dropper malware — downloads and installs additional apps silently"
        ),
    )

    /**
     * Scan an installed app's APK file
     */
    fun scanInstalledApp(context: Context, packageName: String): ApkScanResult {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(packageName, 0)
        val appName = pm.getApplicationLabel(appInfo).toString()
        val apkPath = appInfo.sourceDir

        val threats = mutableListOf<ThreatDetail>()
        var riskScore = 0

        // ── 1. Certificate analysis ───────────────────────────────────
        val certInfo = analyzeCertificate(pm, packageName)
        if (certInfo.isSelfSigned) {
            threats.add(ThreatDetail(
                title = "Self-signed certificate",
                description = "This app is not signed by a trusted certificate authority. Legitimate apps from the Play Store use proper certificates.",
                severity = Severity.MEDIUM,
                recommendation = "Be cautious — this app may have been sideloaded or tampered with."
            ))
            riskScore += 15
        }
        if (certInfo.isDebugSigned) {
            threats.add(ThreatDetail(
                title = "Debug certificate detected",
                description = "This app is signed with a debug certificate, meaning it was not properly released through official channels.",
                severity = Severity.HIGH,
                recommendation = "This app should not be on a production device. Consider uninstalling it."
            ))
            riskScore += 25
        }

        // ── 2. Permission analysis ────────────────────────────────────
        val permAnalysis = analyzePermissions(pm, packageName)

        // Check dangerous combos
        for ((combo, description) in DANGEROUS_COMBOS) {
            if (combo.all { perm -> permAnalysis.dangerousNames.any { it.contains(perm) } }) {
                threats.add(ThreatDetail(
                    title = "Dangerous permission combination",
                    description = description,
                    severity = Severity.HIGH,
                    recommendation = "Review whether this app genuinely needs these permissions for its stated purpose."
                ))
                riskScore += 20
            }
        }

        // Too many dangerous permissions
        if (permAnalysis.dangerous > 8) {
            threats.add(ThreatDetail(
                title = "Excessive dangerous permissions (${permAnalysis.dangerous})",
                description = "This app requests ${permAnalysis.dangerous} dangerous permissions. Most legitimate apps need fewer than 5.",
                severity = Severity.MEDIUM,
                recommendation = "Check if each permission is justified by the app's functionality."
            ))
            riskScore += 10
        }

        // ── 3. APK content scan ───────────────────────────────────────
        val apkThreats = scanApkContent(apkPath)
        threats.addAll(apkThreats)
        for (threat in apkThreats) {
            riskScore += when (threat.severity) {
                Severity.CRITICAL -> 30
                Severity.HIGH     -> 20
                Severity.MEDIUM   -> 10
                Severity.LOW      -> 5
                Severity.INFO     -> 0
            }
        }

        // ── 4. Install source check ───────────────────────────────────
        val isSideloaded = isSideloaded(context, packageName)
        if (isSideloaded) {
            threats.add(ThreatDetail(
                title = "Sideloaded application",
                description = "This app was not installed from the Google Play Store or a known trusted source.",
                severity = Severity.MEDIUM,
                recommendation = "Only install apps from trusted sources. Sideloaded apps bypass Google's security scanning."
            ))
            riskScore += 15
        }

        // ── 5. Compute APK hash ───────────────────────────────────────
        val apkHash = computeMd5(apkPath) ?: "unknown"

        return ApkScanResult(
            packageName  = packageName,
            appName      = appName,
            riskScore    = riskScore.coerceIn(0, 100),
            threats      = threats,
            certificate  = certInfo,
            permissions  = permAnalysis,
            apkHash      = apkHash
        )
    }

    /**
     * Scan APK file content for malicious strings
     * Reads the DEX bytecode as text and looks for patterns
     */
    private fun scanApkContent(apkPath: String): List<ThreatDetail> {
        val found = mutableListOf<ThreatDetail>()
        try {
            ZipFile(apkPath).use { zip ->
                // Scan classes.dex files
                val dexEntries = zip.entries().asSequence()
                    .filter { it.name.endsWith(".dex") }

                for (entry in dexEntries) {
                    val content = zip.getInputStream(entry).readBytes()
                    val text = String(content, Charsets.ISO_8859_1)

                    for ((pattern, severity, description) in MALICIOUS_API_PATTERNS) {
                        if (text.contains(pattern) &&
                            found.none { it.title.contains(pattern) }) {
                            found.add(ThreatDetail(
                                title          = "Suspicious API: $pattern",
                                description    = description,
                                severity       = severity,
                                recommendation = "Verify this API usage is legitimate for the app's stated purpose."
                            ))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Can't read APK — not necessarily malicious
        }
        return found
    }

    /**
     * Analyse app certificate
     */
    private fun analyzeCertificate(pm: PackageManager, packageName: String): CertificateInfo {
        return try {
            val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo?.apkContentsSigners ?: emptyArray()
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                    .signatures ?: emptyArray()
            }

            if (signatures.isEmpty()) {
                return CertificateInfo("Unknown", true, false, "none")
            }

            val sig = signatures[0]
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(sig.toByteArray())
            val fingerprint = digest.joinToString(":") { "%02X".format(it) }

            // Check for debug keystore fingerprint
            val isDebug = fingerprint.startsWith("A4:0D:A8") // common debug key prefix

            // Simple self-signed check: for Play Store apps the cert has a proper issuer
            val certBytes = sig.toByteArray()
            val isSelfSigned = certBytes.size < 500 // very small certs are usually self-signed

            CertificateInfo(
                issuer      = if (isSelfSigned) "Self-signed" else "Certificate Authority",
                isSelfSigned = isSelfSigned,
                isDebugSigned = isDebug,
                fingerprint = fingerprint.take(47) // first 5 bytes for display
            )
        } catch (e: Exception) {
            CertificateInfo("Unknown", false, false, "error")
        }
    }

    /**
     * Analyse permissions in detail
     */
    private fun analyzePermissions(pm: PackageManager, packageName: String): PermissionAnalysis {
        val allPerms = try {
            pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions?.toList() ?: emptyList()
        } catch (e: Exception) { emptyList() }

        val dangerous = allPerms.filter { perm ->
            try {
                val info = pm.getPermissionInfo(perm, 0)
                info.protectionLevel and 0xFF == 1 // dangerous = level 1
            } catch (e: Exception) {
                // If we can't get info, check for common dangerous permission names
                perm.contains("READ_") || perm.contains("WRITE_") ||
                        perm.contains("ACCESS_") || perm.contains("RECORD_") ||
                        perm.contains("CAMERA") || perm.contains("SEND_") ||
                        perm.contains("RECEIVE_")
            }
        }

        val combosFound = DANGEROUS_COMBOS
            .filter { (combo, _) -> combo.all { perm -> allPerms.any { it.contains(perm) } } }
            .map { it.second }

        return PermissionAnalysis(
            total         = allPerms.size,
            dangerous     = dangerous.size,
            dangerousNames = dangerous,
            combosFound   = combosFound
        )
    }

    private fun computeMd5(filePath: String): String? {
        return try {
            val md = MessageDigest.getInstance("MD5")
            File(filePath).inputStream().use { stream ->
                val buffer = ByteArray(8192)
                var bytes: Int
                while (stream.read(buffer).also { bytes = it } != -1) {
                    md.update(buffer, 0, bytes)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) { null }
    }

    private fun isSideloaded(context: Context, packageName: String): Boolean {
        return try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(packageName)
            }
            installer == null || installer !in listOf(
                "com.android.vending",
                "com.google.android.feedback"
            )
        } catch (e: Exception) { false }
    }
}
