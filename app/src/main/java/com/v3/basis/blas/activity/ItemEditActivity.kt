package com.v3.basis.blas.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar

class ItemEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_edit)

        //左上の戻るボタン実装の処理
        showBackKeyForActionBar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //Write your logic here
                this.finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
