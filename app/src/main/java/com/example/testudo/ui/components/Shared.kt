package com.example.testudo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PremiumToggle(
    isPremium: Boolean,
    onToggle: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1C2B3A))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            "Premium Account",
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5)
        )

        Switch(
            checked = isPremium,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun EditableField(
    label: String,
    value: String,
    editable: Boolean,
    validate: ((String) -> Boolean)? = null,
    errorMessage: String? = null,
    onValueChange: (String) -> Unit
) {

    Column(modifier = Modifier.padding(bottom = 12.dp)) {

        Text(
            label,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5)
        )

        if (editable) {

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFFCDD9E5)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFCDD9E5),
                    unfocusedTextColor = Color(0xFFCDD9E5),
                    focusedBorderColor = Color(0xFF1E3A5F),
                    unfocusedBorderColor = Color(0xFF1E3A5F),
                    cursorColor = Color(0xFF1E3A5F)
                )
            )

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1C2B3A))
                    .padding(14.dp)
            ) {
                Text(
                    value,
                    color = Color(0xFFCDD9E5),
                    fontSize = 16.sp
                )
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
        color = Color(0xFF1E90FF),
    )
}