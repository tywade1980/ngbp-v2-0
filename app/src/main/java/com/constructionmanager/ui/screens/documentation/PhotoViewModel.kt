package com.constructionmanager.ui.screens.documentation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.data.cloud.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    enum class UploadStatus { UPLOADING, UPLOADED, FAILED }

    data class CapturedPhoto(
        val id: String,
        val uri: Uri,
        val category: PhotoCategory,
        val status: UploadStatus,
        val url: String? = null
    )

    private val _photos = MutableStateFlow<List<CapturedPhoto>>(emptyList())
    val photos: StateFlow<List<CapturedPhoto>> = _photos.asStateFlow()

    private var pendingCaptureUri: Uri? = null

    /** Creates a FileProvider URI for the camera to write into, remembered for the result callback. */
    fun createCaptureUri(context: Context): Uri {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        pendingCaptureUri = uri
        return uri
    }

    fun onCaptureResult(success: Boolean, projectId: String, category: PhotoCategory) {
        val uri = pendingCaptureUri
        pendingCaptureUri = null
        if (success && uri != null) addAndUpload(uri, projectId, category)
    }

    fun onPicked(uri: Uri?, projectId: String, category: PhotoCategory) {
        if (uri != null) addAndUpload(uri, projectId, category)
    }

    private fun addAndUpload(uri: Uri, projectId: String, category: PhotoCategory) {
        val id = UUID.randomUUID().toString()
        _photos.update { listOf(CapturedPhoto(id, uri, category, UploadStatus.UPLOADING)) + it }
        viewModelScope.launch {
            val result = photoRepository.upload(uri, projectId, category.name)
            _photos.update { list ->
                list.map {
                    if (it.id == id) {
                        it.copy(
                            status = if (result.isSuccess) UploadStatus.UPLOADED else UploadStatus.FAILED,
                            url = result.getOrNull()
                        )
                    } else it
                }
            }
        }
    }
}
