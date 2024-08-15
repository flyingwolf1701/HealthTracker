package com.example.healthTracker


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen(
                        onBasicDataClick = { startActivity(Intent(this, BasicDataActivity::class.java)) },
                        onSleepDataClick = { startActivity(Intent(this, SleepDataActivity::class.java)) }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(onBasicDataClick: () -> Unit, onSleepDataClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onBasicDataClick, modifier = Modifier.fillMaxWidth()) {
            Text("Basic Data")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSleepDataClick, modifier = Modifier.fillMaxWidth()) {
            Text("Sleep Data")
        }
    }
}


