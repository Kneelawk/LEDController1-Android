package com.kneelawk.ledcontroller1

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kneelawk.ledcontroller1.ui.theme.LEDController1Theme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

class MainActivity : AppCompatActivity() {
    private val scope = MainScope()
    private val monitor = ESPLEDSMonitor(scope)
    private var listening = true
    private lateinit var espList: SnapshotStateList<ESPLEDS>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LEDController1Theme {
                espList = remember { mutableStateListOf() }
                MainView(espList)
            }
        }

        scope.launch {
            while (listening) {
                monitor.collectESPs(espList)

                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listening = false
        monitor.destroy()
        scope.cancel()
    }
}

@Composable
fun MainView(espList: List<ESPLEDS>) {
    LazyColumn {
        items(items = espList, key = { it.ip }) { esp ->
            ESPView(esp = esp)
        }
    }
}

@Composable
fun ESPView(esp: ESPLEDS) {
    val context = LocalContext.current
    OutlinedButton(onClick = {
        val intent = Intent(context, ESPLEDSActivity::class.java).apply {
            putExtra(ESPLEDS_MESSAGE, esp)
        }
        context.startActivity(intent)
    }, modifier = Modifier.fillMaxWidth(), shape = RectangleShape) {
        Row(
            modifier = Modifier.padding(all = 8.dp) then Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (esp.name.isBlank()) {
                Text(
                    text = esp.ip,
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.h6
                )
            } else {
                Text(
                    text = esp.name,
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = esp.ip,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewESPView() {
    LEDController1Theme {
        ESPView(esp = ESPLEDS("192.168.1.6", "Name Here", Instant.now()))
    }
}