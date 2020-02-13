package com.v3.basis.blas.ui.test3.Test3Second

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

import com.v3.basis.blas.R
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Test3Second3Fragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Test3Second3Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Test3Second3Fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_test3_second3, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)
        val hour = calender.get(Calendar.YEAR)
        val minute = calender.get(Calendar.MONTH)

        val btn = view.findViewById<Button>(R.id.btn_test_datapick)
        val btn2 = view.findViewById<Button>(R.id.btn_test_timepick)

        btn.setOnClickListener {
            val dtp = DatePickerDialog(getContext()!!, DatePickerDialog.OnDateSetListener{ view, y, m, d ->
                Toast.makeText(activity, "日付を選択しました${y}/${m}/${d}", Toast.LENGTH_LONG).show()
            }, year,month,day)
            dtp.show()
        }
        btn2.setOnClickListener {
            val tp = TimePickerDialog(getContext()!!,TimePickerDialog.OnTimeSetListener{view,hour,minute->
                Toast.makeText(activity, "時間を選択しました${hour}:${minute}", Toast.LENGTH_LONG).show()
            },hour,minute,true)
            tp.show()
        }
    }

}
