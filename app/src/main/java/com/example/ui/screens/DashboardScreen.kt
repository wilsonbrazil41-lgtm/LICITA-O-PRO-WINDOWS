package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Certificate
import com.example.data.model.Tender
import com.example.data.model.Appointment
import com.example.ui.components.DashboardDonutChart
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToSection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tenders by viewModel.tenders.collectAsState()
    val products by viewModel.products.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val certificates by viewModel.certificates.collectAsState()
    val appointments by viewModel.appointments.collectAsState()

    // Calculate metrics
    val totalTenders = tenders.size
    val inProgressTenders = tenders.filter { it.situacao == "Em Andamento" || it.situacao == "Cadastrada" }
    val wonTenders = tenders.filter { it.situacao == "Vencida" }
    val lostTenders = tenders.filter { it.situacao == "Perdida" }

    val wonCount = wonTenders.size
    val lostCount = lostTenders.size
    val activeCount = inProgressTenders.size

    val totalSold = wonTenders.sumOf { it.valorVencedor }
    val estimatedPipeline = inProgressTenders.sumOf { it.valorEstimado }

    // Expired or expiring certificates list
    val now = System.currentTimeMillis()
    val dayMillis = 24 * 3600 * 1000L
    val warningLimit = now + (30 * dayMillis) // 30 days window

    val problematicCerts = certificates.filter { it.dataVencimento < warningLimit }.sortedBy { it.dataVencimento }
    
    // Upcoming Bidding dates (next 3)
    val upcomingPregões = appointments
        .filter { !it.concluido && it.dataHora >= now && it.tipo == "Pregão" }
        .sortedBy { it.dataHora }
        .take(3)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_scroll_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Licitação PRO",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gerencie todo o fluxo de compras públicas, propostas e certidões regulamentares em um só lugar.",
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Financial Overview Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricValueCard(
                    title = "Valor Total Vendido",
                    value = "R$ ${String.format("%,.2f", totalSold)}",
                    icon = Icons.Default.MonetizationOn,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                MetricValueCard(
                    title = "Valor em Disputa",
                    value = "R$ ${String.format("%,.2f", estimatedPipeline)}",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFFE65100),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Counters Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniCounterCard(
                        title = "Licitações",
                        count = totalTenders.toString(),
                        icon = Icons.Default.Assignment,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSection("Licitações") }
                    )
                    MiniCounterCard(
                        title = "Produtos",
                        count = products.size.toString(),
                        icon = Icons.Default.Inventory,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSection("Produtos") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniCounterCard(
                        title = "Fornecedores",
                        count = suppliers.size.toString(),
                        icon = Icons.Default.LocalShipping,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSection("Fornecedores") }
                    )
                    MiniCounterCard(
                        title = "Documentos",
                        count = certificates.size.toString(),
                        icon = Icons.Default.VerifiedUser,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSection("Certidões") }
                    )
                }
            }
        }

        // Donut Chart Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Distribuição de Resultados",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    DashboardDonutChart(
                        wonCount = wonCount,
                        lostCount = lostCount,
                        activeCount = activeCount,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Alerts (Certificates Expiring)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alertas de Vencimento de Certidões",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { onNavigateToSection("Certidões") }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Ver Certidões")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (problematicCerts.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "OK", tint = Color(0xFF388E3C))
                            Text(
                                text = "Excelente! Todas as certidões estão atualizadas e regulares.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            problematicCerts.take(3).forEach { cert ->
                                val isExpired = cert.dataVencimento < now
                                val daysLeft = ((cert.dataVencimento - now) / dayMillis).toInt()

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isExpired) Color(0xFFFEEBEE) else Color(0xFFFFF3E0),
                                        contentColor = if (isExpired) Color(0xFFC62828) else Color(0xFFE65100)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isExpired) Icons.Default.Error else Icons.Default.Warning,
                                            contentDescription = "Alerta"
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cert.nome,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (isExpired) "Vencida" else "Vence em $daysLeft dias (${formatDate(cert.dataVencimento)})",
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Tenders (Licitações Ativas)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Gavel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Licitações Ativas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        TextButton(
                            onClick = { onNavigateToSection("Licitações") },
                            modifier = Modifier.testTag("view_all_active_tenders")
                        ) {
                            Text("Ver Todas", fontSize = 12.sp)
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val activeTenders = tenders.filter { it.situacao == "Em Andamento" || it.situacao == "Cadastrada" }
                    if (activeTenders.isEmpty()) {
                        Text(
                            text = "Nenhuma licitação ativa cadastrada no momento.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            activeTenders.take(4).forEach { tender ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToSection("Licitações") }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${tender.modalidade} Nº ${tender.numero}/${tender.ano}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        shape = MaterialTheme.shapes.extraSmall
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tender.situacao,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        // Title (Objeto)
                                        Text(
                                            text = tender.objeto,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Órgão Público
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.weight(1.1f)
                                            ) {
                                                Icon(
                                                    Icons.Default.Business,
                                                    contentDescription = "Órgão Público",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = tender.orgaoNome,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            
                                            // Data de encerramento
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.weight(0.9f)
                                            ) {
                                                Icon(
                                                    Icons.Default.CalendarToday,
                                                    contentDescription = "Data de Encerramento",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = formatDate(tender.dataHora),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Tenders (Pregões)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Próximos Pregões e Prazos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { onNavigateToSection("Agenda") }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Ver Agenda")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (upcomingPregões.isEmpty()) {
                        Text(
                            text = "Nenhum pregão agendado nos próximos dias.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            upcomingPregões.forEach { appt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                MaterialTheme.colorScheme.secondaryContainer,
                                                shape = MaterialTheme.shapes.small
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Gavel,
                                            contentDescription = "Bidding",
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appt.titulo,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = formatDateWithTime(appt.dataHora),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricValueCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MiniCounterCard(
    title: String,
    count: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = count,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Format utilities
fun formatDate(millis: Long): String {
    if (millis == 0L) return ""
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatDateWithTime(millis: Long): String {
    if (millis == 0L) return ""
    val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
