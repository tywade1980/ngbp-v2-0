package com.constructionmanager.ui.screens.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.domain.model.Material
import com.constructionmanager.domain.model.MaterialCategory
import com.constructionmanager.domain.repository.MaterialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaterialsUiState(
    val isLoading: Boolean = true,
    val materials: List<Material> = emptyList(),
    val filteredMaterials: List<Material> = emptyList(),
    val categories: List<MaterialCategory> = emptyList(),
    val selectedCategory: MaterialCategory? = null,
    val error: String? = null
)

@HiltViewModel
class MaterialsViewModel @Inject constructor(
    private val materialRepository: MaterialRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MaterialsUiState())
    val uiState: StateFlow<MaterialsUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        // Combine search query and selected category to filter materials
        combine(
            _searchQuery,
            _uiState.map { it.selectedCategory },
            _uiState.map { it.materials }
        ) { query, category, materials ->
            filterMaterials(materials, query, category)
        }.onEach { filteredMaterials ->
            _uiState.value = _uiState.value.copy(filteredMaterials = filteredMaterials)
        }.launchIn(viewModelScope)
    }
    
    fun loadMaterials() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load categories
                val categories = materialRepository.getAllCategories()
                
                // Load all materials
                materialRepository.getAllActiveMaterials().collect { materials ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        materials = materials,
                        categories = categories,
                        filteredMaterials = filterMaterials(
                            materials, 
                            _searchQuery.value, 
                            _uiState.value.selectedCategory
                        )
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load materials"
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectCategory(category: MaterialCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    private fun filterMaterials(
        materials: List<Material>,
        searchQuery: String,
        selectedCategory: MaterialCategory?
    ): List<Material> {
        return materials.asSequence()
            .filter { material ->
                selectedCategory == null || material.category == selectedCategory
            }
            .filter { material ->
                if (searchQuery.isBlank()) {
                    true
                } else {
                    material.name.contains(searchQuery, ignoreCase = true) ||
                    material.description.contains(searchQuery, ignoreCase = true) ||
                    material.supplier.contains(searchQuery, ignoreCase = true) ||
                    material.subcategory.contains(searchQuery, ignoreCase = true)
                }
            }
            .sortedBy { it.name }
            .toList()
    }
}