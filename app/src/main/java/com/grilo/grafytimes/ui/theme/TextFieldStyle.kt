package com.grilo.grafytimes.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Enhanced text field styling with consistent animation properties
 */
object TextFieldStyle {
    // Animation durations
    private val focusAnimationDuration = AnimationDuration.MEDIUM
    
    // Animation scales
    private val focusedScale = 1.02f
    private val defaultScale = 1f
    
    /**
     * Creates an animated outlined text field with focus effects
     */
    @Composable
    fun AnimatedOutlinedTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        label: @Composable (() -> Unit)? = null,
        placeholder: @Composable (() -> Unit)? = null,
        supportingText: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
        isError: Boolean = false,
        enabled: Boolean = true,
        readOnly: Boolean = false,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        singleLine: Boolean = false,
        maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
        shape: Shape = MaterialTheme.shapes.small,
        colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()
        
        val scale by animateFloatAsState(
            targetValue = if (isFocused) focusedScale else defaultScale,
            animationSpec = tween(durationMillis = focusAnimationDuration),
            label = "textFieldScale"
        )
        
        Column(modifier = modifier) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale),
                label = label,
                placeholder = placeholder,
                trailingIcon = trailingIcon,
                isError = isError,
                enabled = enabled,
                readOnly = readOnly,
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                singleLine = singleLine,
                maxLines = maxLines,
                shape = shape,
                colors = colors,
                interactionSource = interactionSource
            )
            
            // Supporting text with animation
            if (supportingText != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(AnimationDuration.SHORT)),
                    exit = fadeOut(animationSpec = tween(AnimationDuration.SHORT))
                ) {
                    Box(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                        supportingText()
                    }
                }
            }
        }
    }
    
    /**
     * Box composable for supporting text
     */
    @Composable
    private fun Box(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = modifier
        ) {
            content()
        }
    }
}