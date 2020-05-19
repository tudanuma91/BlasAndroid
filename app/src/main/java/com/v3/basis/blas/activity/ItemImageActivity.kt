package com.v3.basis.blas.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentTransaction
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar
import com.v3.basis.blas.ui.item.item_image.ItemImageFragment

class ItemImageActivity : AppCompatActivity() {

    companion object {
        const val TOKEN = "token"
        const val PROJECT_ID = "project_id"
        const val ITEM_ID = "item_id"

        fun createIntent(context: Context, token: String?, projectId: String?, itemId: String?) : Intent {

            return Intent(context, ItemImageActivity::class.java).apply {
                putExtra(TOKEN, token)
                putExtra(PROJECT_ID, projectId)
                putExtra(ITEM_ID, itemId)
            }
        }
    }

    private val token:String
        get() = intent.extras?.getString(TOKEN) ?: ""

    private val projectId: String
        get() = intent.extras?.getString(PROJECT_ID) ?: ""

    private val itemId: String
        get() = intent.extras?.getString(ITEM_ID) ?: ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_image)

        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = ItemImageFragment.newInstance(token, projectId, itemId)
            transaction.replace(R.id.container, fragment)
            transaction.commit()
        }

        //左上の戻るボタンの処理
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
