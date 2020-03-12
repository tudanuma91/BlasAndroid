package com.v3.basis.blas.ui.item.item_view

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.ItemEditActivity
import com.v3.basis.blas.activity.ItemImageActivity
import com.v3.basis.blas.ui.ext.getStringExtra


class ViewHolders(itemView: View) : RecyclerView.ViewHolder(itemView){
    val titleView: TextView = itemView.findViewById(R.id.row_title)
    val detailView: TextView = itemView.findViewById(R.id.row_detail)
    val image:ImageButton = itemView.findViewById<ImageButton>(R.id.imageButton)
    val image2:ImageButton = itemView.findViewById<ImageButton>(R.id.imageButton2)
    var itemList: MutableList<MutableMap<String, String?>> = mutableListOf()
    var token :String? = null
    var projectId:String? = null

    //var projectId:String? = null
    //var token :String? = null
    //val itemListEditFragment = ItemEditFragment()

    init{

       // token = getStringExtra("token")
        //projectId = getStringExtra("project_id")

        //編集アイコンタップ時の処理
        image.setOnClickListener{
            Log.d("test","${titleView.text}")
            val context = itemView.context
            val intent = Intent(context, ItemImageActivity::class.java)
            context.startActivity(intent)
        }

        //画像アイコンタップ時の処理
        val valueList :Array<String?> = arrayOf("test")
        image2.setOnClickListener{
            itemList.forEach{
                if(it["item_id"] == titleView.text.toString() ){
                    for(i  in 1..150){
                        //valueList.add(i,it.get("fld_${i}"))
                       // valueList.set(i,it.get("fld_${i}"))
                    }
                    return@forEach
                }
            }
            valueList.forEach {
                Log.d("aaaaaaaaaaa","${it}")
            }
            val context = itemView.context
            val intent = Intent(context, ItemEditActivity::class.java)
            intent.putExtra("item_id", "${titleView.text}")
            intent.putExtra("token", token)
            intent.putExtra("project_id", projectId)
            //intent.putExtra("value_list",valueList)
            context.startActivity(intent)
        }

    }

}