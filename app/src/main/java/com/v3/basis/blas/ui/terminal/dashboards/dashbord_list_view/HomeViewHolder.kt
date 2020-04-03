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
import android.app.PendingIntent.getActivity
import java.util.Base64.getEncoder
import java.util.Base64
import android.util.Base64.NO_WRAP
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.net.toUri
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.rest.BlasRest
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

        val info = result.getJSONObject("records").getJSONObject("Information")
        val file_data = info["data"].toString()
        val intent = Intent(Intent.ACTION_VIEW)
        var decFile = Base64.getDecoder().decode(file_data.toByteArray())

        // ファイルパス
        val filePath = context.filesDir.toString() + "/" + info["filename"].toString()

        try {
            val fp = File(filePath)
            var fos = FileOutputStream(fp)
            var bos = BufferedOutputStream(fos)
            // 出力ストリームへの書き込み（ファイルへの書き込み）
            bos.write(decFile)
            bos.flush()
            bos.close()

        }catch (exception: Exception){
            Toast.makeText(context, R.string.download_error, Toast.LENGTH_LONG).show()
            Log.e("dashBoardFileError", exception.toString())
        }

        // ファイルプロバイダーの為のURIを取得。content形式のURIを取得
        try {
            val uri = FileProvider.getUriForFile(context,"com.v3.basis.blas",File(filePath));
            intent.setDataAndType(uri, "*/*");
        }catch (exception: Exception){
            Toast.makeText(context, R.string.uri_error, Toast.LENGTH_LONG).show()
            Log.e("FileProviderUriGetError", exception.toString())
        }

        // パーミッションの追加　updateで必要となった　ハマりポイント
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(intent)

        TerminalActivity().resorce()

    }
    fun getFileError(errorCode:Int){
        Log.d("取得失敗","取得失敗")
    }
}