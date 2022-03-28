package com.kneelawk.ledcontroller1

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kneelawk.ledcontroller1.ui.theme.LEDController1Theme
import kotlinx.coroutines.*
import java.time.Instant

const val ESPLEDS_MESSAGE = "com.kneelawk.ledcontroller1.ESPLEDS_MESSAGE"

private const val ESPLEDSA_TAG = "ESPLEDSActivity"
private const val ESPLEDS_MAX_NAME_LENGTH = 32

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

    var renaming by remember { mutableStateOf(false) }
    var espName by remember { mutableStateOf(esp.initialName) }
    val espNameConflator = remember {
        Conflator<String>(scope, Dispatchers.IO) { newName ->
            espName = esp.putName(newName).getOrDefaultElse("") {
                Log.w(ESPLEDSA_TAG, "Error setting name: $it")
            }
        }
    }

    var brightness by remember { mutableStateOf(0) }
    val brightnessConflator = remember {
        Conflator<Int>(scope, Dispatchers.IO) {
            val res = esp.putBrightness(it)
            if (res.isFailure) {
                Log.w(ESPLEDSA_TAG, "Error setting brightness: ${res.exceptionOrNull()}")
            }
        }
    }

    var frameDuration by remember { mutableStateOf(5) }
    val frameDurationConflator = remember {
        Conflator<Int>(scope, Dispatchers.IO) {
            val res = esp.putFrameDuration(it)
            if (res.isFailure) {
                Log.w(ESPLEDSA_TAG, "Error setting frame duration: ${res.exceptionOrNull()}")
            }
        }
    }

    var huePerFrame by remember { mutableStateOf(1) }
    val huePerFrameConflator = remember {
        Conflator<Int>(scope, Dispatchers.IO) {
            val res = esp.putHuePerFrame(it)
            if (res.isFailure) {
                Log.w(ESPLEDSA_TAG, "Error setting hue per frame: ${res.exceptionOrNull()}")
            }
        }
    }

    var huePerPixel by remember { mutableStateOf(3) }
    val huePerPixelConflator = remember {
        Conflator<Int>(scope, Dispatchers.IO) {
            val res = esp.putHuePerPixel(it)
            if (res.isFailure) {
                Log.w(ESPLEDSA_TAG, "Error setting hue per pixel: ${res.exceptionOrNull()}")
            }
        }
    }

    suspend fun refresh() = coroutineScope {
        refreshing = true
        val nameJob = async {
            espName = esp.getName()
                .getOrDefaultElse("") { Log.w(ESPLEDSA_TAG, "Error getting name: $it") }
        }
        val brightnessJob = async {
            brightness = esp.getBrightness()
                .getOrDefaultElse(0) { Log.w(ESPLEDSA_TAG, "Error getting brightness: $it") }
        }
        val frameDurationJob = async {
            frameDuration = esp.getFrameDuration()
                .getOrDefaultElse(5) { Log.w(ESPLEDSA_TAG, "Error getting frame duration: $it") }
        }
        val huePerFrameJob = async {
            huePerFrame = esp.getHuePerFrame()
                .getOrDefaultElse(1) { Log.w(ESPLEDSA_TAG, "Error getting hue per frame: $it") }
                .toByte().toInt()
        }
        val huePerPixelJob = async {
            huePerPixel = esp.getHuePerPixel()
                .getOrDefaultElse(3) { Log.w(ESPLEDSA_TAG, "Error getting hue per pixel: $it") }
                .toByte().toInt()
        }
        listOf(nameJob, brightnessJob, frameDurationJob, huePerFrameJob, huePerPixelJob).awaitAll()
        refreshing = false
    }

    if (renaming) {
        Dialog(onDismissRequest = { renaming = false }) {
            RenameDialog(
                name = espName,
                newName = {
                    espNameConflator.send(it)
                    renaming = false
                },
                cancel = { renaming = false }
            )
        }
    }

    Scaffold(topBar = {
        Column {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable(
                            enabled = !refreshing,
                            onClickLabel = "Rename controller",
                            role = Role.Button
                        ) { renaming = true }
                    ) {
                        if (espName.isBlank()) {
                            Text(text = esp.ip)
                        } else {
                            Text(text = espName, modifier = Modifier.alignBy(FirstBaseline))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = esp.ip,
                                style = MaterialTheme.typography.subtitle1,
                                modifier = Modifier.alignBy(FirstBaseline)
                            )
                        }
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
            SectionHeader(title = "Brightness")

            Slider(
                value = brightness.toFloat(),
                onValueChange = {
                    brightness = it.toInt()
                    brightnessConflator.send(brightness)
                },
                valueRange = 0F..255F,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp),
                enabled = !refreshing,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.secondary,
                    activeTrackColor = MaterialTheme.colors.secondaryVariant
                )
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.secondaryVariant,
                elevation = 1.dp
            ) {
                Text(
                    text = brightness.toString(),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            SectionHeader(title = "Frame Duration")

            Spinner(
                value = frameDuration,
                onValueChange = {
                    frameDuration = it
                    frameDurationConflator.send(frameDuration)
                },
                minValue = 5,
                maxValue = 1000,
                enabled = !refreshing
            )

            SectionHeader(title = "Hue Per Frame")

            Spinner(
                value = huePerFrame,
                onValueChange = {
                    huePerFrame = it
                    huePerFrameConflator.send(huePerFrame)
                },
                minValue = -128,
                maxValue = 127,
                enabled = !refreshing
            )

            SectionHeader(title = "Hue Per Pixel")

            Spinner(
                value = huePerPixel,
                onValueChange = {
                    huePerPixel = it
                    huePerPixelConflator.send(huePerPixel)
                },
                minValue = -128,
                maxValue = 127,
                enabled = !refreshing
            )
        }
    }

    LaunchedEffect(key1 = esp, block = {
        refresh()
    })
}

@Composable
fun RenameDialog(name: String, newName: (String) -> Unit, cancel: () -> Unit) {
    var curName by remember { mutableStateOf(name) }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(text = "Rename controller", style = MaterialTheme.typography.subtitle1)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = curName,
            onValueChange = {
                if (it.length <= ESPLEDS_MAX_NAME_LENGTH) {
                    curName = it
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardActions = KeyboardActions { newName(curName) },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = MaterialTheme.colors.secondary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F)
            )

            TextButton(onClick = cancel) {
                Text(text = "Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(onClick = { newName(curName) }) {
                Text(text = "Ok")
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.primaryVariant,
        elevation = 2.dp
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LEDController1Theme {
        ESPControlView(esp = ESPLEDS("192.168.1.6", "Name Here", Instant.now()))
    }
}

@Composable
@Preview(showBackground = true)
fun RenameDialogPreview() {
    LEDController1Theme {
        RenameDialog(name = "Name Here", newName = {}, cancel = {})
    }
}
