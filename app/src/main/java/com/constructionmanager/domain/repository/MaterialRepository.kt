package com.constructionmanager.domain.repository

import com.constructionmanager.domain.model.Material
import com.constructionmanager.domain.model.MaterialCategory
import kotlinx.coroutines.flow.Flow

interface MaterialRepository {
    
    fun getAllActiveMaterials(): Flow<List<Material>>
    
    fun getMaterialsByCategory(category: MaterialCategory): Flow<List<Material>>
    
    suspend fun getMaterialById(materialId: String): Material?
    
    fun searchMaterials(searchQuery: String): Flow<List<Material>>
    
    fun getMaterialsBySupplier(supplier: String): Flow<List<Material>>
    
    suspend fun insertMaterial(material: Material)
    
    suspend fun insertMaterials(materials: List<Material>)
    
    suspend fun updateMaterial(material: Material)
    
    suspend fun deactivateMaterial(materialId: String)
    
    suspend fun getAllCategories(): List<MaterialCategory>
    
    suspend fun getAllSuppliers(): List<String>
    
    suspend fun initializeSeedData()
}