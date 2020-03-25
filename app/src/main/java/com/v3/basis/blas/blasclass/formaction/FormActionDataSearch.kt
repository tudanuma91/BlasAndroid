package com.v3.basis.blas.blasclass.formaction

import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.blasclass.config.FieldType

class FormActionDataSearch(setToken: String, setActivity: FragmentActivity) : FormActionDataBasic(setToken, setActivity){
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

    override fun typeCheck(list: MutableMap<String, String?>?): formType {
        return super.typeCheck(list)
    }

    override fun createTextField(params: LinearLayout.LayoutParams?, cnt: Int, formInfo: formType): EditText {
        return super.createTextField(params, cnt, formInfo)
    }

    override fun createTextAlea(params: LinearLayout.LayoutParams?, cnt: Int, formInfo: formType): EditText {
        return super.createTextAlea(params, cnt, formInfo)
    }

    override fun createDateTime(params: LinearLayout.LayoutParams?, cnt: Int, formInfo: formType): EditText {
        return super.createDateTime(params, cnt, formInfo)
    }


}