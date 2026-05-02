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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testudo.ui.components.EditableField
import com.example.testudo.ui.components.PremiumToggle
import com.example.testudo.ui.components.TitleSection
import com.example.testudo.viewmodel.UserViewModel

@Composable
fun UserScreen(
    vm: UserViewModel = viewModel()
) {
    val user by vm.user
    val editMode by vm.editMode

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
                            .filter { it.isNotBlank() }
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

        Spacer(Modifier.height(36.dp))

        EditableField("Name", currentUser.name, editMode) {
            vm.updateName(it)
        }

        EditableField("Email", currentUser.email, editMode) {
            vm.updateEmail(it)
        }

        EditableField("Phone Number", currentUser.phone, editMode) {
            vm.updatePhone(it)
        }

        EditableField("Payment Details", currentUser.paymentDetails, editMode) {
            vm.updatePaymentDetails(it)
        }

        Spacer(Modifier.height(16.dp))

        PremiumToggle(currentUser.isPremium) {
            vm.updatePremium(it)
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { vm.toggleEditMode() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Text(if (editMode) "Cancel" else "Edit", color = Color.White)
            }

            if (editMode) {
                Button(
                    onClick = { vm.saveUser() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }
    }
}