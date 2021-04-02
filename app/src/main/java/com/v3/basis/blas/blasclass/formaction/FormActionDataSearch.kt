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


    fun createFreeWordTextBox(params: LinearLayout.LayoutParams?): EditText {
        val edit = EditText(baseActivity)
        edit.setText("")
        edit.inputType =1
        edit.setLayoutParams(params)
        edit.id = 0
        return edit
    }

    fun createFreeWordSearchTitle(params: LinearLayout.LayoutParams?): TextView {
        val view = TextView(baseActivity)
        val title = "検索キーワード"
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
        return newValue
    }


    //[(,),?,\,[,{,},*,+,.,]
    //別件だけど$,^,|,:,でなぜか全件ヒットする...
    fun valueChk(value:String): Boolean {
        val valueLen = value.length
        var chk = false
        Log.d("デバック用ログ","値の中身はコレ!!=>${value}")
        loop@ for(idx in 0 until valueLen){
            Log.d("デバック用ログ","値の中身はコレ!!=>${value.get(idx)}")
            Log.d("デバック用ログ","値の中身はコレ!!=>${'\\'}")
            when(value.get(idx)){
                '[', ']', '(', ')', '\\', '{', '}',
                '*', '+', '.', '$', '^', '|', ':', '!'
                ->{
                    chk = true
                    break@loop
                }
            }
        }
        Log.d("結果","処理結果はこの通り=>${chk.toString()}")
        return chk
    }

    fun test(key:String,titleMap:MutableMap<String,TextView>): TextView {
        var title = titleMap[key]!!
        val newTitle = title.text.toString() + FieldType.SEARCH_ERROR
        title.setText(newTitle)
        title.setTextColor(Color.RED)

        return title
    }

    fun test2(value: String , titleMap:MutableMap<String,TextView>): String {
        var text = titleMap[value]?.text.toString()
        val delNum = FieldType.SEARCH_ERROR.length
        text = text.dropLast(delNum)
        return text
    }

}