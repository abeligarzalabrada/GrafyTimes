package com.grilo.grafytimes.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Enhanced list item styling with consistent animation properties
 */
object ListItemStyle {
    // Animation durations
    private val hoverAnimationDuration = AnimationDuration.SHORT
    private val pressAnimationDuration = AnimationDuration.SHORT
    
    // Animation scales
    private val pressedScale = 0.98f
    private val defaultScale = 1f
    
    // Default padding
    val defaultPadding = 12.dp
    
    /**
     * Creates an animated list item with hover and press effects
     */
    @Composable
    fun AnimatedListItem(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        shape: Shape = MaterialTheme.shapes.small,
        backgroundColor: Color = MaterialTheme.colorScheme.surface,
        hoverColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        pressedColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        content: @Composable () -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isPressed by interactionSource.collectIsPressedAsState()
        
        val scale by animateFloatAsState(
            targetValue = if (isPressed) pressedScale else defaultScale,
            animationSpec = tween(durationMillis = pressAnimationDuration),
            label = "listItemScale"
        )
        
        val elevation by animateDpAsState(
            targetValue = if (isHovered || isPressed) 2.dp else 0.dp,
            animationSpec = tween(durationMillis = hoverAnimationDuration),
            label = "listItemElevation"
        )
        
        val bgColor by animateColorAsState(
            targetValue = when {
                isPressed -> pressedColor
                isHovered -> hoverColor
                else -> backgroundColor
            },
            animationSpec = tween(durationMillis = hoverAnimationDuration),
            label = "listItemBackgroundColor"
        )
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(
                    onClick = onClick,
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null // We're handling our own indication
                )
                .scale(scale)
                .background(bgColor)
                .padding(defaultPadding)
        ) {
            content()
        }
    }
    
    /**
     * Creates a bordered list item with hover and press effects
     */
    @Composable
    fun BorderedListItem(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        shape: Shape = MaterialTheme.shapes.small,
        backgroundColor: Color = MaterialTheme.colorScheme.surface,
        hoverColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        pressedColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        borderColor: Color = MaterialTheme.colorScheme.outline,
        content: @Composable () -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isPressed by interactionSource.collectIsPressedAsState()
        
        val scale by animateFloatAsState(
            targetValue = if (isPressed) pressedScale else defaultScale,
            animationSpec = tween(durationMillis = pressAnimationDuration),
            label = "borderedListItemScale"
        )
        
        val bgColor by animateColorAsState(
            targetValue = when {
                isPressed -> pressedColor
                isHovered -> hoverColor
                else -> backgroundColor
            },
            animationSpec = tween(durationMillis = hoverAnimationDuration),
            label = "borderedListItemBackgroundColor"
        )
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(
                    onClick = onClick,
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null // We're handling our own indication
                )
                .scale(scale)
                .background(bgColor)
                .border(1.dp, borderColor, shape)
                .padding(defaultPadding)
        ) {
            content()
        }
    }
}