package com.grilo.grafytimes.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Enhanced button styling with consistent animation properties
 */
object ButtonStyle {
    // Default animation values
    private val pressedScale = 0.96f
    private val defaultScale = 1f
    
    /**
     * Creates an animated button with press effects
     */
    @Composable
    fun AnimatedButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        shape: Shape = MaterialTheme.shapes.small,
        colors: ButtonColors = ButtonDefaults.buttonColors(),
        contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
        content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        
        val scale by animateFloatAsState(
            targetValue = if (isPressed) pressedScale else defaultScale,
            label = "buttonScale"
        )
        
        Button(
            onClick = onClick,
            modifier = modifier.scale(scale),
            enabled = enabled,
            shape = shape,
            colors = colors,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content
        )
    }
    
    /**
     * Creates an animated text button with press effects
     */
    @Composable
    fun AnimatedTextButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        shape: Shape = MaterialTheme.shapes.small,
        colors: ButtonColors = ButtonDefaults.textButtonColors(),
        contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
        content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        
        val scale by animateFloatAsState(
            targetValue = if (isPressed) pressedScale else defaultScale,
            label = "textButtonScale"
        )
        
        TextButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            enabled = enabled,
            shape = shape,
            colors = colors,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content
        )
    }
}