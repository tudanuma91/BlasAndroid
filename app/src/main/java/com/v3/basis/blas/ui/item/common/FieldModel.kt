package com.v3.basis.blas.ui.item.common

import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.databinding.InputField5Binding


open class FieldModel(
    val context: Context,
    val layoutInflater: LayoutInflater,
    val fieldNumber: Int,
    val field: LdbFieldRecord){

    val validationMsg: ObservableField<String> = ObservableField("")
    val text: ObservableField<String> = ObservableField("")
    val memo = ObservableField("")

    //親が子供のフィールドを持つ
    protected val childFieldList = mutableListOf<FieldModel>()
    public var parentField:FieldModel? = null

    //文字列を返却する
    open fun convertToString(): String? {
        return text.get()
    }

    //自分自身に値を設定する
    open fun setValue(value:String?) {

        text.set(value)
    }

    open fun addChildField(field:FieldModel) {
        //子供のフィールドを追加する
        childFieldList.add(field)
    }

    open fun addParentField(field:FieldModel) {
        parentField = field
    }


    //子供に親のデータを通知する
    open fun notifyedFromParent(value:String) {
        //継承先で実体を書く前提
    }

    //値不正のチェック
    open fun validate():Boolean{

        // 必須チェック
        if( 1 == field.essential && "" == text.get() ) {
            val msg = field.name + "を入力してください"
            this.validationMsg.set(msg)
            return false
        }

        // 重複チェック
        if( 1 == field.unique_chk ) {
            val itemsController = ItemsController(context, field.project_id.toString())

            if( !itemsController.checkUnique(field,text.get()) ) {
                val msg = text.get() + "は既に登録されています"
                this.validationMsg.set(msg)
                return false
            }
        }


        return true
    }

    open fun parentValidate():Boolean {
        return true
    }


    fun validateKenpinRendou() : Boolean {

        val itemsController = ItemsController(context, field.project_id.toString())

        try {
            itemsController.qrCodeCheck( text.get() )
        }
        catch ( ex: ItemsController.ItemCheckException) {
            val msg = ex.message
            this.validationMsg.set(msg)
            return false
        }

        return true
    }

    fun validateTekkyoRendou() : Boolean {

        val itemsController = ItemsController(context, field.project_id.toString())

        try {
            itemsController.rmQrCodeCheck(text.get())
        }
        catch ( ex: ItemsController.ItemCheckException ) {
            val msg = ex.message
            this.validationMsg.set(msg)
            return false
        }

        return true
    }
}


