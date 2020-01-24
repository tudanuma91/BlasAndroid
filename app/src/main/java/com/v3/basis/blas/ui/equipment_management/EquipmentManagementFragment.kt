package com.v3.basis.blas.ui.equipment_management

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.QrcodeActivity


class EquipmentManagementFragment : Fragment() {

    private lateinit var equipmentManagementViewModel: EquipmentManagementViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        equipmentManagementViewModel =
            ViewModelProviders.of(this).get(EquipmentManagementViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_equipment_management, container, false)
        val textView: TextView = root.findViewById(R.id.text_equip)
        equipmentManagementViewModel.text.observe(this, Observer {
            textView.text = it
        })

        val loginBtn = root.findViewById<Button>(R.id.btnStartQr)
        loginBtn.setOnClickListener(ItemClickListener())
        return root
    }

    private inner class ItemClickListener : View.OnClickListener{
        override fun onClick(v: View?) {
            //ログイン処理開始
            val intent = Intent(activity, QrcodeActivity::class.java)
            startActivity(intent)
        }
    }
}