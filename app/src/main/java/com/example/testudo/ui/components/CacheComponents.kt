package com.example.testudo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testudo.utils.openAppCacheSettings

@Composable
fun UsageCircle(percent: String) {

    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(Color(0xFF00897B)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            percent,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5)
        )
    }
}

@Composable
fun CacheItem(
    appName: String,
    size: String,
    packageName: String
) {

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(80.dp)
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .background(Color(0xFF1E3A5F))
                .padding(start = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = appName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = size,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .width(130.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .background(Color(0xFF00897B))
                .clickable {
                    openAppCacheSettings(context, packageName)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Clean Cache",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}