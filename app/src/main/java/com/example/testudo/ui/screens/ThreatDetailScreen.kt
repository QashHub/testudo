package com.example.testudo.ui.screens

import com.example.testudo.ApkScanner
import com.example.testudo.UserListManager
import com.example.testudo.AppTelemetry
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ── Colour palette matching the dark navy theme ──────────────────
private val BG_MAIN    = Color(0xFF0D1B2A)
private val BG_CARD    = Color(0xFF1C2B3A)
private val BG_BUTTON  = Color(0xFF1E3A5F)
private val TEXT_PRIMARY   = Color(0xFFCDD9E5)
private val TEXT_SECONDARY = Color(0xFF8EAECF)
private val ACCENT_TEAL    = Color(0xFF00897B)
private val RED_DANGER     = Color(0xFFB22222)
private val ORANGE_WARN    = Color(0xFFF9A825)
private val GREEN_SAFE     = Color(0xFF2E7D32)

@Composable
fun ThreatDetailScreen(
    navController: NavHostController,
    packageName: String,
    passedRiskScore: String? = null  // ✅ NEW: Accept passed risk score
) {

    val context = navController.context
    var scanResult by remember { mutableStateOf<ApkScanner.ApkScanResult?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // ✅ NEW: Use passed risk score if available, otherwise scan
    var displayRiskScore by remember { mutableStateOf(passedRiskScore?.toIntOrNull() ?: 0) }

    LaunchedEffect(packageName) {
        withContext(Dispatchers.IO) {
            try {
                scanResult = ApkScanner.scanInstalledApp(context, packageName)
                // ✅ NEW: If no passed score, use the scanned result's score
                if (passedRiskScore == null && scanResult != null) {
                    displayRiskScore = scanResult!!.riskScore
                } else if (passedRiskScore != null) {
                    // ✅ Use the passed score
                    displayRiskScore = passedRiskScore.toInt()
                }
            } catch (e: Exception) { }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG_MAIN)
    ) {
        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Testudo",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00BFFF)
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(BG_BUTTON)
                .clickable { navController.popBackStack() }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text("< Back", color = TEXT_PRIMARY, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = ACCENT_TEAL)
                    Spacer(Modifier.height(8.dp))
                    Text("Scanning APK...", color = TEXT_PRIMARY)
                }
            }
        } else {
            val result = scanResult
            if (result == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Could not scan this app.", color = TEXT_PRIMARY)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Risk Score Header ──────────────────────────────
                    item {
                        // ✅ FIXED: Use displayRiskScore instead of result.riskScore
                        val bgColor = when {
                            displayRiskScore >= 60 -> RED_DANGER
                            displayRiskScore >= 30 -> ORANGE_WARN
                            else                   -> GREEN_SAFE
                        }
                        val label = when {
                            displayRiskScore >= 60 -> "MALICIOUS"
                            displayRiskScore >= 30 -> "SUSPICIOUS"
                            else                   -> "SAFE"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgColor)
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayRiskScore.toString(),  // ✅ FIXED
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(result.appName, fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp, color = Color.White)
                                    Text(label, fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.9f))
                                    Text("${result.threats.size} threats detected",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }

                    // ── APK Hash ───────────────────────────────────────
                    item {
                        InfoCard("APK Fingerprint", result.apkHash, BG_CARD)
                    }

                    // ── Certificate ────────────────────────────────────
                    item {
                        val certColor = if (result.certificate.isSelfSigned ||
                            result.certificate.isDebugSigned) BG_BUTTON else BG_CARD
                        SectionCard("Certificate", certColor) {
                            DetailRow("Issuer", result.certificate.issuer)
                            DetailRow("Self-signed",
                                if (result.certificate.isSelfSigned) "Yes ⚠️" else "No ✓")
                            DetailRow("Debug signed",
                                if (result.certificate.isDebugSigned) "Yes ⛔" else "No ✓")
                            DetailRow("Fingerprint", result.certificate.fingerprint)
                        }
                    }

                    // ── Permissions ────────────────────────────────────
                    item {
                        SectionCard("Permissions", BG_CARD) {
                            DetailRow("Total permissions",
                                result.permissions.total.toString())
                            DetailRow("Dangerous permissions",
                                result.permissions.dangerous.toString())
                            if (result.permissions.dangerousNames.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text("Dangerous permissions:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TEXT_PRIMARY)
                                result.permissions.dangerousNames.take(8).forEach { perm ->
                                    Text("• ${perm.substringAfterLast(".")}",
                                        fontSize = 12.sp, color = ORANGE_WARN)
                                }
                            }
                        }
                    }

                    // ── Threats ────────────────────────────────────────
                    if (result.threats.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1A3A2A))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓ No threats detected",
                                    color = Color(0xFF00FF87),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp)
                            }
                        }
                    } else {
                        item {
                            Text("Detected Threats (${result.threats.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TEXT_PRIMARY)
                        }
                        items(result.threats) { threat ->
                            ThreatCard(threat)
                        }
                    }

                    // ── Actions ────────────────────────────────────────
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GREEN_SAFE)
                                    .clickable {
                                        UserListManager.addToWhitelist(context, packageName)
                                        navController.popBackStack()
                                    }
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓ Mark Safe", color = Color.White,
                                    fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RED_DANGER)
                                    .clickable {
                                        UserListManager.addToBlacklist(context, packageName)
                                        navController.popBackStack()
                                    }
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✗ Blacklist", color = Color.White,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ThreatCard(threat: ApkScanner.ThreatDetail) {
    var expanded by remember { mutableStateOf(false) }

    val color = when (threat.severity) {
        ApkScanner.Severity.CRITICAL -> Color(0xFFFF1744)
        ApkScanner.Severity.HIGH     -> Color(0xFFFF5252)
        ApkScanner.Severity.MEDIUM   -> Color(0xFFFF9800)
        ApkScanner.Severity.LOW      -> Color(0xFFFFC107)
        ApkScanner.Severity.INFO     -> Color(0xFF40C4FF)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C2B3A))
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(threat.title, fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, color = Color(0xFFCDD9E5))
                Text(threat.severity.name, fontSize = 11.sp, color = color)
            }
            Text(if (expanded) "▲" else "▼",
                color = Color(0xFF8EAECF), fontSize = 12.sp)
        }

        AnimatedVisibility(visible = expanded,
            enter = expandVertically(), exit = shrinkVertically()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.08f))
                    .padding(14.dp)
            ) {
                Text(threat.description, fontSize = 13.sp, color = Color(0xFFCDD9E5))
                Spacer(Modifier.height(6.dp))
                Text("Recommendation:", fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, color = Color(0xFFCDD9E5))
                Text(threat.recommendation, fontSize = 12.sp,
                    color = Color(0xFF8EAECF))
            }
        }
    }
}

@Composable
fun SectionCard(title: String, bgColor: Color,
                content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(14.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold,
            fontSize = 14.sp, color = Color(0xFFCDD9E5))
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun InfoCard(label: String, value: String, bgColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(14.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold,
            fontSize = 12.sp, color = Color(0xFFCDD9E5))
        Text(value, fontSize = 11.sp, color = Color(0xFF8EAECF))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFFCDD9E5),
            fontWeight = FontWeight.Bold)
        Text(value, fontSize = 12.sp, color = Color(0xFF8EAECF))
    }
}
