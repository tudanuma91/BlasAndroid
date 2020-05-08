package com.v3.basis.blas.blasclass.formaction

import android.util.Log
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.blasclass.config.FieldType

class FormActionDataEdit(setToken: String, setActivity: FragmentActivity):FormActionDataBasic(setToken, setActivity) {
    var setToken = super.token
    var setActivity = super.baseActivity
    var payloard = super.payload
    var mode :String? = null
        get() = field
        set(mode:String?){
            if(mode != null){
                field = mode
            }
        }


    override fun createFormSectionTitle(
        params: LinearLayout.LayoutParams?,
        formInfo: formType
    ): TextView {
        var title =  super.createFormSectionTitle(params, formInfo)
        if(formInfo.require == FieldType.TURE){
            //ラジオボタンかチェックボックスの場合、タイトルに入力必須を表示。
            if(formInfo.type != FieldType.SIG_FOX) {
                title.setError("入力必須です")
            }
        }
        return title
    }


    fun setDefaultValueEdit(value:String?,formPart:EditText): EditText {
        formPart.setText(value)
        return formPart
    }


    fun setDefaultValueRadio(value: String?, formPart: RadioButton, radioCount:Int,selectedValueId:Int): Int {
        var valueId = selectedValueId
        if(selectedValueId == -1){
            val checkValue = value?.split(",")
            if(checkValue != null) {
                if (checkValue.contains(formPart.text)) {
                    valueId = radioCount
                }
            }
        }
        return valueId
    }


    fun setDefaultValueRadioGroup(formPart: RadioGroup,selectedValueId:Int){
        if (selectedValueId != -1) {
            formPart.check(selectedValueId)
        }
    }


    fun setDefaultValueCheck(value: String?,formPart: CheckBox){
        val checkValue = value?.split(",")
        if(checkValue != null) {
            if (checkValue.contains(formPart.text)) {
                formPart.setChecked(true)
            }
        }
    }


}