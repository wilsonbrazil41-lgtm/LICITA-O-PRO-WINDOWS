package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class AppRepository(private val db: AppDatabase) {

    val products: Flow<List<Product>> = db.productDao().getAllProducts()
    val suppliers: Flow<List<Supplier>> = db.supplierDao().getAllSuppliers()
    val organs: Flow<List<PublicOrgan>> = db.publicOrganDao().getAllOrgans()
    val tenders: Flow<List<Tender>> = db.tenderDao().getAllTenders()
    val certificates: Flow<List<Certificate>> = db.certificateDao().getAllCertificates()
    val transactions: Flow<List<FinanceTransaction>> = db.financeTransactionDao().getAllTransactions()
    val appointments: Flow<List<Appointment>> = db.appointmentDao().getAllAppointments()

    // Products CRUD
    suspend fun insertProduct(product: Product) = db.productDao().insertProduct(product)
    suspend fun updateProduct(product: Product) = db.productDao().updateProduct(product)
    suspend fun deleteProduct(product: Product) = db.productDao().deleteProduct(product)
    suspend fun getProductById(id: Int) = db.productDao().getProductById(id)

    // Suppliers CRUD
    suspend fun insertSupplier(supplier: Supplier) = db.supplierDao().insertSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = db.supplierDao().updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = db.supplierDao().deleteSupplier(supplier)
    suspend fun getSupplierById(id: Int) = db.supplierDao().getSupplierById(id)

    // Public Organs CRUD
    suspend fun insertOrgan(organ: PublicOrgan) = db.publicOrganDao().insertOrgan(organ)
    suspend fun updateOrgan(organ: PublicOrgan) = db.publicOrganDao().updateOrgan(organ)
    suspend fun deleteOrgan(organ: PublicOrgan) = db.publicOrganDao().deleteOrgan(organ)
    suspend fun getOrganById(id: Int) = db.publicOrganDao().getOrganById(id)

    // Tenders CRUD
    suspend fun insertTender(tender: Tender) = db.tenderDao().insertTender(tender)
    suspend fun updateTender(tender: Tender) = db.tenderDao().updateTender(tender)
    suspend fun deleteTender(tender: Tender) = db.tenderDao().deleteTender(tender)
    suspend fun getTenderById(id: Int) = db.tenderDao().getTenderById(id)

    // Certificates CRUD
    suspend fun insertCertificate(certificate: Certificate) = db.certificateDao().insertCertificate(certificate)
    suspend fun updateCertificate(certificate: Certificate) = db.certificateDao().updateCertificate(certificate)
    suspend fun deleteCertificate(certificate: Certificate) = db.certificateDao().deleteCertificate(certificate)
    suspend fun getCertificateById(id: Int) = db.certificateDao().getCertificateById(id)

    // Transactions CRUD
    suspend fun insertTransaction(transaction: FinanceTransaction) = db.financeTransactionDao().insertTransaction(transaction)
    suspend fun updateTransaction(transaction: FinanceTransaction) = db.financeTransactionDao().updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: FinanceTransaction) = db.financeTransactionDao().deleteTransaction(transaction)
    suspend fun getTransactionById(id: Int) = db.financeTransactionDao().getTransactionById(id)

    // Appointments CRUD
    suspend fun insertAppointment(appointment: Appointment) = db.appointmentDao().insertAppointment(appointment)
    suspend fun updateAppointment(appointment: Appointment) = db.appointmentDao().updateAppointment(appointment)
    suspend fun deleteAppointment(appointment: Appointment) = db.appointmentDao().deleteAppointment(appointment)
    suspend fun getAppointmentById(id: Int) = db.appointmentDao().getAppointmentById(id)

    suspend fun insertDemoDataIfNeeded() {
        val currentProducts = products.first()
        if (currentProducts.isEmpty()) {
            // 1. Prepopulate Suppliers
            val s1 = Supplier(nome = "Distribuidora Tech Brasil", cnpj = "12.345.678/0001-90", inscricaoEstadual = "111.222.333.444", endereco = "Av. Paulista, 1000 - São Paulo/SP", telefone = "(11) 3232-4040", whatsapp = "(11) 98888-7777", email = "vendas@techbrasil.com.br", site = "www.techbrasil.com.br", contato = "Marcos Souza", produtosFornecidos = "Computadores, Notebooks, Servidores", prazoEntrega = "5 dias", formaPagamento = "Boleto 30 dias")
            val s2 = Supplier(nome = "Office Global S/A", cnpj = "98.765.432/0001-10", inscricaoEstadual = "555.666.777.888", endereco = "Rua do Ouvidor, 50 - Rio de Janeiro/RJ", telefone = "(21) 2500-1234", whatsapp = "(21) 97777-5555", email = "contato@officeglobal.com", site = "www.officeglobal.com", contato = "Ana Paula", produtosFornecidos = "Papelaria, Cadeiras, Mesas de escritório", prazoEntrega = "3 dias", formaPagamento = "Faturado 15/30 dias")
            val s3 = Supplier(nome = "ErgoMóveis Ltda", cnpj = "45.678.901/0001-23", inscricaoEstadual = "999.888.777.666", endereco = "Rua das Indústrias, 400 - Bento Gonçalves/RS", telefone = "(54) 3451-9999", whatsapp = "(54) 99111-2222", email = "comercial@ergomoveis.com", site = "www.ergomoveis.com", contato = "Roberto Silva", produtosFornecidos = "Móveis Ergonômicos, Divisórias", prazoEntrega = "10 dias", formaPagamento = "Sinal 50% + 50% na entrega")

            db.supplierDao().insertSupplier(s1)
            db.supplierDao().insertSupplier(s2)
            db.supplierDao().insertSupplier(s3)

            val savedSuppliers = db.supplierDao().getAllSuppliers().first()
            val s1Id = savedSuppliers.getOrNull(0)?.id
            val s2Id = savedSuppliers.getOrNull(1)?.id
            val s3Id = savedSuppliers.getOrNull(2)?.id

            // 2. Prepopulate Products
            val p1 = Product(codigo = "PRD-001", descricao = "Computador Desktop Core i5 16GB SSD 512GB", categoria = "Informática", marca = "TechMax", unidade = "UN", precoCompra = 2100.00, precoVenda = 3200.00, estoque = 45, fornecedorId = s1Id, fornecedorNome = s1.nome, ncm = "8471.30.12", cest = "21.031.00", observacoes = "Ideal para licitações de escritórios públicos.")
            val p2 = Product(codigo = "PRD-002", descricao = "Papel A4 Alcalino 75g Caixa com 5 Resmas", categoria = "Papelaria", marca = "GlobalPaper", unidade = "CX", precoCompra = 95.00, precoVenda = 140.00, estoque = 200, fornecedorId = s2Id, fornecedorNome = s2.nome, ncm = "4802.56.10", cest = "19.001.00", observacoes = "Produto certificado com selo ecológico.")
            val p3 = Product(codigo = "PRD-003", descricao = "Cadeira de Escritório Ergonômica NR-17", categoria = "Mobiliário", marca = "ErgoComfort", unidade = "UN", precoCompra = 450.00, precoVenda = 780.00, estoque = 60, fornecedorId = s3Id, fornecedorNome = s3.nome, ncm = "9401.30.90", cest = "28.040.00", observacoes = "Atende integralmente a norma NR-17 de ergonomia.")
            val p4 = Product(codigo = "PRD-004", descricao = "Notebook Core i7 16GB RAM 1TB SSD", categoria = "Informática", marca = "TechMax Pro", unidade = "UN", precoCompra = 3800.00, precoVenda = 5500.00, estoque = 15, fornecedorId = s1Id, fornecedorNome = s1.nome, ncm = "8471.30.19", cest = "21.031.00", observacoes = "Alto desempenho para órgãos de pesquisa.")

            db.productDao().insertProduct(p1)
            db.productDao().insertProduct(p2)
            db.productDao().insertProduct(p3)
            db.productDao().insertProduct(p4)

            // 3. Prepopulate Organs
            val o1 = PublicOrgan(nome = "Prefeitura Municipal de São Paulo", cnpj = "46.392.130/0001-18", cidade = "São Paulo", estado = "SP", portal = "compras.prefeitura.sp.gov.br", telefone = "(11) 3113-8000", email = "licitacoes@prefeitura.sp.gov.br", responsavel = "Dr. Marcelo Almeida", observacoes = "Volume alto de contratações de informática.")
            val o2 = PublicOrgan(nome = "Tribunal de Justiça do Rio Grande do Sul", cnpj = "92.802.798/0001-44", cidade = "Porto Alegre", estado = "RS", portal = "www.tjrs.jus.br", telefone = "(51) 3210-6000", email = "compras@tjrs.jus.br", responsavel = "Dra. Sandra Regina", observacoes = "Exige certificados rigorosos e prazos estreitos.")
            val o3 = PublicOrgan(nome = "Universidade Federal do Rio de Janeiro", cnpj = "33.663.683/0001-16", cidade = "Rio de Janeiro", estado = "RJ", portal = "www.compras.gov.br", telefone = "(21) 3938-1234", email = "licitacoes@ufrj.br", responsavel = "Roberto de Souza", observacoes = "Portal Federal (compras.gov.br).")

            db.publicOrganDao().insertOrgan(o1)
            db.publicOrganDao().insertOrgan(o2)
            db.publicOrganDao().insertOrgan(o3)

            val savedOrgans = db.publicOrganDao().getAllOrgans().first()
            val o1Id = savedOrgans.getOrNull(0)?.id
            val o2Id = savedOrgans.getOrNull(1)?.id
            val o3Id = savedOrgans.getOrNull(2)?.id

            // 4. Prepopulate Tenders (Licitações)
            val cal = Calendar.getInstance()
            
            // Bidding 1 (Pregão Eletrônico)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 30)
            val t1 = Tender(
                numero = "045", ano = "2026", modalidade = "Pregão Eletrônico",
                objeto = "Aquisição de computadores desktop e notebooks para secretarias",
                orgaoId = o1Id, orgaoNome = o1.nome,
                dataHora = cal.timeInMillis + (24 * 3600 * 1000 * 3), // 3 days in future
                situacao = "Em Andamento", valorEstimado = 150000.00, valorVencedor = 0.0,
                responsavel = "Carlos Roberto", observacoes = "Exige proposta assinada digitalmente e amostra de 1 item.",
                isDispensa = false,
                produtosJson = """[{"codigo":"PRD-001","quantidade":30,"precoUnitario":3200.00},{"codigo":"PRD-004","quantidade":10,"precoUnitario":5400.00}]"""
            )

            // Bidding 2 (Dispensa Eletrônica)
            cal.set(Calendar.HOUR_OF_DAY, 14)
            val t2 = Tender(
                numero = "012", ano = "2026", modalidade = "Dispensa Eletrônica",
                objeto = "Compra emergencial de papel sulfite A4 para o TJRS",
                orgaoId = o2Id, orgaoNome = o2.nome,
                dataHora = cal.timeInMillis + (24 * 3600 * 1000 * 1), // 1 day in future
                situacao = "Cadastrada", valorEstimado = 28000.00, valorVencedor = 0.0,
                responsavel = "Fernanda Costa", observacoes = "Portal TJRS. Entrega em lote único.",
                isDispensa = true,
                produtosJson = """[{"codigo":"PRD-002","quantidade":200,"precoUnitario":140.00}]"""
            )

            // Bidding 3 (Vencida - Ganhamos!)
            cal.set(Calendar.HOUR_OF_DAY, 10)
            val t3 = Tender(
                numero = "008", ano = "2026", modalidade = "Pregão Eletrônico",
                objeto = "Mobiliário ergonômico para reestruturação das salas de aula",
                orgaoId = o3Id, orgaoNome = o3.nome,
                dataHora = cal.timeInMillis - (24 * 3600 * 1000 * 5), // 5 days in past
                situacao = "Vencida", valorEstimado = 46800.00, valorVencedor = 45000.00,
                responsavel = "Carlos Roberto", observacoes = "Vencemos todos os itens com margem de 35% de lucro.",
                isDispensa = false,
                produtosJson = """[{"codigo":"PRD-003","quantidade":60,"precoUnitario":750.00}]"""
            )

            // Bidding 4 (Perdida)
            val t4 = Tender(
                numero = "002", ano = "2026", modalidade = "Pregão Eletrônico",
                objeto = "Notebooks de alta performance para laboratório de engenharia",
                orgaoId = o3Id, orgaoNome = o3.nome,
                dataHora = cal.timeInMillis - (24 * 3600 * 1000 * 15), // 15 days in past
                situacao = "Perdida", valorEstimado = 55000.00, valorVencedor = 51000.00,
                responsavel = "Carlos Roberto", observacoes = "Concorrente cobriu nossa oferta com preço inexequível.",
                isDispensa = false,
                produtosJson = """[{"codigo":"PRD-004","quantidade":10,"precoUnitario":5500.00}]"""
            )

            db.tenderDao().insertTender(t1)
            db.tenderDao().insertTender(t2)
            db.tenderDao().insertTender(t3)
            db.tenderDao().insertTender(t4)

            // 5. Prepopulate Certificates (Documentos)
            val day = 24 * 3600 * 1000L
            val cert1 = Certificate(nome = "Certidão Conjunta de Débitos Relativos a Créditos Tributários Federais", numeroCertidao = "CND-RFB-98442", dataEmissao = cal.timeInMillis - (day * 30), dataVencimento = cal.timeInMillis + (day * 15), orgaoEmissor = "Receita Federal do Brasil", observacoes = "Controlar vencimento próximo!")
            val cert2 = Certificate(nome = "Certificado de Regularidade do FGTS (CRF)", numeroCertidao = "FGTS-87425", dataEmissao = cal.timeInMillis - (day * 10), dataVencimento = cal.timeInMillis + (day * 20), orgaoEmissor = "Caixa Econômica Federal", observacoes = "Regularidade em dia.")
            val cert3 = Certificate(nome = "Certidão de Débito de FGTS e INSS Integrado", numeroCertidao = "INSS-77631", dataEmissao = cal.timeInMillis - (day * 120), dataVencimento = cal.timeInMillis - (day * 5), orgaoEmissor = "Ministério do Trabalho e Previdência", observacoes = "CERTIDÃO VENCIDA! Necessário renovar imediatamente.")
            val cert4 = Certificate(nome = "SICAF - Certificado de Registro Cadastral", numeroCertidao = "CRC-SICAF-10928", dataEmissao = cal.timeInMillis - (day * 150), dataVencimento = cal.timeInMillis + (day * 210), orgaoEmissor = "Portal de Compras Governamentais", observacoes = "Renovação anual requer balanço patrimonial.")
            val cert5 = Certificate(nome = "Contrato Social Atualizado", numeroCertidao = "JUCESP-3522-A", dataEmissao = cal.timeInMillis - (day * 365), dataVencimento = cal.timeInMillis + (day * 3650), orgaoEmissor = "Junta Comercial do Estado", observacoes = "Documento constitutivo permanente.")

            db.certificateDao().insertCertificate(cert1)
            db.certificateDao().insertCertificate(cert2)
            db.certificateDao().insertCertificate(cert3)
            db.certificateDao().insertCertificate(cert4)
            db.certificateDao().insertCertificate(cert5)

            // 6. Prepopulate Financial Transactions
            val ft1 = FinanceTransaction(tipo = "Entrada", descricao = "Recebimento Pregão 008/2026 - Móveis UFRJ", valor = 45000.00, data = cal.timeInMillis - (day * 1), categoria = "Venda de Licitação", licitacaoId = 3)
            val ft2 = FinanceTransaction(tipo = "Saída", descricao = "Compra de suprimentos - ErgoMóveis Ltda", valor = 27000.00, data = cal.timeInMillis - (day * 3), categoria = "Compras Fornecedores")
            val ft3 = FinanceTransaction(tipo = "Saída", descricao = "Pagamento de impostos Simples Nacional DAS", valor = 1800.00, data = cal.timeInMillis - (day * 6), categoria = "Impostos")
            val ft4 = FinanceTransaction(tipo = "Entrada", descricao = "Adiantamento Contrato Prefeitura SP", valor = 30000.00, data = cal.timeInMillis - (day * 8), categoria = "Adiantamento")
            val ft5 = FinanceTransaction(tipo = "Saída", descricao = "Despesas de envio e logística de entrega", valor = 1200.00, data = cal.timeInMillis - (day * 10), categoria = "Logística/Frete")

            db.financeTransactionDao().insertTransaction(ft1)
            db.financeTransactionDao().insertTransaction(ft2)
            db.financeTransactionDao().insertTransaction(ft3)
            db.financeTransactionDao().insertTransaction(ft4)
            db.financeTransactionDao().insertTransaction(ft5)

            // 7. Prepopulate Appointments (Agenda)
            val app1 = Appointment(titulo = "Abertura do Pregão Eletrônico 045/2026 - Prefeitura SP", descricao = "Batalha de lances para notebooks e desktops. Responsável: Carlos Roberto.", dataHora = cal.timeInMillis + (day * 3) + (9 * 3600 * 1000L) + (30 * 60 * 1000L), tipo = "Pregão", concluido = false)
            val app2 = Appointment(titulo = "Fim do envio de lances - Dispensa Eletrônica 012/2026", descricao = "Acompanhamento do fechamento de lances no portal TJRS para papel A4.", dataHora = cal.timeInMillis + (day * 1) + (14 * 3600 * 1000L), tipo = "Pregão", concluido = false)
            val app3 = Appointment(titulo = "Renovar Certidão INSS Vencida", descricao = "Fazer o pedido da guia atualizada e protocolar no portal CRF.", dataHora = cal.timeInMillis + (day * 2), tipo = "Vencimento de Certidão", concluido = false)
            val app4 = Appointment(titulo = "Reunião de Alinhamento de Margem de Lucro", descricao = "Alinhamento com equipe de vendas sobre margens de descontos para Pregão 045.", dataHora = cal.timeInMillis - (day * 2) + (15 * 3600 * 1000L), tipo = "Reunião", concluido = true)

            db.appointmentDao().insertAppointment(app1)
            db.appointmentDao().insertAppointment(app2)
            db.appointmentDao().insertAppointment(app3)
            db.appointmentDao().insertAppointment(app4)
        }
    }
}
