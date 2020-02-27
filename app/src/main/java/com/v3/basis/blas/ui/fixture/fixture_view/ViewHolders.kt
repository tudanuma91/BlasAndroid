package com.v3.basis.blas.ui.fixture.fixture_view

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemEditActivity
import com.v3.basis.blas.activity.ItemImageActivity


class ViewHolders(itemView: View) : RecyclerView.ViewHolder(itemView){
    val titleView: TextView = itemView.findViewById(R.id.row_title)
    val detailView: TextView = itemView.findViewById(R.id.row_detail)
    //val itemListEditFragment = ItemEditFragment()

    init{
       /* val image = itemView.findViewById<ImageButton>(R.id.imageButton)
        val image2 = itemView.findViewById<ImageButton>(R.id.imageButton2)

        //編集アイコンタップ時の処理
        image.setOnClickListener{
            Log.d("test","${titleView.text}")
            val context = itemView.context
            val intent = Intent(context, ItemEditActivity::class.java)
            context.startActivity(intent)
        }

        //画像アイコンタップ時の処理
        image2.setOnClickListener{
         /*   val second3Fragment = Test3Second3Fragment()
            Log.d("test","${titleView.text}")
            ItemActivity()?.replaceFragment(second3Fragment)
            Log.d("aaa","よばれたよ！！")*/
            Log.d("test","${titleView.text}")
            val context = itemView.context
            val intent = Intent(context, ItemImageActivity::class.java)
            context.startActivity(intent)
        }*/

    }

}