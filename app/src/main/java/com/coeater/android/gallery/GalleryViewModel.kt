package com.coeater.android.gallery

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coeater.android.api.GalleryApi
import com.coeater.android.model.User

class GalleryViewModel (
    private val api: GalleryApi
) : ViewModel() {

    val gallery: MutableLiveData<List<User>> by lazy {
        MutableLiveData<List<User>>()
    }

    fun fetchGallery() {
        //TODO
    }
}