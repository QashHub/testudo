package com.example.testudo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testudo.ui.components.EditableField
import com.example.testudo.ui.components.PremiumToggle
import com.example.testudo.ui.components.TitleSection
import com.example.testudo.data.local.db.DatabaseProvider
import com.example.testudo.data.local.db.entity.UserProfileEntity
import kotlinx.coroutines.launch

@Composable
fun UserScreen() {

    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val dao = remember { db.userProfileDao() }
    val coroutineScope = rememberCoroutineScope()

    var user by remember { mutableStateOf<UserProfileEntity?>(null) }
    var editMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val existingUser = dao.getUserProfile()

        user = existingUser ?: UserProfileEntity(
            id = 1,
            name = "John Doe",
            email = "john@example.com",
            phone = "+44 7123456789",
            paymentDetails = "Visa •••• 1234",
            isPremium = false
        )
    }

    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1B2A)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    val currentUser = user ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TitleSection()
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "User Profile",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5)
        )

        Spacer(Modifier.height(16.dp))

// Profile Initials
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E3A5F)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.name
                            .split(" ")
                            .take(2)
                            .joinToString("") { it.first().uppercase() },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF87)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = currentUser.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCDD9E5)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Spacer(Modifier.height(20.dp))

        EditableField("Name", currentUser.name, editMode) {
            user = currentUser.copy(name = it)
        }

        EditableField("Email", currentUser.email, editMode) {
            user = currentUser.copy(email = it)
        }

        EditableField("Phone", currentUser.phone, editMode) {
            user = currentUser.copy(phone = it)
        }

        EditableField("Payment Details", currentUser.paymentDetails, editMode) {
            user = currentUser.copy(paymentDetails = it)
        }

        Spacer(Modifier.height(16.dp))

        PremiumToggle(currentUser.isPremium) {
            user = currentUser.copy(isPremium = it)
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            Button(
                onClick = { editMode = !editMode },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Text(if (editMode) "Cancel" else "Edit", color = Color.White)
            }

            if (editMode) {
                Button(
                    onClick = {
                        user?.let { updatedUser ->
                            coroutineScope.launch {
                                dao.insertOrUpdateUserProfile(updatedUser)
                            }
                        }
                        editMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }
    }
}