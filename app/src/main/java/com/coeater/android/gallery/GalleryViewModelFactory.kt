package com.coeater.android.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coeater.android.api.GalleryApi

class GalleryViewModelFactory (
    private val api: GalleryApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GalleryViewModel(api) as T
    }
}