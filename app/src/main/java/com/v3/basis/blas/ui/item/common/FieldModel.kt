package com.v3.basis.blas.ui.item.common

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.databinding.InputField5Binding
import org.json.JSONObject


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
    public var parentFieldList = mutableListOf<FieldModel>()

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
        parentFieldList.add(field)
    }


    //子供に親のデータを通知する
    open fun notifyedFromParent(value:String) {
        //継承先で実体を書く前提
    }

    //値不正のチェック
    open fun validate(itemId:String):Boolean{

        // 必須チェック
        if( 1 == field.essential && "" == convertToString() ) {
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
}

open class FieldQRBaseModel(
    context: Context,
    layoutInflater: LayoutInflater,
    fieldNumber: Int,
    field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

    fun validateKenpinRendou(itemId:String) : Boolean {
        val itemsController = ItemsController(context, field.project_id.toString())

        //一度設置済みになったものを再度保存すると「すでに設置済み」となり、保存できない問題の対処
        try{
            val record = itemsController.findByItemId(itemId)
            if(!record.isNullOrEmpty()) {
                val serialFld = "fld${field.col}"
                var oldSerial = ""

                if(field.type.toString() == FieldType.QR_CODE_WITH_CHECK ||
                   field.type.toString() == FieldType.BAR_CODE_WITH_CHECK) {
                    //連動系はウソjson形式で来る
                    if(!record[serialFld].isNullOrEmpty()) {
                        val durtyText = record[serialFld]?.replace("\\", "")
                        val json = JSONObject(durtyText)
                        oldSerial = json.getString("value")
                    }

                }
                else {
                    if(!record[serialFld].isNullOrEmpty()) {
                        oldSerial = record[serialFld]!!
                    }
                }

                if(oldSerial == text.get()) {
                    //変更されていない場合はOK
                    return true
                }
            }
        }
        catch (e:Exception) {
            this.validationMsg.set("予期せぬエラーが発生しました。システム管理者に連絡してください")
            BlasLog.trace("E", "findByItemId error", e)
            return false
        }


        try {
            itemsController.qrCodeCheck( text.get() )
        }
        catch ( ex: ItemsController.ItemCheckException) {
            val msg = ex.message
            this.validationMsg.set(msg)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    fun validateTekkyoRendou(itemId:String) : Boolean {

        val itemsController = ItemsController(context, field.project_id.toString())
        //一度設置済みになったものを再度保存すると「すでに設置済み」となり、保存できない問題の対処
        try{
            val record = itemsController.findByItemId(itemId)
            if(!record.isNullOrEmpty()) {
                val serialFld = "fld${field.col}"
                val oldSerial = record[serialFld]
                if(oldSerial == text.get()) {
                    //変更されていない場合はOK
                    return true
                }
            }
        }
        catch (e:Exception) {
            this.validationMsg.set("予期せぬエラーが発生しました。システム管理者に連絡してください")
            BlasLog.trace("E", "findByItemId error", e)
            return false
        }


        try {
            itemsController.rmQrCodeCheck(text.get())
        }
        catch ( ex: ItemsController.ItemCheckException ) {
            val msg = ex.message
            this.validationMsg.set(msg)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }
}