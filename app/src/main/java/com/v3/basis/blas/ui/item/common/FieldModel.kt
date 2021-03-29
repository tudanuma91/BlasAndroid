package com.v3.basis.blas.ui.item.common

import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
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
        return true
    }

    //親データとの整合チェック
    open fun parentValidate():Boolean {
        var ret = true
        parentField?.let{parent->
            val parentData = parent.convertToString()
            if(parentData == this.text.get()) {
                //親のフィールドの値と自分のフィールドが同じなので
                //入力を受け付ける
                text.set(this.text.get())
                ret = true
            }
            else {
                //親のフィールドと自分の値が異なる
                if(!this.memo.get().isNullOrBlank()) {
                    //メモの入力があったので
                    //入力を受け付ける
                    text.set(this.text.get())
                    ret = true
                }
                else {
                    //メモの入力がない
                    val msg = "相違しています。備考欄を入力してください"
                    this.validationMsg.set(msg)
                    ret = false
                }
            }
        }

        return ret
    }
}


