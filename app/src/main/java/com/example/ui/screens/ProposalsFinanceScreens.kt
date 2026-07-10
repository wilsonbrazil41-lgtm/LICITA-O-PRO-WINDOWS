package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.FinanceBarChart
import com.example.ui.viewmodel.AppViewModel
import java.util.*

// ==========================================
// 1. EMISSÃO DE PROPOSTAS SCREEN
// ==========================================
@Composable
fun ProposalsBuilderScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val organs by viewModel.organs.collectAsState()
    val products by viewModel.products.collectAsState()

    var selectedOrgan by remember { mutableStateOf<PublicOrgan?>(null) }
    val selectedProducts = remember { mutableStateListOf<Pair<Product, Int>>() } // Product and Quantity

    var discountStr by remember { mutableStateOf("0") }
    var freightStr by remember { mutableStateOf("0") }
    var taxRateStr by remember { mutableStateOf("6") } // Percentage (e.g. 6% Simples Nacional)

    var showProductPicker by remember { mutableStateOf(false) }
    var showProposalPreview by remember { mutableStateOf(false) }

    // Math calculations
    val subtotal = selectedProducts.sumOf { it.first.precoVenda * it.second }
    val purchaseCost = selectedProducts.sumOf { it.first.precoCompra * it.second }

    val discount = discountStr.toDoubleOrNull() ?: 0.0
    val freight = freightStr.toDoubleOrNull() ?: 0.0
    val taxRate = (taxRateStr.toDoubleOrNull() ?: 0.0) / 100.0

    val totalProposta = (subtotal - discount + freight).coerceAtLeast(0.0)
    val calculatedTaxes = totalProposta * taxRate
    val totalCosts = purchaseCost + calculatedTaxes + freight
    val netProfit = totalProposta - purchaseCost - calculatedTaxes
    val profitMargin = if (totalProposta > 0) (netProfit / totalProposta) * 100 else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Emissor de Propostas Comerciais",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Organ Selector
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("1. Selecione o Órgão Destinatário", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (organs.isEmpty()) {
                        Text("Cadastre um órgão público nas outras seções primeiro!", color = Color.Red, fontSize = 12.sp)
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
                                    text = { Text(org.nome, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Product selection panel
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("2. Itens da Proposta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Button(
                            onClick = { showProductPicker = true },
                            modifier = Modifier.testTag("add_item_proposal_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inserir Item", fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedProducts.isEmpty()) {
                        Text("Nenhum produto adicionado à proposta.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        selectedProducts.forEach { (prod, qty) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.descricao, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Qtd: $qty | Unitário: R$ ${String.format("%.2f", prod.precoVenda)}", fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "R$ ${String.format("%.2f", prod.precoVenda * qty)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(onClick = { selectedProducts.remove(Pair(prod, qty)) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        // Math adjustments panel
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("3. Ajustes Financeiros", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discountStr,
                            onValueChange = { discountStr = it },
                            label = { Text("Desconto (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = freightStr,
                            onValueChange = { freightStr = it },
                            label = { Text("Frete / Frete (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = taxRateStr,
                            onValueChange = { taxRateStr = it },
                            label = { Text("Alíquota Imposto (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Totals and Margins summary
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("4. Demonstrativo de Margens", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Subtotal de Venda:", fontSize = 13.sp)
                        Text("R$ ${String.format("%.2f", subtotal)}", fontSize = 13.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Custo de Compra dos Produtos:", fontSize = 13.sp)
                        Text("R$ ${String.format("%.2f", purchaseCost)}", fontSize = 13.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Impostos Estimados (${taxRateStr}%):", fontSize = 13.sp)
                        Text("R$ ${String.format("%.2f", calculatedTaxes)}", fontSize = 13.sp)
                    }
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("VALOR TOTAL DA PROPOSTA:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("R$ ${String.format("%.2f", totalProposta)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    // Lucro e margem
                    val marginColor = if (profitMargin >= 30) Color(0xFF2E7D32) else if (profitMargin >= 15) Color(0xFFEF6C00) else Color(0xFFC62828)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Lucro Líquido Estimado:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("R$ ${String.format("%.2f", netProfit)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = marginColor)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Margem de Lucro (%):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${String.format("%.1f", profitMargin)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = marginColor)
                    }
                }
            }
        }

        // Emission action
        item {
            Button(
                onClick = {
                    if (selectedOrgan == null) {
                        Toast.makeText(context, "Selecione o órgão de destino", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedProducts.isEmpty()) {
                        Toast.makeText(context, "Adicione pelo menos 1 produto", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    showProposalPreview = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_proposal_button")
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gerar Proposta Oficial")
            }
        }
    }

    // Product Picker Dialog
    if (showProductPicker) {
        var query by remember { mutableStateOf("") }
        var qtyStr by remember { mutableStateOf("1") }
        val filtered = products.filter { it.descricao.contains(query, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showProductPicker = false },
            title = { Text("Selecione o Produto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Filtrar descrição") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Quantidade desejada") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Selecione o produto abaixo:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(modifier = Modifier.height(180.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filtered) { p ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val quantity = qtyStr.toIntOrNull() ?: 1
                                        selectedProducts.add(Pair(p, quantity))
                                        showProductPicker = false
                                        Toast.makeText(context, "Item adicionado!", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(p.descricao, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("SKU: ${p.codigo} | Venda: R$ ${p.precoVenda}", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showProductPicker = false }) { Text("Voltar") }
            }
        )
    }

    // Proposal Official PDF Preview Dialog
    if (showProposalPreview) {
        AlertDialog(
            onDismissRequest = { showProposalPreview = false },
            title = { Text("Visualização da Proposta Comercial") },
            text = {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "PROPOSTA COMERCIAL - LICITAÇÃO PRO",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "ÓRGÃO: ${selectedOrgan?.nome}\nCNPJ ÓRGÃO: ${selectedOrgan?.cnpj}\nCIDADE: ${selectedOrgan?.cidade}/${selectedOrgan?.estado}",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontFamily = FontFamily.Monospace
                        )
                        HorizontalDivider(color = Color.Black)

                        Text("ITENS DA PROPOSTA:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        selectedProducts.forEach { (prod, qty) ->
                            Text(
                                text = "- ${prod.descricao} (Cod: ${prod.codigo})\n  Qtd: $qty | Unitário: R$ ${String.format("%.2f", prod.precoVenda)} | Total: R$ ${String.format("%.2f", prod.precoVenda * qty)}",
                                fontSize = 10.sp,
                                color = Color.DarkGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        HorizontalDivider(color = Color.Black)
                        Text(
                            text = "Ajuste Desconto: - R$ ${String.format("%.2f", discount)}\nFrete/Logística: + R$ ${String.format("%.2f", freight)}\nImpostos Estimados: R$ ${String.format("%.2f", calculatedTaxes)}",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "VALOR LÍQUIDO FINAL: R$ ${String.format("%.2f", totalProposta)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showProposalPreview = false
                    viewModel.logAction("Proposta impressa/compartilhada com sucesso para órgão: ${selectedOrgan?.nome}")
                    Toast.makeText(context, "Proposta compartilhada com sucesso como PDF/Excel!", Toast.LENGTH_LONG).show()
                }) {
                    Text("Compartilhar/Imprimir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProposalPreview = false }) { Text("Fechar") }
            }
        )
    }
}

// ==========================================
// 2. FINANCEIRO SCREEN
// ==========================================
@Composable
fun FinanceScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    val totalInflow = transactions.filter { it.tipo == "Entrada" }.sumOf { it.valor }
    val totalOutflow = transactions.filter { it.tipo == "Saída" }.sumOf { it.valor }
    val balance = totalInflow - totalOutflow

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nova Transação")
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
                text = "Fluxo de Caixa & Finanças",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Dynamic Balance comparative bar chart
            ElevatedCard(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                FinanceBarChart(
                    inflow = totalInflow,
                    outflow = totalOutflow,
                    modifier = Modifier.fillMaxSize().padding(12.dp)
                )
            }

            // Quick financial metrics
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricValueCard(title = "Entradas", value = "R$ ${String.format("%,.2f", totalInflow)}", icon = Icons.Default.TrendingUp, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                MetricValueCard(title = "Saídas", value = "R$ ${String.format("%,.2f", totalOutflow)}", icon = Icons.Default.TrendingDown, color = Color(0xFFC62828), modifier = Modifier.weight(1f))
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saldo Líquido em Caixa:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "R$ ${String.format("%,.2f", balance)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            // Transaction log
            Text("Histórico de Lançamentos", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (transactions.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhum lançamento registrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(transactions) { t ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(t.descricao, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Categoria: ${t.categoria} | ${formatDate(t.data)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${if (t.tipo == "Entrada") "+" else "-"} R$ ${String.format("%.2f", t.valor)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (t.tipo == "Entrada") Color(0xFF2E7D32) else Color(0xFFC62828),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    IconButton(onClick = {
                                        if (userRole == "Administrador") {
                                            viewModel.deleteTransaction(t)
                                            Toast.makeText(context, "Lançamento excluído.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Ação restrita! Operador não pode remover lançamentos.", Toast.LENGTH_LONG).show()
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
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
        var desc by remember { mutableStateOf("") }
        var valStr by remember { mutableStateOf("") }
        var tipo by remember { mutableStateOf("Entrada") }
        var cat by remember { mutableStateOf("Venda de Licitação") }

        val categoriesIn = listOf("Venda de Licitação", "Adiantamento", "Rendimento", "Outro")
        val categoriesOut = listOf("Compras Fornecedores", "Impostos", "Logística/Frete", "Administrativo", "Outro")

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Registrar Lançamento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { tipo = "Entrada"; cat = "Venda de Licitação" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (tipo == "Entrada") Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Entrada", color = if (tipo == "Entrada") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { tipo = "Saída"; cat = "Compras Fornecedores" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (tipo == "Saída") Color(0xFFC62828) else MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Saída", color = if (tipo == "Saída") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descrição do lançamento") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = valStr, onValueChange = { valStr = it }, label = { Text("Valor (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    
                    Text("Categoria:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    val cats = if (tipo == "Entrada") categoriesIn else categoriesOut
                    ScrollableTabRow(
                        selectedTabIndex = cats.indexOf(cat).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        cats.forEach { c ->
                            Tab(selected = cat == c, onClick = { cat = c }, text = { Text(c, fontSize = 11.sp) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (desc.isBlank() || valStr.isBlank()) {
                            Toast.makeText(context, "Descrição e valor são obrigatórios", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.saveTransaction(
                            FinanceTransaction(
                                tipo = tipo,
                                descricao = desc,
                                valor = valStr.toDoubleOrNull() ?: 0.0,
                                data = System.currentTimeMillis(),
                                categoria = cat
                            )
                        )
                        showDialog = false
                    }
                ) { Text("Registrar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// ==========================================
// 3. AGENDA / CALENDÁRIO SCREEN
// ==========================================
@Composable
fun ScheduleScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appointments by viewModel.appointments.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Evento")
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
                text = "Agenda de Pregões & Prazos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (appointments.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhum evento agendado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(appointments) { app ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (app.concluido) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Checkbox(
                                        checked = app.concluido,
                                        onCheckedChange = { viewModel.toggleAppointmentConcluded(app) }
                                    )
                                    Column {
                                        Text(
                                            text = app.titulo,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (app.concluido) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${app.tipo} | ${formatDateWithTime(app.dataHora)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (app.descricao.isNotEmpty()) {
                                            Text(app.descricao, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }

                                IconButton(onClick = {
                                    if (userRole == "Administrador") {
                                        viewModel.deleteAppointment(app)
                                        Toast.makeText(context, "Evento cancelado.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Restrito para Operador", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var tit by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var tipo by remember { mutableStateOf("Pregão") }
        var dateStr by remember { mutableStateOf("") }
        var hourStr by remember { mutableStateOf("") }

        val appTypes = listOf("Pregão", "Vencimento de Certidão", "Reunião", "Outro")

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Agendar Novo Compromisso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = tit, onValueChange = { tit = it }, label = { Text("Título do Evento") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                    
                    Text("Tipo do Evento:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    ScrollableTabRow(
                        selectedTabIndex = appTypes.indexOf(tipo).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        appTypes.forEach { t ->
                            Tab(selected = tipo == t, onClick = { tipo = t }, text = { Text(t, fontSize = 11.sp) })
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("Data (DD/MM/AAAA)") }, placeholder = { Text("15/07/2026") }, modifier = Modifier.weight(1.1f))
                        OutlinedTextField(value = hourStr, onValueChange = { hourStr = it }, label = { Text("Hora (HH:MM)") }, placeholder = { Text("10:00") }, modifier = Modifier.weight(0.9f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tit.isBlank()) {
                            Toast.makeText(context, "Preencha o título do evento", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val calendar = Calendar.getInstance()
                        val dParts = dateStr.split("/")
                        val hParts = hourStr.split(":")
                        if (dParts.size == 3) {
                            calendar.set(Calendar.DAY_OF_MONTH, dParts[0].toInt())
                            calendar.set(Calendar.MONTH, dParts[1].toInt() - 1)
                            calendar.set(Calendar.YEAR, dParts[2].toInt())
                        }
                        if (hParts.size == 2) {
                            calendar.set(Calendar.HOUR_OF_DAY, hParts[0].toInt())
                            calendar.set(Calendar.MINUTE, hParts[1].toInt())
                        }

                        viewModel.saveAppointment(
                            Appointment(
                                titulo = tit,
                                descricao = desc,
                                dataHora = calendar.timeInMillis,
                                tipo = tipo
                            )
                        )
                        showDialog = false
                    }
                ) { Text("Agendar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// ==========================================
// 4. CONFIGURAÇÕES & SECURITY AUDIT LOG SCREEN
// ==========================================
@Composable
fun SettingsScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val userRole by viewModel.userRole.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val securityLogs by viewModel.securityLogs.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Configurações do Sistema",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Authentication & Role
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Controle de Permissões de Usuário", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Selecione seu nível de privilégio abaixo para validar restrições:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.setUserRole("Administrador") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (userRole == "Administrador") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Administrador", color = if (userRole == "Administrador") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.setUserRole("Operador") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (userRole == "Operador") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Operador", color = if (userRole == "Operador") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Theme preference
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tema Visual do Sistema", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(if (isDark) "Modo Escuro Ativo" else "Modo Claro Ativo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isDark, onCheckedChange = { viewModel.toggleTheme() })
                }
            }
        }

        // Backup and recovery
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Cópia de Segurança (Backup)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Mantenha seus dados seguros exportando e restaurando em formato SQLite comprimido.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.performBackup { fileName ->
                                    Toast.makeText(context, "Backup salvo com sucesso: $fileName", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4C81)),
                            modifier = Modifier.weight(1f).testTag("backup_manual_button")
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backup Manual", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.restoreBackup("CÓPIA_AUTOMÁTICA.db") {
                                    Toast.makeText(context, "Banco de dados restaurado com sucesso para ponto anterior!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restaurar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Security logs (Logs de acesso)
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Logs de Acesso & Auditoria de Segurança", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Rastreamento em tempo real de operações efetuadas:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.Black.copy(alpha = 0.05f), shape = MaterialTheme.shapes.small)
                            .padding(8.dp)
                    ) {
                        if (securityLogs.isEmpty()) {
                            Text("Nenhum log gravado ainda.", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(securityLogs) { log ->
                                    Text(
                                        text = log,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (log.contains("excluí", ignoreCase = true) || log.contains("restrita", ignoreCase = true)) Color.Red else Color.DarkGray
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
