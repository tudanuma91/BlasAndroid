package com.v3.basis.blas.ui.item.item_image

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.v3.basis.blas.blasclass.rest.BlasRestImage
import com.v3.basis.blas.ui.item.item_image.model.ItemImageModel
import org.json.JSONObject

class ItemImageViewModel : ViewModel() {

    val deleteObserver: MutableLiveData<Int> = MutableLiveData()
    val rightRotate: MutableLiveData<Int> = MutableLiveData()
    val leftRotate: MutableLiveData<Int> = MutableLiveData()
    val fetchSuccess: MutableLiveData<ItemImageModel> = MutableLiveData()
    val fetchError: MutableLiveData<Int> = MutableLiveData()

    private lateinit var images: ItemImageModel

    fun fetch(token: String?, itemId: String?) {

        val payload = mapOf("token" to token, "item_id" to itemId)
        BlasRestImage("download", payload, ::success, ::error).execute()
    }

    fun deleteClick(item: ItemImageCellItem) {
        item.empty.set(true)
    }

    fun rightRotate(id: String) {

    }

    fun leftRotate(id: String) {

    }

    fun selectFile(id: String) {

    }

    fun upload(bitmap: Bitmap) {

    }

    private fun success(json: JSONObject) {

        Log.d("fetch image", json.toString(4))
        images = Gson().fromJson(json.toString(), ItemImageModel::class.java)
        fetchSuccess.postValue(images)
    }

    /**
     * フィールド取得失敗時
     */
    private fun error(errorCode: Int) {
        fetchError.postValue(errorCode)
    }
}
