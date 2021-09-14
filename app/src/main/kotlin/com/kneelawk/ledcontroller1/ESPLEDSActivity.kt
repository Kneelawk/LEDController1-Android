package com.kneelawk.ledcontroller1

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kneelawk.ledcontroller1.ui.theme.LEDController1Theme
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

@Composable
fun ESPControlView(esp: ESPLEDS) {
    val scope = rememberCoroutineScope()

    var brightness by remember { mutableStateOf(0) }

    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.primary,
            elevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(all = 4.dp) then Modifier.fillMaxWidth(),
                verticalAlignment = BiasAlignment.Vertical(0.5F),
            ) {
                if (esp.name.isBlank()) {
                    Text(
                        text = esp.ip,
                        style = MaterialTheme.typography.h5
                    )
                } else {
                    Text(
                        text = esp.name,
                        style = MaterialTheme.typography.h5
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = esp.ip,
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.primaryVariant,
            elevation = 2.dp
        ) {
            Text(
                text = "Brightness",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(2.dp)
            )
        }

        Slider(
            value = brightness.toFloat(),
            onValueChange = {
                brightness = it.toInt()
            },
            valueRange = 0F..255F,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 32.dp, end = 32.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.secondary,
            elevation = 1.dp
        ) {
            Text(
                text = brightness.toString(),
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(all = 2.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    val res = esp.putBrightness(brightness)
                    if (res.isFailure) {
                        Log.w(
                            "ESPLEDSActivity",
                            "Error setting brightness: ${res.exceptionOrNull()}"
                        )
                    }
                }
            }, modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text(
                text = "Apply"
            )
        }
    }

    LaunchedEffect(key1 = esp, block = {
        brightness = esp.getBrightness().getOrDefault(0)
    })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LEDController1Theme {
        ESPControlView(esp = ESPLEDS("192.168.1.6", "Name Here", Instant.now()))
    }
}