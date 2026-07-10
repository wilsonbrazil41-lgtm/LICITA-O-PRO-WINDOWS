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
import com.example.data.model.Certificate
import com.example.data.model.Product
import com.example.data.model.PublicOrgan
import com.example.data.model.Supplier
import com.example.ui.viewmodel.AppViewModel
import java.util.*

// ==========================================
// 1. PRODUCTS REGISTRY SCREEN
// ==========================================
@Composable
fun ProductsScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    val categories = listOf("Todos") + products.map { it.categoria }.distinct().filter { it.isNotEmpty() }

    val filteredProducts = products.filter {
        (selectedCategory == "Todos" || it.categoria == selectedCategory) &&
        (it.descricao.contains(searchQuery, ignoreCase = true) || it.codigo.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProduct = null
                    showDialog = true
                },
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Produto")
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
            // Header actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cadastro de Produtos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = {
                        viewModel.importProductsFromExcel { count ->
                            Toast.makeText(context, "$count produtos importados da planilha Excel!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.testTag("import_excel_button")
                ) {
                    Icon(Icons.Default.FilePresent, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Importar Excel", fontSize = 12.sp)
                }
            }

            // Search and filters
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Pesquisar produto por descrição ou código") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Horizontal categories filter chips
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                edgePadding = 0.dp,
                divider = {}
            ) {
                categories.forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        text = { Text(cat) }
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhum produto cadastrado nesta categoria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).testTag("products_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { prod ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingProduct = prod
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
                                            text = prod.descricao,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Código: ${prod.codigo} | Marca: ${prod.marca}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            if (userRole == "Administrador") {
                                                viewModel.deleteProduct(prod)
                                                Toast.makeText(context, "Produto removido.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Ação restrita! Operador não pode excluir produtos.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Preço Venda: R$ ${String.format("%.2f", prod.precoVenda)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Preço Compra: R$ ${String.format("%.2f", prod.precoCompra)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Stock Alert
                                    val isLowStock = prod.estoque < 20
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isLowStock) Color(0xFFFEEBEE) else Color(0xFFE8F5E9),
                                                shape = MaterialTheme.shapes.extraSmall
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Estoque: ${prod.estoque} ${prod.unidade}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLowStock) Color(0xFFC62828) else Color(0xFF2E7D32)
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

    // Product Add/Edit Dialog
    if (showDialog) {
        var cod by remember { mutableStateOf(editingProduct?.codigo ?: "") }
        var desc by remember { mutableStateOf(editingProduct?.descricao ?: "") }
        var cat by remember { mutableStateOf(editingProduct?.categoria ?: "") }
        var marca by remember { mutableStateOf(editingProduct?.marca ?: "") }
        var uni by remember { mutableStateOf(editingProduct?.unidade ?: "UN") }
        var prCompra by remember { mutableStateOf(editingProduct?.precoCompra?.toString() ?: "") }
        var prVenda by remember { mutableStateOf(editingProduct?.precoVenda?.toString() ?: "") }
        var est by remember { mutableStateOf(editingProduct?.estoque?.toString() ?: "") }
        var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }
        var ncm by remember { mutableStateOf(editingProduct?.ncm ?: "") }
        var cest by remember { mutableStateOf(editingProduct?.cest ?: "") }
        var obs by remember { mutableStateOf(editingProduct?.observacoes ?: "") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingProduct == null) "Cadastrar Produto" else "Editar Produto") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descrição do Produto") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = cod, onValueChange = { cod = it }, label = { Text("Código") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = marca, onValueChange = { marca = it }, label = { Text("Marca") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = cat, onValueChange = { cat = it }, label = { Text("Categoria") }, modifier = Modifier.weight(1.2f))
                            OutlinedTextField(value = uni, onValueChange = { uni = it }, label = { Text("Unidade") }, modifier = Modifier.weight(0.8f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = prCompra, onValueChange = { prCompra = it }, label = { Text("Preço Compra") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                            OutlinedTextField(value = prVenda, onValueChange = { prVenda = it }, label = { Text("Preço Venda") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        OutlinedTextField(value = est, onValueChange = { est = it }, label = { Text("Estoque Inicial") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = ncm, onValueChange = { ncm = it }, label = { Text("NCM") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = cest, onValueChange = { cest = it }, label = { Text("CEST") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        OutlinedTextField(value = obs, onValueChange = { obs = it }, label = { Text("Observações") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (desc.isBlank() || cod.isBlank()) {
                            Toast.makeText(context, "Preencha a descrição e código do produto", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val productToSave = Product(
                            id = editingProduct?.id ?: 0,
                            codigo = cod,
                            descricao = desc,
                            categoria = cat,
                            marca = marca,
                            unidade = uni,
                            precoCompra = prCompra.toDoubleOrNull() ?: 0.0,
                            precoVenda = prVenda.toDoubleOrNull() ?: 0.0,
                            estoque = est.toIntOrNull() ?: 0,
                            ncm = ncm,
                            cest = cest,
                            observacoes = obs
                        )
                        viewModel.saveProduct(productToSave)
                        showDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ==========================================
// 2. SUPPLIERS REGISTRY SCREEN
// ==========================================
@Composable
fun SuppliersScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val suppliers by viewModel.suppliers.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingSupplier by remember { mutableStateOf<Supplier?>(null) }

    val filteredSuppliers = suppliers.filter {
        it.nome.contains(searchQuery, ignoreCase = true) || it.cnpj.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingSupplier = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Fornecedor")
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
                text = "Fornecedores Parceiros",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Pesquisar fornecedor por nome ou CNPJ") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (filteredSuppliers.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhum fornecedor cadastrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredSuppliers) { sup ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingSupplier = sup
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
                                        Text(text = sup.nome, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "CNPJ: ${sup.cnpj}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "Contato: ${sup.contato} | Tel: ${sup.telefone}", fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            if (userRole == "Administrador") {
                                                viewModel.deleteSupplier(sup)
                                                Toast.makeText(context, "Fornecedor removido.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Ação restrita! Operador não pode remover fornecedores.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SuggestionChip(
                                        onClick = {
                                            Toast.makeText(context, "Iniciando contato via WhatsApp com ${sup.contato}", Toast.LENGTH_SHORT).show()
                                        },
                                        label = { Text("WhatsApp: ${sup.whatsapp}", fontSize = 11.sp) },
                                        icon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    )
                                    SuggestionChip(
                                        onClick = {
                                            Toast.makeText(context, "Email para ${sup.email}", Toast.LENGTH_SHORT).show()
                                        },
                                        label = { Text("Email", fontSize = 11.sp) },
                                        icon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var nome by remember { mutableStateOf(editingSupplier?.nome ?: "") }
        var cnpj by remember { mutableStateOf(editingSupplier?.cnpj ?: "") }
        var ie by remember { mutableStateOf(editingSupplier?.inscricaoEstadual ?: "") }
        var end by remember { mutableStateOf(editingSupplier?.endereco ?: "") }
        var tel by remember { mutableStateOf(editingSupplier?.telefone ?: "") }
        var whats by remember { mutableStateOf(editingSupplier?.whatsapp ?: "") }
        var email by remember { mutableStateOf(editingSupplier?.email ?: "") }
        var site by remember { mutableStateOf(editingSupplier?.site ?: "") }
        var contato by remember { mutableStateOf(editingSupplier?.contato ?: "") }
        var produtos by remember { mutableStateOf(editingSupplier?.produtosFornecidos ?: "") }
        var prazo by remember { mutableStateOf(editingSupplier?.prazoEntrega ?: "") }
        var pag by remember { mutableStateOf(editingSupplier?.formaPagamento ?: "") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingSupplier == null) "Novo Fornecedor" else "Editar Fornecedor") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome / Razão Social") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = cnpj, onValueChange = { cnpj = it }, label = { Text("CNPJ") }, modifier = Modifier.weight(1.1f))
                            OutlinedTextField(value = ie, onValueChange = { ie = it }, label = { Text("IE") }, modifier = Modifier.weight(0.9f))
                        }
                    }
                    item { OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("Endereço") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = tel, onValueChange = { tel = it }, label = { Text("Telefone") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = whats, onValueChange = { whats = it }, label = { Text("WhatsApp") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = site, onValueChange = { site = it }, label = { Text("Site") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item { OutlinedTextField(value = contato, onValueChange = { contato = it }, label = { Text("Pessoa de Contato") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = produtos, onValueChange = { produtos = it }, label = { Text("Linha de Produtos Fornecidos") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = prazo, onValueChange = { prazo = it }, label = { Text("Prazo de Entrega") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = pag, onValueChange = { pag = it }, label = { Text("Forma de Pagamento") }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nome.isBlank()) {
                            Toast.makeText(context, "Nome é obrigatório", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val supplierToSave = Supplier(
                            id = editingSupplier?.id ?: 0,
                            nome = nome,
                            cnpj = cnpj,
                            inscricaoEstadual = ie,
                            endereco = end,
                            telefone = tel,
                            whatsapp = whats,
                            email = email,
                            site = site,
                            contato = contato,
                            produtosFornecidos = produtos,
                            prazoEntrega = prazo,
                            formaPagamento = pag
                        )
                        viewModel.saveSupplier(supplierToSave)
                        showDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// ==========================================
// 3. PUBLIC ORGANS SCREEN
// ==========================================
@Composable
fun OrgansScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val organs by viewModel.organs.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingOrgan by remember { mutableStateOf<PublicOrgan?>(null) }

    val filteredOrgans = organs.filter {
        it.nome.contains(searchQuery, ignoreCase = true) || it.cnpj.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingOrgan = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Órgão")
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
                text = "Órgãos Públicos Clientes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Pesquisar órgão por nome ou CNPJ") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (filteredOrgans.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhum órgão público cadastrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredOrgans) { org ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingOrgan = org
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
                                        Text(text = org.nome, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "CNPJ: ${org.cnpj}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "Localização: ${org.cidade}/${org.estado}", fontSize = 12.sp)
                                        Text(text = "Portal: ${org.portal}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                                    }
                                    IconButton(
                                        onClick = {
                                            if (userRole == "Administrador") {
                                                viewModel.deleteOrgan(org)
                                                Toast.makeText(context, "Órgão público removido.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Ação restrita! Operador não pode excluir órgãos.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
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
        var nome by remember { mutableStateOf(editingOrgan?.nome ?: "") }
        var cnpj by remember { mutableStateOf(editingOrgan?.cnpj ?: "") }
        var cid by remember { mutableStateOf(editingOrgan?.cidade ?: "") }
        var est by remember { mutableStateOf(editingOrgan?.estado ?: "") }
        var portal by remember { mutableStateOf(editingOrgan?.portal ?: "") }
        var tel by remember { mutableStateOf(editingOrgan?.telefone ?: "") }
        var email by remember { mutableStateOf(editingOrgan?.email ?: "") }
        var resp by remember { mutableStateOf(editingOrgan?.responsavel ?: "") }
        var obs by remember { mutableStateOf(editingOrgan?.observacoes ?: "") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingOrgan == null) "Novo Órgão" else "Editar Órgão") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Órgão") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = cnpj, onValueChange = { cnpj = it }, label = { Text("CNPJ") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = cid, onValueChange = { cid = it }, label = { Text("Cidade") }, modifier = Modifier.weight(1.3f))
                            OutlinedTextField(value = est, onValueChange = { est = it }, label = { Text("Estado (UF)") }, modifier = Modifier.weight(0.7f))
                        }
                    }
                    item { OutlinedTextField(value = portal, onValueChange = { portal = it }, label = { Text("Portal de Compras (Ex: comprasnet)") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = tel, onValueChange = { tel = it }, label = { Text("Telefone") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item { OutlinedTextField(value = resp, onValueChange = { resp = it }, label = { Text("Pregoeiro / Responsável") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = obs, onValueChange = { obs = it }, label = { Text("Observações") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nome.isBlank()) {
                            Toast.makeText(context, "Nome é obrigatório", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val organToSave = PublicOrgan(
                            id = editingOrgan?.id ?: 0,
                            nome = nome,
                            cnpj = cnpj,
                            cidade = cid,
                            estado = est,
                            portal = portal,
                            telefone = tel,
                            email = email,
                            responsavel = resp,
                            observacoes = obs
                        )
                        viewModel.saveOrgan(organToSave)
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

// ==========================================
// 4. DOCUMENTS / CERTIFICATE SCREEN
// ==========================================
@Composable
fun CertificatesScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val certificates by viewModel.certificates.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingCertificate by remember { mutableStateOf<Certificate?>(null) }

    val now = System.currentTimeMillis()
    val dayMillis = 24 * 3600 * 1000L

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingCertificate = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nova Certidão")
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
                text = "Habilitação & Certidões",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (certificates.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma certidão cadastrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(certificates) { cert ->
                        val isExpired = cert.dataVencimento < now
                        val daysLeft = ((cert.dataVencimento - now) / dayMillis).toInt()

                        val statusColor = when {
                            isExpired -> Color(0xFFC62828) // Expired Red
                            daysLeft < 30 -> Color(0xFFEF6C00) // Warning Orange
                            else -> Color(0xFF2E7D32) // Healthy Green
                        }

                        val statusText = when {
                            isExpired -> "Vencida"
                            daysLeft < 30 -> "Vence em $daysLeft dias"
                            else -> "Válida (Faltam $daysLeft dias)"
                        }

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingCertificate = cert
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
                                        Text(text = cert.nome, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Número: ${cert.numeroCertidao}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "Emissor: ${cert.orgaoEmissor}", fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            if (userRole == "Administrador") {
                                                viewModel.deleteCertificate(cert)
                                                Toast.makeText(context, "Certidão removida.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Ação restrita! Operador não pode remover certidões.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Emissão: ${formatDate(cert.dataEmissao)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "Vencimento: ${formatDate(cert.dataVencimento)}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = statusColor)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(statusColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.extraSmall)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = statusText,
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
        var nome by remember { mutableStateOf(editingCertificate?.nome ?: "") }
        var num by remember { mutableStateOf(editingCertificate?.numeroCertidao ?: "") }
        var emissor by remember { mutableStateOf(editingCertificate?.orgaoEmissor ?: "") }
        var emiDateStr by remember { mutableStateOf(if (editingCertificate != null) formatDate(editingCertificate!!.dataEmissao) else "") }
        var venDateStr by remember { mutableStateOf(if (editingCertificate != null) formatDate(editingCertificate!!.dataVencimento) else "") }
        var obs by remember { mutableStateOf(editingCertificate?.observacoes ?: "") }

        val defaultCertOptions = listOf(
            "CND Federal (Tributos)",
            "Regularidade de FGTS (CRF)",
            "Regularidade de INSS",
            "Certidão Negativa Tributos Estaduais",
            "Certidão Negativa Tributos Municipais",
            "Balanço Patrimonial",
            "Contrato Social / Ato Constitutivo",
            "Regularidade Trabalhista (CNDT)",
            "SICAF",
            "Outro Documento"
        )

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingCertificate == null) "Novo Documento/Certidão" else "Editar Documento/Certidão") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text("Selecione ou digite o nome do documento:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        ScrollableTabRow(
                            selectedTabIndex = defaultCertOptions.indexOf(nome).coerceAtLeast(0),
                            edgePadding = 0.dp,
                            divider = {}
                        ) {
                            defaultCertOptions.forEach { opt ->
                                Tab(
                                    selected = nome == opt,
                                    onClick = { nome = opt },
                                    text = { Text(opt, fontSize = 11.sp) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = nome,
                            onValueChange = { nome = it },
                            label = { Text("Nome Personalizado") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                    item { OutlinedTextField(value = num, onValueChange = { num = it }, label = { Text("Número da Certidão") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = emissor, onValueChange = { emissor = it }, label = { Text("Órgão Emissor") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        OutlinedTextField(
                            value = emiDateStr,
                            onValueChange = { emiDateStr = it },
                            label = { Text("Data Emissão (DD/MM/AAAA)") },
                            placeholder = { Text("Ex: 15/01/2026") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = venDateStr,
                            onValueChange = { venDateStr = it },
                            label = { Text("Data Vencimento (DD/MM/AAAA)") },
                            placeholder = { Text("Ex: 15/07/2026") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item { OutlinedTextField(value = obs, onValueChange = { obs = it }, label = { Text("Observações") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nome.isBlank()) {
                            Toast.makeText(context, "Nome é obrigatório", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val dateEmi = parseDateStr(emiDateStr) ?: System.currentTimeMillis()
                        val dateVen = parseDateStr(venDateStr) ?: (System.currentTimeMillis() + (90 * dayMillis))

                        val certToSave = Certificate(
                            id = editingCertificate?.id ?: 0,
                            nome = nome,
                            numeroCertidao = num,
                            dataEmissao = dateEmi,
                            dataVencimento = dateVen,
                            orgaoEmissor = emissor,
                            observacoes = obs
                        )
                        viewModel.saveCertificate(certToSave)
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

// Simple Date parsing util
fun parseDateStr(dateStr: String): Long? {
    return try {
        val parts = dateStr.split("/")
        if (parts.size != 3) return null
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, parts[0].toInt())
        cal.set(Calendar.MONTH, parts[1].toInt() - 1)
        cal.set(Calendar.YEAR, parts[2].toInt())
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.timeInMillis
    } catch (e: Exception) {
        null
    }
}
