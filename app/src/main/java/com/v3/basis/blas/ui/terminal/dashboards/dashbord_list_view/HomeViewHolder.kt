package com.v3.basis.blas.ui.terminal.dashboards.dashbord_list_view

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.BlasRestInformation
import org.json.JSONObject
import android.content.Context.MODE_PRIVATE
import android.content.Context
import com.v3.basis.blas.blasclass.rest.BlasRest.Companion.context
import android.widget.Toast
import android.content.ActivityNotFoundException
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri
import com.v3.basis.blas.activity.TerminalActivity
import android.R.id.message
import java.util.Base64.getEncoder
import java.util.Base64
import android.util.Base64.NO_WRAP
import java.io.*
import java.lang.Exception




class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleView: TextView = itemView.findViewById(R.id.row_title)
    val detailView: TextView = itemView.findViewById(R.id.row_detail)
    val fileView1 :TextView = itemView.findViewById(R.id.file1)
    val fileView2 :TextView = itemView.findViewById(R.id.file2)
    val fileView3 :TextView = itemView.findViewById(R.id.file3)

    var token:String? = null
    var informationId:String? = null

    init{
        fileView1.setOnClickListener{
            if(fileView1.text.toString() != ""){
                Log.d("取得","ファイルを取得する")
                getFile("1")
            }
        }

        fileView2.setOnClickListener{
            if(fileView2.text.toString() != ""){
                Log.d("取得","ファイルを取得する")
                getFile("2")
            }
        }

        fileView3.setOnClickListener{
            if(fileView3.text.toString() != ""){
                Log.d("取得","ファイルを取得する")
                getFile("3")
            }
        }
    }


    fun getFile(id :String){
        val payload =  mapOf("token" to token,"information_id" to informationId,"file_col_id" to id)
        BlasRestInformation("download",payload, ::getFileSuccess, ::getFileError).execute()
    }

    fun getFileSuccess(result: JSONObject) {
        Log.d("取得成功", "取得成功")
        Log.d("取得成功", "${result}")
        //dataのみ抜き取り完了
        val test = result.getJSONObject("records").getJSONObject("Information")
        val test2 = test["data"].toString()
        Log.d("testtest","${test2}")
        Log.d("testtest","aaaaaaaaaa")

        //base64のデコードとエンコード
        val s: String = "abcdefg"
        val b64Encode: String = getEncoder().encodeToString(s.toByteArray())
        val b64Decode = Base64.getDecoder().decode(b64Encode.toByteArray()).toString(Charsets.UTF_8)
        val b64Decode_data = Base64.getDecoder().decode(test2.toByteArray()).toString(Charsets.UTF_8)
        Log.d("testtest","${s}")
        Log.d("testtest","${b64Encode}")
        Log.d("testtest","${b64Decode}")
        Log.d("testtest","${b64Decode_data}")

        //C:\Users\PC697\AndroidStudioProjects\Blas_Android\app\src\main\java\com\v3\basis\blas\ui\terminal\dashboards\dashbord_list_view\HomeViewHolder.kt
       // val fos = FileOutputStream("com.v3.basis.blas.R.raw.newtest3.testpdf")
        //fos.write(b64Decode_data.toByteArray())
       // fos.flush()
        //fos.close()
        //com.v3.basis.blas.R.raw.test.pdf

       //val  uri = "android.resource://${getPackageName()}/${R.raw.test}/"
        TerminalActivity().resorce()








    }
    fun getFileError(errorCode:Int){
        Log.d("取得失敗","取得失敗")
    }
}