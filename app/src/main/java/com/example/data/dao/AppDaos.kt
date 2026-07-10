package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM produtos ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM produtos WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM fornecedores ORDER BY nome ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM fornecedores WHERE id = :id LIMIT 1")
    suspend fun getSupplierById(id: Int): Supplier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)
}

@Dao
interface PublicOrganDao {
    @Query("SELECT * FROM orgaos_publicos ORDER BY nome ASC")
    fun getAllOrgans(): Flow<List<PublicOrgan>>

    @Query("SELECT * FROM orgaos_publicos WHERE id = :id LIMIT 1")
    suspend fun getOrganById(id: Int): PublicOrgan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrgan(organ: PublicOrgan)

    @Update
    suspend fun updateOrgan(organ: PublicOrgan)

    @Delete
    suspend fun deleteOrgan(organ: PublicOrgan)
}

@Dao
interface TenderDao {
    @Query("SELECT * FROM licitacoes ORDER BY dataHora DESC")
    fun getAllTenders(): Flow<List<Tender>>

    @Query("SELECT * FROM licitacoes WHERE isDispensa = 1 ORDER BY dataHora DESC")
    fun getAllDispensas(): Flow<List<Tender>>

    @Query("SELECT * FROM licitacoes WHERE isDispensa = 0 ORDER BY dataHora DESC")
    fun getAllLicitacoes(): Flow<List<Tender>>

    @Query("SELECT * FROM licitacoes WHERE id = :id LIMIT 1")
    suspend fun getTenderById(id: Int): Tender?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTender(tender: Tender)

    @Update
    suspend fun updateTender(tender: Tender)

    @Delete
    suspend fun deleteTender(tender: Tender)
}

@Dao
interface CertificateDao {
    @Query("SELECT * FROM documentos_certidoes ORDER BY dataVencimento ASC")
    fun getAllCertificates(): Flow<List<Certificate>>

    @Query("SELECT * FROM documentos_certidoes WHERE id = :id LIMIT 1")
    suspend fun getCertificateById(id: Int): Certificate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: Certificate)

    @Update
    suspend fun updateCertificate(certificate: Certificate)

    @Delete
    suspend fun deleteCertificate(certificate: Certificate)
}

@Dao
interface FinanceTransactionDao {
    @Query("SELECT * FROM financeiro_lancamentos ORDER BY data DESC")
    fun getAllTransactions(): Flow<List<FinanceTransaction>>

    @Query("SELECT * FROM financeiro_lancamentos WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): FinanceTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinanceTransaction)

    @Update
    suspend fun updateTransaction(transaction: FinanceTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: FinanceTransaction)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM agenda_compromissos ORDER BY dataHora ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM agenda_compromissos WHERE id = :id LIMIT 1")
    suspend fun getAppointmentById(id: Int): Appointment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment)

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)
}
