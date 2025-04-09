package com.grilo.grafytimes.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Enhanced card styling with consistent elevation, shape, and animation properties
 */
object CardStyle {
    // Default elevation values
    val defaultElevation = 2.dp
    val hoveredElevation = 4.dp
    val pressedElevation = 1.dp
    
    // Default shape
    val defaultShape @Composable get() = MaterialTheme.shapes.medium
    
    // Default padding
    val defaultPadding = 16.dp
    
    /**
     * Creates an animated card with hover and press effects
     */
    @Composable
    fun AnimatedCard(
        onClick: () -> Unit = {},
        modifier: Modifier = Modifier,
        shape: Shape = defaultShape,
        colors: CardColors = CardDefaults.cardColors(),
        elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = defaultElevation),
        border: BorderStroke? = null,
        content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isPressed by interactionSource.collectIsPressedAsState()
        
        val elevationValue = when {
            isPressed -> pressedElevation
            isHovered -> hoveredElevation
            else -> defaultElevation
        }
        
        val animatedElevation by animateDpAsState(
            targetValue = elevationValue,
            label = "elevation"
        )
        
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            label = "scale"
        )
        
        Card(
            modifier = modifier
                .shadow(animatedElevation, shape, clip = false)
                .padding(defaultPadding / 4),
            shape = shape,
            colors = colors,
            border = border,
            content = content
        )
    }
    
    /**
     * Creates an elevated card with consistent styling
     */
    @Composable
    fun StyledCard(
        modifier: Modifier = Modifier,
        shape: Shape = defaultShape,
        colors: CardColors = CardDefaults.cardColors(),
        elevation: Dp = defaultElevation,
        border: BorderStroke? = null,
        content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
    ) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = border,
            content = content
        )
    }
    
    /**
     * Creates an elevated card with consistent styling
     */
    @Composable
    fun StyledElevatedCard(
        modifier: Modifier = Modifier,
        shape: Shape = defaultShape,
        colors: CardColors = CardDefaults.elevatedCardColors(),
        elevation: Dp = defaultElevation,
        content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
    ) {
        ElevatedCard(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
            content = content
        )
    }
}