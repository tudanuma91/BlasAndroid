package com.v3.basis.blas.ui.fixture.fixture_config

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.gms.vision.barcode.Barcode
import com.v3.basis.blas.R

/**
 * 機器管理 設定画面のViewModel
 */
class FixtureConfigViewModel(handle: SavedStateHandle) : ViewModel() {
    //シングル読み取りのラジオボタンを監視する
    val isCheckedSingle: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    //マルチ読み取りのラジオボタンを監視する
    val isCheckedMulti: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    //監視するのは、リストに登録するBarcodeItemのuseFlgだけ。リスト自体は監視しない。
    val listItems = mutableListOf<BarcodeItem>()


    /**
     * 初期値の設定
     */
    private fun initBarcodeList() {
        //Switchに使うuseFlgだけ変更の監視対象とする
        var item = BarcodeItem("QRCode",  MutableLiveData<Boolean>(false), R.drawable.qrcode)
        listItems.add(item)

        item = BarcodeItem("EAN", MutableLiveData<Boolean>(true), R.drawable.ean)
        listItems.add(item)

        item = BarcodeItem("CODE",  MutableLiveData<Boolean>(true), R.drawable.code128)
        listItems.add(item)

        item = BarcodeItem("ITF", MutableLiveData<Boolean>(true), R.drawable.itf)
        listItems.add(item)

        item = BarcodeItem( "UPC",  MutableLiveData<Boolean>(false), R.drawable.upca)
        listItems.add(item)

        item = BarcodeItem("PDF417", MutableLiveData<Boolean>(false), R.drawable.pdf417)
        listItems.add(item)

        item = BarcodeItem("AZTEC",  MutableLiveData<Boolean>(false), R.drawable.aztec)
        listItems.add(item)

        item = BarcodeItem("CODABAR",  MutableLiveData<Boolean>(true), R.drawable.codabar)
        listItems.add(item)
    }


    /**
     * パラメーターをプレファレンスから復元する。
     * viewモデル作成後、即座に本APIを呼び出すこと。
     */
    public fun loadViewModel(context: Context) {
        var prefs = context?.getSharedPreferences(context.getString(R.string.BlasAppConfig), Context.MODE_PRIVATE)

       if(prefs != null) {
            //カメラの選択
            isCheckedSingle.value = prefs.getBoolean("singleCamera",true)
            isCheckedMulti.value = prefs.getBoolean("multiCamera",false)

            var check = prefs.getBoolean("QRCode",false)
            var item = BarcodeItem("QRCode",  MutableLiveData<Boolean>(check), R.drawable.qrcode)
            listItems.add(item)

            check = prefs.getBoolean("EAN",true)
            item = BarcodeItem("EAN", MutableLiveData<Boolean>(check), R.drawable.ean)
            listItems.add(item)

            check = prefs.getBoolean("CODE",true)
            item = BarcodeItem("CODE",  MutableLiveData<Boolean>(check), R.drawable.code128)
            listItems.add(item)

            item = BarcodeItem("ITF", MutableLiveData<Boolean>(check), R.drawable.itf)
            listItems.add(item)

            check = prefs.getBoolean("UPC",false)
            item = BarcodeItem( "UPC",  MutableLiveData<Boolean>(check), R.drawable.upca)
            listItems.add(item)

            check = prefs.getBoolean("PDF417",false)
            item = BarcodeItem("PDF417", MutableLiveData<Boolean>(check), R.drawable.pdf417)
            listItems.add(item)

            check = prefs.getBoolean("AZTEC",false)
            item = BarcodeItem("AZTEC",  MutableLiveData<Boolean>(check), R.drawable.aztec)
            listItems.add(item)

           check = prefs.getBoolean("CODABAR",false) //NW-7
           item = BarcodeItem("CODABAR",  MutableLiveData<Boolean>(check), R.drawable.codabar)
           listItems.add(item)

       }
        else {
           isCheckedSingle.value = false
           isCheckedMulti.value = false
           initBarcodeList()
       }
    }


    /**
     * パラメーターをプレファレンスに保存する
     */
    public fun saveViewModel(context: Context) {
        var prefs = context?.getSharedPreferences(context.getString(R.string.BlasAppConfig), Context.MODE_PRIVATE)
        var editor = prefs?.edit()

        isCheckedSingle.value?.let {
            editor?.putBoolean("singleCamera", it)
        }

        isCheckedMulti.value?.let {
            editor?.putBoolean("multiCamera", it)
        }

        listItems.forEach {barCode->
            barCode.useFlg.value?.let {useFlg->
                var name = barCode.name
                editor?.putBoolean(name, useFlg)
            }
        }

        editor?.apply()
    }
}


