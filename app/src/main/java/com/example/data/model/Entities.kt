package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produtos")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigo: String = "",
    val descricao: String = "",
    val categoria: String = "",
    val marca: String = "",
    val unidade: String = "",
    val precoCompra: Double = 0.0,
    val precoVenda: Double = 0.0,
    val estoque: Int = 0,
    val fornecedorId: Int? = null,
    val fornecedorNome: String = "",
    val ncm: String = "",
    val cest: String = "",
    val observacoes: String = "",
    val codigoBarras: String = ""
)

@Entity(tableName = "fornecedores")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String = "",
    val cnpj: String = "",
    val inscricaoEstadual: String = "",
    val endereco: String = "",
    val telefone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val site: String = "",
    val contato: String = "",
    val produtosFornecidos: String = "",
    val prazoEntrega: String = "",
    val formaPagamento: String = ""
)

@Entity(tableName = "orgaos_publicos")
data class PublicOrgan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String = "",
    val cnpj: String = "",
    val cidade: String = "",
    val estado: String = "",
    val portal: String = "",
    val telefone: String = "",
    val email: String = "",
    val responsavel: String = "",
    val observacoes: String = ""
)

@Entity(tableName = "licitacoes")
data class Tender(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numero: String = "",
    val ano: String = "",
    val modalidade: String = "", // e.g. "Pregão Eletrônico", "Dispensa Eletrônica", "Concorrência"
    val objeto: String = "",
    val orgaoId: Int? = null,
    val orgaoNome: String = "",
    val dataHora: Long = 0L,
    val situacao: String = "Cadastrada", // "Cadastrada", "Em Andamento", "Vencida", "Perdida", "Suspensa"
    val valorEstimado: Double = 0.0,
    val valorVencedor: Double = 0.0,
    val responsavel: String = "",
    val observacoes: String = "",
    val isDispensa: Boolean = false,
    val produtosJson: String = "" // Simple JSON array or list of items involved
)

@Entity(tableName = "documentos_certidoes")
data class Certificate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String = "", // "CND Federal", "FGTS", "INSS", "Receita Estadual", "Receita Municipal", "Balanço", "Contrato Social", "SICAF", "Outros"
    val numeroCertidao: String = "",
    val dataEmissao: Long = 0L,
    val dataVencimento: Long = 0L,
    val orgaoEmissor: String = "",
    val observacoes: String = ""
)

@Entity(tableName = "financeiro_lancamentos")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: String = "Entrada", // "Entrada" or "Saída"
    val descricao: String = "",
    val valor: Double = 0.0,
    val data: Long = 0L,
    val categoria: String = "",
    val licitacaoId: Int? = null
)

@Entity(tableName = "agenda_compromissos")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String = "",
    val descricao: String = "",
    val dataHora: Long = 0L,
    val tipo: String = "Outro", // "Pregão", "Vencimento de Certidão", "Reunião", "Outro"
    val concluido: Boolean = false
)
