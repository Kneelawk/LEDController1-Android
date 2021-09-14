package com.kneelawk.ledcontroller1

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kneelawk.ledcontroller1.ui.theme.LEDController1Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

const val ESPLEDS_MESSAGE = "com.kneelawk.ledcontroller1.ESPLEDS_MESSAGE"

class ESPLEDSActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val esp = intent.getParcelableExtra<ESPLEDS>(ESPLEDS_MESSAGE)

        setContent {
            LEDController1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    if (esp != null) {
                        ESPControlView(esp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ESPControlView(esp: ESPLEDS) {
    val scope = rememberCoroutineScope()

    var refreshing by remember { mutableStateOf(true) }
    refreshing = true

    var brightness by remember { mutableStateOf(0) }
    val brightnessDenoiser = remember {
        Denoiser<Int>(scope, Dispatchers.IO) {
            val res = esp.putBrightness(it)
            if (res.isFailure) {
                Log.w(
                    "ESPLEDSActivity",
                    "Error setting brightness: ${res.exceptionOrNull()}"
                )
            }
        }
    }

    suspend fun refresh() {
        refreshing = true
        brightness = esp.getBrightness().getOrDefault(0)
        refreshing = false
    }

    Scaffold(topBar = {
        Column {
            TopAppBar(
                title = {
                    if (esp.name.isBlank()) {
                        Text(text = esp.ip)
                    } else {
                        Text(text = esp.name)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = esp.ip,
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                refresh()
                            }
                        },
                        enabled = !refreshing
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )

            AnimatedVisibility(visible = refreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }) {
        Column {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.primaryVariant,
                elevation = 2.dp
            ) {
                Text(
                    text = "Brightness",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Slider(
                value = brightness.toFloat(),
                onValueChange = {
                    brightness = it.toInt()
                    brightnessDenoiser.send(brightness)
                },
                valueRange = 0F..255F,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp),
                enabled = !refreshing
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.secondary,
                elevation = 1.dp
            ) {
                Text(
                    text = brightness.toString(),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }

    LaunchedEffect(key1 = esp, block = {
        refresh()
    })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LEDController1Theme {
        ESPControlView(esp = ESPLEDS("192.168.1.6", "Name Here", Instant.now()))
    }
}