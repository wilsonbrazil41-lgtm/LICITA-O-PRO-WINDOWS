package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardDonutChart(
    wonCount: Int,
    lostCount: Int,
    activeCount: Int,
    modifier: Modifier = Modifier
) {
    val total = wonCount + lostCount + activeCount
    if (total == 0) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sem dados para o gráfico",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }

    val wonAngle = (wonCount.toFloat() / total) * 360f
    val lostAngle = (lostCount.toFloat() / total) * 360f
    val activeAngle = (activeCount.toFloat() / total) * 360f

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "chartAnim"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Donut Canvas
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            val wonColor = Color(0xFF388E3C)
            val lostColor = Color(0xFFD32F2F)
            val activeColor = Color(0xFFF57C00)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 30f
                val sizeMin = size.minDimension - strokeWidth
                val offset = strokeWidth / 2
                val chartSize = Size(sizeMin, sizeMin)

                var startAngle = -90f

                // Active
                if (activeAngle > 0) {
                    drawArc(
                        color = activeColor,
                        startAngle = startAngle,
                        sweepAngle = activeAngle * animProgress,
                        useCenter = false,
                        topLeft = Offset(offset, offset),
                        size = chartSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += activeAngle
                }

                // Won
                if (wonAngle > 0) {
                    drawArc(
                        color = wonColor,
                        startAngle = startAngle,
                        sweepAngle = wonAngle * animProgress,
                        useCenter = false,
                        topLeft = Offset(offset, offset),
                        size = chartSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += wonAngle
                }

                // Lost
                if (lostAngle > 0) {
                    drawArc(
                        color = lostColor,
                        startAngle = startAngle,
                        sweepAngle = lostAngle * animProgress,
                        useCenter = false,
                        topLeft = Offset(offset, offset),
                        size = chartSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Inside Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = total.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Licitações",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Legends List
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 16.dp)
        ) {
            LegendItem(color = Color(0xFFF57C00), text = "Em aberto: $activeCount (${String.format("%.0f", (activeCount.toFloat()/total)*100)}%)")
            LegendItem(color = Color(0xFF388E3C), text = "Vencidas: $wonCount (${String.format("%.0f", (wonCount.toFloat()/total)*100)}%)")
            LegendItem(color = Color(0xFFD32F2F), text = "Perdidas: $lostCount (${String.format("%.0f", (lostCount.toFloat()/total)*100)}%)")
        }
    }
}

@Composable
fun FinanceBarChart(
    inflow: Double,
    outflow: Double,
    modifier: Modifier = Modifier
) {
    val total = inflow + outflow
    if (total == 0.0) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sem movimentações financeiras",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }

    val maxVal = maxOf(inflow, outflow).toFloat()
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "barAnim"
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Text(
            text = "Comparativo Mensal",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val greenBrush = Brush.verticalGradient(listOf(Color(0xFF81C784), Color(0xFF388E3C)))
            val redBrush = Brush.verticalGradient(listOf(Color(0xFFE57373), Color(0xFFD32F2F)))

            // Inflow Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                val barHeightFrac = (inflow.toFloat() / maxVal) * 0.85f * animProgress
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight(barHeightFrac.coerceAtLeast(0.05f))
                        .background(greenBrush, shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Entradas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "R$ ${String.format("%.0f", inflow)}",
                    fontSize = 11.sp,
                    color = Color(0xFF388E3C),
                    fontWeight = FontWeight.Bold
                )
            }

            // Outflow Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                val barHeightFrac = (outflow.toFloat() / maxVal) * 0.85f * animProgress
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight(barHeightFrac.coerceAtLeast(0.05f))
                        .background(redBrush, shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Saídas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "R$ ${String.format("%.0f", outflow)}",
                    fontSize = 11.sp,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.extraSmall)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
