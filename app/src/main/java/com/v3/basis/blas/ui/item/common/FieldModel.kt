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
    //親が子供のフィールドを持つ
    protected val childFieldList = mutableListOf<FieldModel>()

    //文字列を返却する
    open fun convertToString(): String? {
        return text.get()
    }

    //自分自身に値を設定する
    open fun setValue(value:String?) {
        text.set(value)
        childFieldList.forEach {child->
            if (value != null) {
                //親の値が設定されたら子供のフィールドに値を通知する。
                //現時点ではsingleSelectFieldのための機能。
                child.notifyFromParent(value)
            }
        }
    }

    open fun addChildField(field:FieldModel) {
        //子供のフィールドを追加する
        childFieldList.add(field)
    }

    //子供に親のデータを通知する
    open fun notifyFromParent(value:String) {
        //継承先で実体を書く前提
    }
}


