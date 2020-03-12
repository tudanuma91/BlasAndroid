package com.v3.basis.blas.ui.fixture.fixture_search

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast

import com.v3.basis.blas.R
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FixtureSearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FixtureSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FixtureSearchFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)
        val hour = calender.get(Calendar.YEAR)
        val minute = calender.get(Calendar.MONTH)
        val root = inflater.inflate(R.layout.fragment_fixture_search, container, false)

        val kenpinDayMin = root.findViewById<EditText>(R.id.fixKenpinDayMin)

        kenpinDayMin.setOnClickListener{
            Toast.makeText(activity, "タップした", Toast.LENGTH_LONG).show()
            val date = DatePickerDialog(getContext()!!, DatePickerDialog.OnDateSetListener{ view, y, m, d ->
                Toast.makeText(activity, "日付を選択しました${y}/${m+1}/${d}", Toast.LENGTH_LONG).show()
            }, year,month,day)
            date.show()
            val aaa = year.toString()
            kenpinDayMin.setText(aaa)
        }

        val btnSearch = root.findViewById<Button>(R.id.fixSerchBtn)
        btnSearch.setOnClickListener{
            AlertDialog.Builder(activity)
                .setTitle("メッセージ")
                .setMessage("検索結果が表示されます。\n検索機能は現在作成中です。")
                .setPositiveButton("戻る",{dialog, which ->
                    //TODO YESを押したときの処理
                })
                /*.setNegativeButton("NO", { dialog, which ->
                    //TODO NOを押したときの処理
                })*/
                .show()
        }
        return root
    }

}
