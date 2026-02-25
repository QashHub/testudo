package com.example.testudo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testudo.ui.theme.TestudoTheme

//Initial UI Development Made by Andres any questions please ask.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TestudoTheme {
                TestudoApp()
            }
        }
    }
}

@Composable
fun TestudoApp() {
    Scaffold(
        bottomBar = { BottomNavBar() }
    ) { innerPadding ->
        MainScreen(
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFD8CFAE))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            TitleSection()

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                contentAlignment = Alignment.Center
            ) {
                SurroundingButtons()
                ScanButton()
            }
        }
    }
}

@Composable
fun TitleSection() {
    Text(
        text = "Testudo",
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFB22222)
    )
}

@Composable
fun SurroundingButtons() {
    Column(
        verticalArrangement = Arrangement.spacedBy(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(80.dp)
        ) {
            FeatureButton("Alerts")
            FeatureButton("Status")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(80.dp)
        ) {
            FeatureButton("AI Assist")
            FeatureButton("Clean Cache")
        }
    }
}

@Composable
fun FeatureButton(text: String) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF8B1A1A))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScanButton() {
    Box(
        modifier = Modifier
            .size(180.dp)
            .clip(CircleShape)
            .background(Color(0xFFB8860B))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SCAN",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5A3E2B)
        )
    }
}

@Composable
fun BottomNavBar() {
    NavigationBar(
        containerColor = Color(0xFFC9C2A6)
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Person, contentDescription = "User") },
            label = { Text("User") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun TestudoAppPreview() {
    TestudoTheme {
        TestudoApp()
    }
}

@Preview(showBackground = true)
@Composable
fun FeatureButtonPreview() {
    TestudoTheme {
        FeatureButton("Alerts")
    }
}

@Preview(showBackground = true)
@Composable
fun ScanButtonPreview() {
    TestudoTheme {
        ScanButton()
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavPreview() {
    TestudoTheme {
        BottomNavBar()
    }
}

