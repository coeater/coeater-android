package com.coeater.android.webrtc.youtube

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.YoutubeSearchApi
import com.coeater.android.model.HTTPResult
import com.coeater.android.model.YoutubeResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CallYoutubeSearchViewModel (
    private val api: YoutubeSearchApi
) : ViewModel() {

    val result: MutableLiveData<YoutubeResult> by lazy {
        MutableLiveData<YoutubeResult>()
    }
    private var query: String? = null
    var nextToken: String? = null
    var prevToken: String? = null

    fun hasNext(): Boolean {
        if (nextToken == null) return false
        return true
    }

    fun hasPrev(): Boolean {
        if (prevToken == null) return false
        return true
    }

    fun hasQuery(): Boolean {
        if (query == null) return false
        return true
    }

    fun setToken(prev: String?, next: String?) {
        this.prevToken = prev
        this.nextToken = next
    }

    fun fetchNewResult(query: String) {
        this.query = query
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = getResult(query)) {
                is HTTPResult.Success<YoutubeResult> -> {
                    result.postValue(response.data)
                }
                is Error -> {
                }
            }
        }
    }

    fun fetchNextResult() {
        if (!hasNext() || !hasQuery())
            return
        val query = this.query
        val token = this.nextToken
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = getOtherResult(query!!, token!!)) {
                is HTTPResult.Success<YoutubeResult> -> {
                    result.postValue(response.data)
                }
                is Error -> {
                }
            }
        }
    }

    fun fetchPrevResult() {
        if (!hasPrev() || !hasQuery())
            return
        val query = this.query
        val token = this.prevToken
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = getOtherResult(query!!, token!!)) {
                is HTTPResult.Success<YoutubeResult> -> {
                    result.postValue(response.data)
                }
                is Error -> {
                }
            }
        }
    }

    private suspend fun getResult(query: String): HTTPResult<YoutubeResult> {
        return try {
            val response = api.search(query)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }

    private suspend fun getOtherResult(query: String, token: String): HTTPResult<YoutubeResult> {
        return try {
            val response = api.getOtherPage(query, token)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }
}