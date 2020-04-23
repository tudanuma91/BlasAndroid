package com.v3.basis.blas.blasclass.formaction

import android.graphics.Color
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil.setContentView
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

    fun createFreeWordSearch(params: LinearLayout.LayoutParams?): EditText {
        val edit = EditText(baseActivity)
        edit.setText("")
        edit.inputType =1
        edit.setLayoutParams(params)
        edit.id = 0
        return edit
    }

    fun createFreeWordSearchTitle(params: LinearLayout.LayoutParams?): TextView {
        val view = TextView(baseActivity)
        val title = "フリーワード検索"
        view.setText(title)
        //文字の色変更したい。
        view.setTextColor(Color.BLACK)
        view.setLayoutParams(params)
        return view
    }

    fun pickUpDateTime(dateTime:MutableMap<String,EditText>,cnt:Int,mode:String): String {
        var value = ""
        val word = "_from_"
        when(mode){
            "Day"->{
                //日付検索の値作成
                val minDay = dateTime["col_${cnt}_minDay"]!!.text.toString()
                val maxDay = dateTime["col_${cnt}_maxDay"]!!.text.toString()
                when{
                    //valueが最小値・最大値ともに入力されている場合
                    minDay != "" && maxDay != ""->{
                        value += minDay
                        value += word
                        value += maxDay
                    }

                    //valueが最大値のみ入力されている場合
                    minDay == "" && maxDay != ""->{
                        value += "Null"
                        value += word
                        value += maxDay
                    }

                    //valueが最小値のみ入力されている場合
                    minDay != "" && maxDay == ""->{
                        value += minDay
                        value += word
                        value += "Null"
                    }
                }
            }
            "Time"->{
                //時間検索の作成
                val minTime = dateTime["col_${cnt}_minTime"]!!.text.toString()
                val maxTime = dateTime["col_${cnt}_maxTime"]!!.text.toString()
                when{
                    //valueが最小値・最大値ともに入力されている場合
                    minTime != "" && maxTime != ""->{
                        value += minTime
                        value += word
                        value += maxTime
                    }

                    //valueが最大値のみ入力されている場合
                    minTime == "" && maxTime != ""->{
                        value += "Null"
                        value += word
                        value += maxTime
                    }

                    //valueが最小値のみ入力されている場合
                    minTime != "" && maxTime == ""->{
                        value += minTime
                        value += word
                        value += "Null"
                    }
                }

            }
        }
        return value
    }


    fun pickupCheckValue(editMap:MutableMap<String,EditText?>,cnt:Int): String {
        var newValue = ""
        val value = editMap.get("col_${cnt}_value")
        val memo = editMap.get("col_${cnt}_memo")

        if(value != null && memo != null){
            newValue = "{\"value\": \"${value.text}\", \"memo\": \"${memo.text}\"}"
        }
        Log.d("テスト、checkValue","バリュー=>${newValue}")
        return newValue
    }

}