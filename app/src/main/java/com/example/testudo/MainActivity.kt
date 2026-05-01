package com.example.testudo
import android.Manifest
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.testudo.ui.theme.TestudoTheme
import androidx.core.app.ActivityCompat
import com.example.testudo.utils.PermissionGate


//AA
//Initial UI Development Made by Andres any questions please ask.

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        BackgroundScanWorker.schedule(this)

        enableEdgeToEdge()

        setContent {
            TestudoTheme {
                PermissionGate()
            }
        }
    }
}

