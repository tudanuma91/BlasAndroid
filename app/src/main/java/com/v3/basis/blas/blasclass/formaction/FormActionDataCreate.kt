package com.v3.basis.blas.blasclass.formaction

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.blasclass.config.FieldType

class FormActionDataCreate(setToken: String, setActivity: FragmentActivity):FormActionDataBasic(setToken, setActivity) {

    var payloard = super.payload
    var mode :String? = null
        get() = field
        set(mode:String?){
            if(mode != null){
                field = mode
            }
        }

    var root: View? = null

    override fun createFormSectionTitle(
        params: LinearLayout.LayoutParams?,
        formInfo: formType
    ): TextView {
        var title =  super.createFormSectionTitle(params, formInfo)
        if(formInfo.require == FieldType.TURE){
            if(formInfo.type != FieldType.SIG_FOX) {
                title.setError("入力必須です")
            }
        }
        return title
    }
}
