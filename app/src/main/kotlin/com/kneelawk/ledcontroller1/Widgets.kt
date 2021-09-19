package com.kneelawk.ledcontroller1

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kneelawk.ledcontroller1.ui.theme.LEDController1Theme

private data class Wrapper<T>(var value: T)

@Composable
fun Spinner(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
    enabled: Boolean = true
) {
    val keepValues = remember { Wrapper(false) }
    var curValue by remember { mutableStateOf(value) }
    var curText by remember { mutableStateOf(value.toString()) }

    if (keepValues.value) {
        keepValues.value = false
    } else {
        curValue = value
        curText = value.toString()
    }

    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        TextField(
            value = curText,
            onValueChange = {
                // Changing curText causes a recompose but we don't want to recalculate curText
                keepValues.value = true
                curText = it
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                var newValue = curValue
                try {
                    newValue = curText.toInt()
                } catch (e: NumberFormatException) {
                }

                curValue = newValue.coerceIn(minValue, maxValue)
                curText = curValue.toString()

                onValueChange(curValue)
            },
            shape = RectangleShape,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(0.75F),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = MaterialTheme.colors.secondary,
                disabledIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .weight(0.5f)
        ) {
            SmallButton(
                onClick = {
                    curValue = (curValue + 1).coerceAtMost(maxValue)
                    curText = curValue.toString()
                    onValueChange(curValue)
                },
                contentPadding = PaddingValues(0.dp),
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.5f),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                enabled = enabled
            ) {
                Text("+1")
            }
            SmallButton(
                onClick = {
                    curValue = (curValue - 1).coerceAtLeast(minValue)
                    curText = curValue.toString()
                    onValueChange(curValue)
                },
                contentPadding = PaddingValues(0.dp),
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.5f),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                enabled = enabled
            ) {
                Text("-1")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .weight(0.5f)
        ) {
            SmallButton(
                onClick = {
                    curValue = (curValue + 10).coerceAtMost(maxValue)
                    curText = curValue.toString()
                    onValueChange(curValue)
                },
                contentPadding = PaddingValues(0.dp),
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.5f),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                enabled = enabled
            ) {
                Text("+10")
            }
            SmallButton(
                onClick = {
                    curValue = (curValue - 10).coerceAtLeast(minValue)
                    curText = curValue.toString()
                    onValueChange(curValue)
                },
                contentPadding = PaddingValues(0.dp),
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.5f),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                enabled = enabled
            ) {
                Text("-10")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SmallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val contentColor by colors.contentColor(enabled)
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(alpha = 1f),
        border = border,
        elevation = elevation?.elevation(enabled, interactionSource)?.value ?: 0.dp,
        onClick = onClick,
        enabled = enabled,
        role = Role.Button,
        interactionSource = interactionSource,
        indication = rememberRipple()
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
            ProvideTextStyle(
                value = MaterialTheme.typography.button
            ) {
                Row(
                    Modifier
                        .defaultMinSize(
                            minWidth = ButtonDefaults.MinWidth
                        )
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SpinnerPreview() {
    LEDController1Theme {
        Spinner(value = 20, onValueChange = {}, 5, 10000)
    }
}
