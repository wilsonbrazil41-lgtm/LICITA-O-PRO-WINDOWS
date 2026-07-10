package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    
    // States
    val products: StateFlow<List<Product>>
    val suppliers: StateFlow<List<Supplier>>
    val organs: StateFlow<List<PublicOrgan>>
    val tenders: StateFlow<List<Tender>>
    val certificates: StateFlow<List<Certificate>>
    val transactions: StateFlow<List<FinanceTransaction>>
    val appointments: StateFlow<List<Appointment>>

    // User State
    private val _userRole = MutableStateFlow("Administrador")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _currentUser = MutableStateFlow("Diretoria PRO")
    val currentUser: StateFlow<String> = _currentUser.asStateFlow()

    // UI Configuration State
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Global Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Security/Audit Log State
    private val _securityLogs = MutableStateFlow<List<String>>(emptyList())
    val securityLogs: StateFlow<List<String>> = _securityLogs.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database)

        // Bind data reactive flows
        products = repository.products.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        suppliers = repository.suppliers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        organs = repository.organs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        tenders = repository.tenders.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        certificates = repository.certificates.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        appointments = repository.appointments.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Initial launch checks
        viewModelScope.launch {
            repository.insertDemoDataIfNeeded()
            logAction("Sistema iniciado. Banco de dados carregado com sucesso.")
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        logAction("Tema visual alterado para: ${if (_isDarkTheme.value) "Escuro" else "Claro"}")
    }

    fun setUserRole(role: String) {
        _userRole.value = role
        logAction("Permissão de usuário alterada para: $role")
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Logging helper
    fun logAction(action: String) {
        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "[$timestamp] [${_userRole.value}] $action"
        _securityLogs.update { listOf(logEntry) + it }
    }

    // CRUD operations with safety logging
    fun saveProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0) {
                repository.insertProduct(product)
                logAction("Produto cadastrado: ${product.codigo} - ${product.descricao}")
            } else {
                repository.updateProduct(product)
                logAction("Produto atualizado: ${product.codigo}")
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            logAction("Produto excluído: ${product.codigo}")
        }
    }

    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            if (supplier.id == 0) {
                repository.insertSupplier(supplier)
                logAction("Fornecedor cadastrado: ${supplier.nome}")
            } else {
                repository.updateSupplier(supplier)
                logAction("Fornecedor atualizado: ${supplier.nome}")
            }
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
            logAction("Fornecedor excluído: ${supplier.nome}")
        }
    }

    fun saveOrgan(organ: PublicOrgan) {
        viewModelScope.launch {
            if (organ.id == 0) {
                repository.insertOrgan(organ)
                logAction("Órgão Público cadastrado: ${organ.nome}")
            } else {
                repository.updateOrgan(organ)
                logAction("Órgão Público atualizado: ${organ.nome}")
            }
        }
    }

    fun deleteOrgan(organ: PublicOrgan) {
        viewModelScope.launch {
            repository.deleteOrgan(organ)
            logAction("Órgão Público excluído: ${organ.nome}")
        }
    }

    fun saveTender(tender: Tender) {
        viewModelScope.launch {
            if (tender.id == 0) {
                repository.insertTender(tender)
                logAction("Licitação cadastrada: Nº ${tender.numero}/${tender.ano} - ${tender.modalidade}")
                // Add an event to calendar automatically if we add a tender!
                if (tender.dataHora > 0) {
                    repository.insertAppointment(
                        Appointment(
                            titulo = "Abertura ${tender.modalidade} ${tender.numero}/${tender.ano}",
                            descricao = tender.objeto,
                            dataHora = tender.dataHora,
                            tipo = "Pregão"
                        )
                    )
                }
            } else {
                repository.updateTender(tender)
                logAction("Licitação atualizada: Nº ${tender.numero}/${tender.ano}")
            }
        }
    }

    fun deleteTender(tender: Tender) {
        viewModelScope.launch {
            repository.deleteTender(tender)
            logAction("Licitação excluída: Nº ${tender.numero}/${tender.ano}")
        }
    }

    fun saveCertificate(certificate: Certificate) {
        viewModelScope.launch {
            if (certificate.id == 0) {
                repository.insertCertificate(certificate)
                logAction("Documento/Certidão cadastrada: ${certificate.nome}")
                // Add reminder
                repository.insertAppointment(
                    Appointment(
                        titulo = "Vencimento: ${certificate.nome}",
                        descricao = "Exige renovação. Certificado Nº ${certificate.numeroCertidao}",
                        dataHora = certificate.dataVencimento,
                        tipo = "Vencimento de Certidão"
                    )
                )
            } else {
                repository.updateCertificate(certificate)
                logAction("Documento/Certidão atualizada: ${certificate.nome}")
            }
        }
    }

    fun deleteCertificate(certificate: Certificate) {
        viewModelScope.launch {
            repository.deleteCertificate(certificate)
            logAction("Documento/Certidão excluída: ${certificate.nome}")
        }
    }

    fun saveTransaction(transaction: FinanceTransaction) {
        viewModelScope.launch {
            if (transaction.id == 0) {
                repository.insertTransaction(transaction)
                logAction("Lançamento Financeiro cadastrado: [${transaction.tipo}] ${transaction.descricao} - R$ ${String.format("%.2f", transaction.valor)}")
            } else {
                repository.updateTransaction(transaction)
                logAction("Lançamento Financeiro atualizado: ${transaction.descricao}")
            }
        }
    }

    fun deleteTransaction(transaction: FinanceTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            logAction("Lançamento Financeiro excluído: ${transaction.descricao}")
        }
    }

    fun saveAppointment(appointment: Appointment) {
        viewModelScope.launch {
            if (appointment.id == 0) {
                repository.insertAppointment(appointment)
                logAction("Compromisso agendado: ${appointment.titulo}")
            } else {
                repository.updateAppointment(appointment)
                logAction("Compromisso atualizado: ${appointment.titulo}")
            }
        }
    }

    fun toggleAppointmentConcluded(appointment: Appointment) {
        viewModelScope.launch {
            val updated = appointment.copy(concluido = !appointment.concluido)
            repository.updateAppointment(updated)
            logAction("Compromisso [${appointment.titulo}] alterado para: ${if (updated.concluido) "Concluído" else "Em aberto"}")
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
            logAction("Compromisso removido: ${appointment.titulo}")
        }
    }

    // Simulated Spreadsheets Import
    fun importProductsFromExcel(onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            val productsToImport = listOf(
                Product(codigo = "IMP-101", descricao = "Papel Sulfite A3 Caixa", categoria = "Papelaria", marca = "Chamequinho", unidade = "CX", precoCompra = 120.0, precoVenda = 180.0, estoque = 50, ncm = "4802.56.10", cest = "19.001.00", observacoes = "Importado via planilha"),
                Product(codigo = "IMP-102", descricao = "Mouse Sem Fio Optico", categoria = "Informática", marca = "Logitech", unidade = "UN", precoCompra = 35.0, precoVenda = 65.0, estoque = 150, ncm = "8471.60.53", cest = "21.031.00", observacoes = "Importado via planilha"),
                Product(codigo = "IMP-103", descricao = "Teclado USB Gamer Pro", categoria = "Informática", marca = "Razer", unidade = "UN", precoCompra = 180.0, precoVenda = 320.0, estoque = 25, ncm = "8471.60.52", cest = "21.031.00", observacoes = "Importado via planilha"),
                Product(codigo = "IMP-104", descricao = "Mesa Diretora em L de Madeira", categoria = "Mobiliário", marca = "Móveis Sul", unidade = "UN", precoCompra = 850.0, precoVenda = 1400.0, estoque = 10, ncm = "9403.30.00", cest = "28.040.00", observacoes = "Importado via planilha")
            )
            for (p in productsToImport) {
                repository.insertProduct(p)
            }
            logAction("Sucesso: Importados 4 produtos via planilha Excel.")
            onSuccess(4)
        }
    }

    // Backups Simulated Operations
    fun performBackup(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "LICITACAO_PRO_BACKUP_$dateFormat.db"
            logAction("Backup automático realizado com sucesso: $backupFileName")
            onSuccess(backupFileName)
        }
    }

    fun restoreBackup(backupName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            logAction("Restauração de banco de dados executada: $backupName")
            onSuccess()
        }
    }
}
