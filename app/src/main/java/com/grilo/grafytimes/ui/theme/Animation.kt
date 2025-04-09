package com.grilo.grafytimes.ui.theme

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Durations for different types of animations
 */
object AnimationDuration {
    const val SHORT = 150
    const val MEDIUM = 300
    const val LONG = 500
}

/**
 * Predefined fade animations with consistent durations
 */
object FadeAnimations {
    val fadeIn = fadeIn(animationSpec = tween(AnimationDuration.MEDIUM))
    val fadeOut = fadeOut(animationSpec = tween(AnimationDuration.MEDIUM))
    
    val fadeInFast = fadeIn(animationSpec = tween(AnimationDuration.SHORT))
    val fadeOutFast = fadeOut(animationSpec = tween(AnimationDuration.SHORT))
    
    val fadeInSlow = fadeIn(animationSpec = tween(AnimationDuration.LONG))
    val fadeOutSlow = fadeOut(animationSpec = tween(AnimationDuration.LONG))
}

/**
 * Predefined slide animations with consistent durations
 */
object SlideAnimations {
    // Slide in animations
    val slideInLeft = slideInHorizontally(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = LinearOutSlowInEasing),
        initialOffsetX = { fullWidth -> -fullWidth }
    )
    
    val slideInRight = slideInHorizontally(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = LinearOutSlowInEasing),
        initialOffsetX = { fullWidth -> fullWidth }
    )
    
    val slideInTop = slideInVertically(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = LinearOutSlowInEasing),
        initialOffsetY = { fullHeight -> -fullHeight }
    )
    
    val slideInBottom = slideInVertically(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = LinearOutSlowInEasing),
        initialOffsetY = { fullHeight -> fullHeight }
    )
    
    // Slide out animations
    val slideOutLeft = slideOutHorizontally(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = FastOutLinearInEasing),
        targetOffsetX = { fullWidth -> -fullWidth }
    )
    
    val slideOutRight = slideOutHorizontally(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = FastOutLinearInEasing),
        targetOffsetX = { fullWidth -> fullWidth }
    )
    
    val slideOutTop = slideOutVertically(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = FastOutLinearInEasing),
        targetOffsetY = { fullHeight -> -fullHeight }
    )
    
    val slideOutBottom = slideOutVertically(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = FastOutLinearInEasing),
        targetOffsetY = { fullHeight -> fullHeight }
    )
}

/**
 * Predefined scale animations with consistent durations
 */
object ScaleAnimations {
    val scaleIn = scaleIn(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = FastOutSlowInEasing),
        initialScale = 0.8f
    )
    
    val scaleOut = scaleOut(
        animationSpec = tween(AnimationDuration.MEDIUM),
        targetScale = 0.8f
    )
}

/**
 * Predefined expand/shrink animations with consistent durations
 */
object ExpandAnimations {
    val expandIn = expandIn(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = LinearOutSlowInEasing),
        expandFrom = androidx.compose.ui.Alignment.Center
    )
    
    val expandVertically = expandVertically(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = LinearOutSlowInEasing),
        expandFrom = androidx.compose.ui.Alignment.Top
    )
    
    val expandHorizontally = expandHorizontally(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = LinearOutSlowInEasing),
        expandFrom = androidx.compose.ui.Alignment.Start
    )
    
    val shrinkOut = shrinkOut(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = FastOutLinearInEasing),
        shrinkTowards = androidx.compose.ui.Alignment.Center
    )
    
    val shrinkVertically = shrinkVertically(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = FastOutLinearInEasing),
        shrinkTowards = androidx.compose.ui.Alignment.Top
    )
    
    val shrinkHorizontally = shrinkHorizontally(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = FastOutLinearInEasing),
        shrinkTowards = androidx.compose.ui.Alignment.Start
    )
}

/**
 * Combined animations for common use cases
 */
object CombinedAnimations {
    // Enter transitions
    val fadeInWithSlideUp: EnterTransition = fadeIn(
        animationSpec = tween(AnimationDuration.MEDIUM)
    ) + slideInVertically(
        animationSpec = tween(AnimationDuration.MEDIUM),
        initialOffsetY = { fullHeight -> fullHeight / 4 }
    )
    
    val fadeInWithScale: EnterTransition = fadeIn(
        animationSpec = tween(AnimationDuration.MEDIUM)
    ) + scaleIn(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = FastOutSlowInEasing),
        initialScale = 0.9f
    )
    
    // Exit transitions
    val fadeOutWithSlideDown: ExitTransition = fadeOut(
        animationSpec = tween(AnimationDuration.MEDIUM)
    ) + slideOutVertically(
        animationSpec = tween(AnimationDuration.MEDIUM),
        targetOffsetY = { fullHeight -> fullHeight / 4 }
    )
    
    val fadeOutWithScale: ExitTransition = fadeOut(
        animationSpec = tween(AnimationDuration.MEDIUM)
    ) + scaleOut(
        animationSpec = tween(AnimationDuration.MEDIUM, easing = FastOutLinearInEasing),
        targetScale = 0.9f
    )
}