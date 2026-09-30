package com.bragadev.list.features.listdetail.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bragadev.list.R
import com.bragadev.list.ui.theme.BragadevlistTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Shown in place of the unchecked items once every item of the list is checked: a check
 * mark that pops in and draws itself, with a kind message.
 *
 * When [playConfetti] is true (the user just checked the last item) a confetti burst and a
 * light vibration celebrate the moment; [onConfettiFinished] is then called so it is not
 * replayed when the section is shown again. A list opened already complete gets only the check.
 */
@Composable
fun AllCheckedCelebration(
    playConfetti: Boolean,
    onConfettiFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val circleScale = remember { Animatable(0f) }
    val checkProgress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    // 1 = nothing on screen; the burst runs from 0 to 1.
    val confettiProgress = remember { Animatable(1f) }
    val confetti = remember { List(CONFETTI_COUNT) { ConfettiPiece.random() } }
    val haptics = LocalHapticFeedback.current
    val currentOnConfettiFinished by rememberUpdatedState(onConfettiFinished)

    LaunchedEffect(Unit) {
        launch {
            circleScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow),
            )
        }
        delay(150)
        launch { checkProgress.animateTo(1f, tween(durationMillis = 450, easing = FastOutSlowInEasing)) }
        delay(250)
        textAlpha.animateTo(1f, tween(durationMillis = 400))
    }

    LaunchedEffect(playConfetti) {
        if (!playConfetti) return@LaunchedEffect
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        confettiProgress.snapTo(0f)
        confettiProgress.animateTo(1f, tween(durationMillis = 1600, easing = LinearOutSlowInEasing))
        currentOnConfettiFinished()
    }

    val colorScheme = MaterialTheme.colorScheme
    val confettiColors = listOf(colorScheme.primary, colorScheme.secondary, colorScheme.tertiary, colorScheme.inversePrimary)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(140.dp)) {
                val progress = confettiProgress.value
                if (progress < 1f) {
                    drawRipple(progress, colorScheme.primary)
                    confetti.forEach { piece -> drawConfetti(piece, progress, confettiColors) }
                }
            }
            Canvas(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        scaleX = circleScale.value
                        scaleY = circleScale.value
                    },
            ) {
                drawCircle(color = colorScheme.primary)
                drawCheckMark(checkProgress.value, colorScheme.onPrimary)
            }
        }
        Text(
            text = stringResource(R.string.list_detail_all_checked_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { alpha = textAlpha.value },
        )
        Text(
            text = stringResource(R.string.list_detail_all_checked_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .graphicsLayer { alpha = textAlpha.value },
        )
    }
}

private const val CONFETTI_COUNT = 28

/** One confetti piece: where it flies to, how it looks and how it spins. Distances are fractions of the canvas. */
private class ConfettiPiece(
    val angle: Float,
    val distance: Float,
    val colorIndex: Int,
    val width: Float,
    val height: Float,
    val spins: Float,
    val isRound: Boolean,
) {
    companion object {
        fun random() = ConfettiPiece(
            angle = Random.nextFloat() * 2f * PI.toFloat(),
            distance = 0.45f + Random.nextFloat() * 0.55f,
            colorIndex = Random.nextInt(4),
            width = 5f + Random.nextFloat() * 4f,
            height = 8f + Random.nextFloat() * 6f,
            spins = (Random.nextFloat() - 0.5f) * 4f,
            isRound = Random.nextInt(4) == 0,
        )
    }
}

/** A ring that grows from the check and fades, like a pulse. */
private fun DrawScope.drawRipple(progress: Float, color: Color) {
    drawCircle(
        color = color.copy(alpha = (1f - progress) * 0.35f),
        radius = size.minDimension * (0.32f + 0.35f * progress),
        style = Stroke(width = 3.dp.toPx() * (1f - progress) + 0.5f),
    )
}

/** Pieces burst out from the center, slow down, fall a little with gravity and fade out. */
private fun DrawScope.drawConfetti(piece: ConfettiPiece, progress: Float, colors: List<Color>) {
    val travel = 1f - (1f - progress) * (1f - progress) // ease-out
    val maxDistance = size.minDimension * 0.75f
    val x = center.x + cos(piece.angle) * piece.distance * maxDistance * travel
    val y = center.y + sin(piece.angle) * piece.distance * maxDistance * travel + progress * progress * maxDistance * 0.5f
    val alpha = if (progress < 0.7f) 1f else (1f - progress) / 0.3f
    val color = colors[piece.colorIndex].copy(alpha = alpha)
    val pieceSize = Size(piece.width.dp.toPx() * 0.6f, piece.height.dp.toPx() * 0.6f)
    if (piece.isRound) {
        drawCircle(color = color, radius = pieceSize.width / 2f, center = Offset(x, y))
    } else {
        rotate(degrees = piece.spins * 360f * progress, pivot = Offset(x, y)) {
            drawRect(
                color = color,
                topLeft = Offset(x - pieceSize.width / 2f, y - pieceSize.height / 2f),
                size = pieceSize,
            )
        }
    }
}

/** Draws the first [progress] (0..1) of a check mark, so it looks hand-drawn. */
private fun DrawScope.drawCheckMark(progress: Float, color: Color) {
    if (progress <= 0f) return
    val w = size.width
    val check = Path().apply {
        moveTo(w * 0.28f, w * 0.52f)
        lineTo(w * 0.44f, w * 0.67f)
        lineTo(w * 0.72f, w * 0.37f)
    }
    val measure = PathMeasure().apply { setPath(check, forceClosed = false) }
    val drawn = Path()
    measure.getSegment(0f, measure.length * progress, drawn, startWithMoveTo = true)
    drawPath(
        path = drawn,
        color = color,
        style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

@Preview(showBackground = true)
@Composable
private fun AllCheckedCelebrationPreview() {
    BragadevlistTheme {
        AllCheckedCelebration(playConfetti = false, onConfettiFinished = {})
    }
}
