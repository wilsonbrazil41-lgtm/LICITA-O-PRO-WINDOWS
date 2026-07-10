package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.PublicOrgan
import com.example.data.model.Tender
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BiddingProcessScreen(
    viewModel: AppViewModel,
    initialFilter: String = "Todas", // "Todas", "Pregões", "Dispensas"
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tenders by viewModel.tenders.collectAsState()
    val organs by viewModel.organs.collectAsState()
    val products by viewModel.products.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var activeTab by remember { mutableStateOf(initialFilter) }
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingTender by remember { mutableStateOf<Tender?>(null) }

    // Segmented tenders
    val filteredTenders = tenders.filter {
        val matchesSearch = it.objeto.contains(searchQuery, ignoreCase = true) ||
                it.numero.contains(searchQuery, ignoreCase = true) ||
                it.orgaoNome.contains(searchQuery, ignoreCase = true)

        val matchesTab = when (activeTab) {
            "Pregões" -> !it.isDispensa && it.modalidade.contains("Pregão", ignoreCase = true)
            "Dispensas" -> it.isDispensa || it.modalidade.contains("Dispensa", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesTab
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingTender = null
                showDialog = true
            }, modifier = Modifier.testTag("add_tender_fab")) {
                Icon(Icons.Default.Add, contentDescription = "Nova Licitação")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Gestão de Licitações & Compras",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Segmented Tab Row
            val tabs = listOf("Todas", "Pregões", "Dispensas")
            TabRow(selectedTabIndex = tabs.indexOf(activeTab).coerceAtLeast(0)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        text = { Text(tab, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Search text
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Pesquisar por objeto, número ou órgão") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (filteredTenders.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma oportunidade de licitação cadastrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredTenders) { tender ->
                        val statusColor = when (tender.situacao) {
                            "Vencida" -> StatusWon
                            "Perdida" -> StatusLost
                            "Em Andamento" -> StatusActive
                            "Cadastrada" -> StatusRegistered
                            "Suspensa" -> StatusSuspended
                            else -> StatusDraft
                        }

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingTender = tender
                                    showDialog = true
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${tender.modalidade} Nº ${tender.numero}/${tender.ano}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Órgão: ${tender.orgaoNome}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = tender.objeto,
                                            fontSize = 13.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (userRole == "Administrador") {
                                                viewModel.deleteTender(tender)
                                                Toast.makeText(context, "Registro excluído.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Ação restrita! Operador não pode excluir licitações.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Data Limite: ${formatDateWithTime(tender.dataHora)}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = "Valor Estimado: R$ ${String.format("%,.2f", tender.valorEstimado)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (tender.situacao == "Vencida" && tender.valorVencedor > 0) {
                                            Text(
                                                text = "Valor Vendido: R$ ${String.format("%,.2f", tender.valorVencedor)}",
                                                fontSize = 12.sp,
                                                color = StatusWon,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Dynamic badge
                                    Box(
                                        modifier = Modifier
                                            .background(statusColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.extraSmall)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tender.situacao,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
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

    if (showDialog) {
        var num by remember { mutableStateOf(editingTender?.numero ?: "") }
        var ano by remember { mutableStateOf(editingTender?.ano ?: Calendar.getInstance().get(Calendar.YEAR).toString()) }
        var modalidade by remember { mutableStateOf(editingTender?.modalidade ?: "Pregão Eletrônico") }
        var objeto by remember { mutableStateOf(editingTender?.objeto ?: "") }
        var selectedOrgan by remember { mutableStateOf<PublicOrgan?>(organs.find { it.id == editingTender?.orgaoId }) }
        var dataStr by remember { mutableStateOf(if (editingTender != null) formatDate(editingTender!!.dataHora) else "") }
        var horaStr by remember { mutableStateOf(if (editingTender != null) formatTime(editingTender!!.dataHora) else "") }
        var situacao by remember { mutableStateOf(editingTender?.situacao ?: "Cadastrada") }
        var valEst by remember { mutableStateOf(editingTender?.valorEstimado?.toString() ?: "") }
        var valVen by remember { mutableStateOf(editingTender?.valorVencedor?.toString() ?: "") }
        var resp by remember { mutableStateOf(editingTender?.responsavel ?: "Carlos Roberto") }
        var obs by remember { mutableStateOf(editingTender?.observacoes ?: "") }
        var isDisp by remember { mutableStateOf(editingTender?.isDispensa ?: (activeTab == "Dispensas")) }

        val modalities = listOf("Pregão Eletrônico", "Dispensa Eletrônica", "Concorrência Pública", "Tomada de Preços", "Inexigibilidade")
        val situations = listOf("Cadastrada", "Em Andamento", "Vencida", "Perdida", "Suspensa", "Cancelada")

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingTender == null) "Cadastrar Licitação" else "Editar Licitação") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = num, onValueChange = { num = it }, label = { Text("Número") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = ano, onValueChange = { ano = it }, label = { Text("Ano") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Text("Modalidade:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        ScrollableTabRow(
                            selectedTabIndex = modalities.indexOf(modalidade).coerceAtLeast(0),
                            edgePadding = 0.dp,
                            divider = {}
                        ) {
                            modalities.forEach { mod ->
                                Tab(
                                    selected = modalidade == mod,
                                    onClick = {
                                        modalidade = mod
                                        isDisp = mod == "Dispensa Eletrônica"
                                    },
                                    text = { Text(mod, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    item { OutlinedTextField(value = objeto, onValueChange = { objeto = it }, label = { Text("Objeto da Licitação") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        Text("Órgão Público Responsável:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        if (organs.isEmpty()) {
                            Text("Cadastre um órgão público primeiro!", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        } else {
                            ScrollableTabRow(
                                selectedTabIndex = organs.indexOf(selectedOrgan).coerceAtLeast(0),
                                edgePadding = 0.dp,
                                divider = {}
                            ) {
                                organs.forEach { org ->
                                    Tab(
                                        selected = selectedOrgan?.id == org.id,
                                        onClick = { selectedOrgan = org },
                                        text = { Text(org.nome, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = dataStr, onValueChange = { dataStr = it }, label = { Text("Data (DD/MM/AAAA)") }, placeholder = { Text("15/07/2026") }, modifier = Modifier.weight(1.1f))
                            OutlinedTextField(value = horaStr, onValueChange = { horaStr = it }, label = { Text("Hora (HH:MM)") }, placeholder = { Text("14:30") }, modifier = Modifier.weight(0.9f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = valEst, onValueChange = { valEst = it }, label = { Text("Valor Estimado") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                            OutlinedTextField(value = valVen, onValueChange = { valVen = it }, label = { Text("Valor Vencedor") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Text("Situação atual:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        ScrollableTabRow(
                            selectedTabIndex = situations.indexOf(situacao).coerceAtLeast(0),
                            edgePadding = 0.dp,
                            divider = {}
                        ) {
                            situations.forEach { sit ->
                                Tab(
                                    selected = situacao == sit,
                                    onClick = { situacao = sit },
                                    text = { Text(sit, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    item { OutlinedTextField(value = resp, onValueChange = { resp = it }, label = { Text("Funcionário Responsável") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = obs, onValueChange = { obs = it }, label = { Text("Observações") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (num.isBlank() || objeto.isBlank()) {
                            Toast.makeText(context, "Número e Objeto são obrigatórios", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedOrgan == null) {
                            Toast.makeText(context, "Selecione um órgão público", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val calendar = Calendar.getInstance()
                        val dParts = dataStr.split("/")
                        val hParts = horaStr.split(":")
                        if (dParts.size == 3) {
                            calendar.set(Calendar.DAY_OF_MONTH, dParts[0].toInt())
                            calendar.set(Calendar.MONTH, dParts[1].toInt() - 1)
                            calendar.set(Calendar.YEAR, dParts[2].toInt())
                        }
                        if (hParts.size == 2) {
                            calendar.set(Calendar.HOUR_OF_DAY, hParts[0].toInt())
                            calendar.set(Calendar.MINUTE, hParts[1].toInt())
                            calendar.set(Calendar.SECOND, 0)
                        }

                        val tenderToSave = Tender(
                            id = editingTender?.id ?: 0,
                            numero = num,
                            ano = ano,
                            modalidade = modalidade,
                            objeto = objeto,
                            orgaoId = selectedOrgan?.id,
                            orgaoNome = selectedOrgan?.nome ?: "",
                            dataHora = calendar.timeInMillis,
                            situacao = situacao,
                            valorEstimado = valEst.toDoubleOrNull() ?: 0.0,
                            valorVencedor = valVen.toDoubleOrNull() ?: 0.0,
                            responsavel = resp,
                            observacoes = obs,
                            isDispensa = isDisp
                        )
                        viewModel.saveTender(tenderToSave)
                        showDialog = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// Format utilities helper
fun formatTime(millis: Long): String {
    if (millis == 0L) return ""
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
