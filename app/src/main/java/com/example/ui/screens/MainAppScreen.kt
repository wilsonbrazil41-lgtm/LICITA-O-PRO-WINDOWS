package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    var currentSection by remember { mutableStateOf("Dashboard") }
    val isDark by viewModel.isDarkTheme.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Determine responsiveness based on screen width
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val menuItems = listOf(
        NavigationItem("Dashboard", Icons.Default.Dashboard, "Painel de controle"),
        NavigationItem("Licitações", Icons.Default.Assignment, "Oportunidades"),
        NavigationItem("Dispensas Eletrônicas", Icons.Default.FlashOn, "Dispensas"),
        NavigationItem("Pregões", Icons.Default.Gavel, "Pregões em disputa"),
        NavigationItem("Produtos", Icons.Default.Inventory, "Estoque e preços"),
        NavigationItem("Fornecedores", Icons.Default.LocalShipping, "Parceiros"),
        NavigationItem("Órgãos Públicos", Icons.Default.Business, "Clientes"),
        NavigationItem("Propostas", Icons.Default.ReceiptLong, "Proposta comercial"),
        NavigationItem("Certidões", Icons.Default.VerifiedUser, "Habilitação fiscal"),
        NavigationItem("Financeiro", Icons.Default.AccountBalanceWallet, "Fluxo financeiro"),
        NavigationItem("Agenda", Icons.Default.CalendarToday, "Calendário"),
        NavigationItem("Configurações", Icons.Default.Settings, "Configurações")
    )

    val drawerContent: @Composable () -> Unit = {
        ModalDrawerSheet(
            modifier = Modifier.width(280.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Licitação PRO",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "ERP de Compras Públicas",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                menuItems.forEach { item ->
                    val isSelected = currentSection == item.title
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                        selected = isSelected,
                        onClick = {
                            currentSection = item.title
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("menu_drawer_${item.title.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isWideScreen,
        drawerContent = drawerContent
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Adaptive Navigation Rail Sidebar for wider screens
            if (isWideScreen) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                "L-PRO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        menuItems.forEach { item ->
                            NavigationRailItem(
                                selected = currentSection == item.title,
                                onClick = { currentSection = item.title },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title, fontSize = 9.sp, maxLines = 1) },
                                modifier = Modifier.testTag("menu_rail_${item.title.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }
            }

            // Main screen canvas frame
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = currentSection,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Licitação PRO",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            if (!isWideScreen) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Abrir Menu")
                                }
                            }
                        },
                        actions = {
                            // Quick theme switch
                            IconButton(onClick = { viewModel.toggleTheme() }) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Alternar Tema"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        )
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Modular Routing engine
                    when (currentSection) {
                        "Dashboard" -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToSection = { currentSection = it }
                        )
                        "Licitações" -> BiddingProcessScreen(
                            viewModel = viewModel,
                            initialFilter = "Todas"
                        )
                        "Dispensas Eletrônicas" -> BiddingProcessScreen(
                            viewModel = viewModel,
                            initialFilter = "Dispensas"
                        )
                        "Pregões" -> BiddingProcessScreen(
                            viewModel = viewModel,
                            initialFilter = "Pregões"
                        )
                        "Produtos" -> ProductsScreen(viewModel = viewModel)
                        "Fornecedores" -> SuppliersScreen(viewModel = viewModel)
                        "Órgãos Públicos" -> OrgansScreen(viewModel = viewModel)
                        "Propostas" -> ProposalsBuilderScreen(viewModel = viewModel)
                        "Certidões" -> CertificatesScreen(viewModel = viewModel)
                        "Financeiro" -> FinanceScreen(viewModel = viewModel)
                        "Agenda" -> ScheduleScreen(viewModel = viewModel)
                        "Configurações" -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val description: String
)
