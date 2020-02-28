package com.v3.basis.blas.ui.test2

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.controller.QueueController
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase
import com.v3.basis.blas.blasclass.db.BlasSQLDataBaseHelper

class Test2Fragment : Fragment() {
    private var token:String? = null
    private var fragmentName = "Test2"

    private var arrayListId: ArrayList<String> = arrayListOf()
    private var arrayListName: ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test2,container,false)
        //トークン取得
        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null) {
            token = extras?.getString("token")
        }
        //トークンをtextViewにセット
        val test = view.findViewById<TextView>(R.id.text_test2)
        test.text = "token=${token}"
        //取得したトークンをログに出す
        Log.d("${fragmentName}","${token}")
        //変更を加えたviewを返す

        var context = getContext()
        val dbHelper = BlasSQLDataBaseHelper(context, BlasSQLDataBase.DB_NAME, null, BlasSQLDataBase.DB_VERSION);
        val db = dbHelper.writableDatabase


        var Button = view.findViewById<Button>(R.id.button1)
        Button.setOnClickListener{
            Log.d("【DBインサート】", "開始")

            val values = ContentValues()
            values.put("uri", "http:///")
            values.put("method", "GET")
            values.put("param_file", "app/sample.txt")
            values.put("retry_count", 0)
            values.put("status", 0)

            try {
                db.insertOrThrow("RequestTable", null, values)
                Log.d("【insert】", "成功")
            }catch(exception: Exception) {
                Log.e("insert", exception.toString())
            }

        }

        var selectBtn = view.findViewById<Button>(R.id.select)
        selectBtn.setOnClickListener{
            Log.d("【DBセレクト】", "開始")
            val sql = "select * from RequestTable"
            val cursor = db.rawQuery(sql, null)
            if (cursor.count > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast) {

                    Log.d("【DBセレクト】",cursor.getString(1))
                    arrayListName.add(cursor.getString(1))
                    cursor.moveToNext()
                }
            }
            cursor.close()

        }

        var deleteBtn = view.findViewById<Button>(R.id.delete)
        deleteBtn.setOnClickListener{
            Log.d("【delete】", "開始")
            val sql = "delete from RequestTable"
            try {
                db.delete("RequestTable",null,null)
            }catch(exception: Exception) {
                Log.e("deleteError", exception.toString())
            }
        }

        var threadBtn = view.findViewById<Button>(R.id.thread)
        threadBtn.setOnClickListener{
            Log.d("【thread】", "開始")

            try {
                val thr = QueueController
                thr.start()
            }catch(exception: Exception) {
                Log.e("【thread】", "エラー")
            }
        }



        return view
    }




}
