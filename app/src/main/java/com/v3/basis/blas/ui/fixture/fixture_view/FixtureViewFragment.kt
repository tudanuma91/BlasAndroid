package com.v3.basis.blas.ui.fixture.fixture_view


import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.canTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.finishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.notTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.rtn
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusCanTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusFinishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusNotTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.takeOut
import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.ldb.LdbFixtureDispRecord
import com.v3.basis.blas.blasclass.rest.BlasRest
import com.v3.basis.blas.databinding.FragmentFixtureViewBinding

import com.v3.basis.blas.ui.ext.addTitleWithProjectName
import com.v3.basis.blas.ui.ext.getStringExtra
import com.v3.basis.blas.ui.common.ARG_PROJECT_ID
import com.v3.basis.blas.ui.common.ARG_PROJECT_NAME
import com.v3.basis.blas.ui.common.ARG_TOKEN
import com.v3.basis.blas.ui.common.FixtureBaseFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.GroupieViewHolder
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fixture.*
import kotlinx.android.synthetic.main.fragment_fixture_view.*
import kotlinx.android.synthetic.main.fragment_item_view.recyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 */
class FixtureViewFragment : FixtureBaseFragment() {
    private var dataListAll = mutableListOf<FixtureListCell>()
    private var dataList = mutableListOf<FixtureListCell>()
    private var valueMap : MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private var searchValueMap:MutableMap<String,String?> = mutableMapOf()
    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG
    private var helper = RestHelper()

    private var jsonParseList : JSONArray? = null
    private val disposables = CompositeDisposable()

    private var paresUnitNum = 100
    private var parseStartNum = 0
    private var parseFinNum = paresUnitNum

    private var currentIndex: Int = 0
    private var offset: Int = 0

    private lateinit var bind: FragmentFixtureViewBinding

    companion object {
        const val CREATE_UNIT = 20
        fun newInstance() = FixtureViewFragment()
    }

    private lateinit var viewModel: FixtureListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitleWithProjectName("??????????????????")
    }

    private val groupAdapter = GroupAdapter<GroupieViewHolder<*>>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        getSearchParams()
        viewModel = ViewModelProviders.of(this).get(FixtureListViewModel::class.java)

        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_fixture_view, container, false)
        bind.vm = viewModel

        return bind.root
    }

    private fun getSearchParams() {
        arguments?.let {
            searchValueMap.set("freeWord",it.getString("freeWord"))
            searchValueMap.set("serial_number",it.getString("serialNumber"))
            searchValueMap.set("fixture_id",it.getString("dataId"))
            searchValueMap.set("FixOrg",it.getString("kenpinOrg"))
            searchValueMap.set("FixUser",it.getString("kenpinUser"))
            searchValueMap.set("kenpinDayMin",it.getString("kenpinDayMin"))
            searchValueMap.set("kenpinDayMax",it.getString("kenpinDayMax"))
            searchValueMap.set("TakeOutOrg",it.getString("takeOutOrg"))
            searchValueMap.set("TakeOutUser",it.getString("takeOutUser"))
            searchValueMap.set("takeOutDayMin",it.getString("takeOutDayMin"))
            searchValueMap.set("takeOutDayMax",it.getString("takeOutDayMax"))
            searchValueMap.set("RtnOrg",it.getString("returnOrg"))
            searchValueMap.set("RtnUser",it.getString("returnUser"))
            searchValueMap.set("returnDayMin",it.getString("returnDayMin"))
            searchValueMap.set("returnDayMax",it.getString("returnDayMax"))
            searchValueMap.set("ItemOrg",it.getString("itemOrg"))
            searchValueMap.set("ItemUser",it.getString("itemUser"))
            searchValueMap.set("itemDayMin",it.getString("itemDayMin"))
            searchValueMap.set("itemDayMax",it.getString("itemDayMax"))
            searchValueMap.set("status",it.getString("status"))
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            if(token != null && projectId != null) {
                Log.d("lifeCycle", "onViewCreated")
                //???????????????-view?????????
                //??????????????????????????????????????????????????????????????????????????????????????????adapter????????????????????????
                val recyclerView = recyclerView
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(activity)
                recyclerView.adapter = groupAdapter
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    //??????????????????????????????????????????????????????
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        //100????????????????????????
                        if (!recyclerView.canScrollVertically(1) && progressBar.visibility == View.INVISIBLE) {
                            progressBar.visibility = View.VISIBLE
                            offset += CREATE_UNIT

                            getRecords()
                        }
                    }
                })

                //?????????????????????????????????????????????
                getRecords()
            }else{
                throw java.lang.Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
            try {
                progressBar.visibility = View.INVISIBLE
            }catch (e:Exception){

            }

        }
    }

    private fun getRecords() {
        val fixtureController = FixtureController(BlasApp.applicationContext(), projectId)
        //?????????????????????????????????
        Single.fromCallable { fixtureController.searchDisp(offset = offset, searchMap = searchValueMap) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn {
                Toast.makeText(BlasRest.context, "???????????????????????????????????????", Toast.LENGTH_LONG).show()
                return@onErrorReturn listOf<LdbFixtureDispRecord>()
            }
            .map {
                val list = mutableListOf<FixtureListCell>()

                if(0 == offset && 0 == it.count()) {
                    Toast.makeText(BlasRest.context, "???????????????????????????????????????", Toast.LENGTH_LONG).show()
                }
                it.forEach {
                    val value = createValue(it) ?: ""

                    var title = it.fixture_id.toString()
                    if( it.fixture_id < 0L ) {
                        title = "(????????????)"
                    }

                    // it???????????????????????????????????????????????????????????????????????????
                    val model = FixtureCellModel(
                        token
                        ,projectId.toInt()
                        ,it.fixture_id
                        , title.toString()
                        , value
                        , it.sync_status
                        , requireContext()
                    )
               //     model.errorMessage.set(it.error_msg)
                    if(it.sync_status > BaseController.SYNC_STATUS_SYNC) {
                        model.syncVisible.set(true)
                        model.errorMessage.set(it.error_msg)
                    }
                    else {
                        model.syncVisible.set(false)
                    }
                    list.add(FixtureListCell(viewModel, model))
                }
                list
            }
            .subscribeBy {
                dataListAll.addAll(it)
                setAdapter()
            }
            .addTo(disposables)
    }

    private fun createDataList() {
        //???????????????????????????
        val filteredList = dataListAll.filterIndexed { index, mutableMap ->
            (index >= currentIndex) && (index < currentIndex + CREATE_UNIT)
        }
        dataList.addAll(filteredList.toMutableList())

        // update
        if (dataList.isNotEmpty()) {
            currentIndex += CREATE_UNIT
        }

        setNotSendCount(filteredList)
    }

    private fun setNotSendCount(list: List<FixtureListCell>) {

        val count = list.filter { it.model.syncVisible.get() }.size
        viewModel.sendCount.set((viewModel.sendCount.get() as Int) + count)
    }

    /**
     *  ???????????????
     */
    private fun setAdapter() {
        createDataList()
        groupAdapter.update(dataList)
        try {
            progressBar.visibility = View.INVISIBLE
        }catch (e:Exception){

        }
    }


    /**
     * ??????????????????????????????
     */
    private fun createValue(rcd: LdbFixtureDispRecord): String? {
        var value:String? = ""

        value += "<table border=\"1\" style=\"border-collapse: collapse; table-layout:fixed; border-style: solid; border-color: #FF69B4;\" width=\"100%\">"
        try {
            val td = "<td bgcolor=\"#FFEFFF\">"
            value += "<tr>"
            value += "$td${getString(R.string.col_serialnumber)}</td>"
            value += "<td>${rcd.serial_number}</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_status)}</td>"
            value += "<td>" + when (rcd.status) {//config.FixtureType???????????????????????????
                canTakeOut.toInt() -> {
                    "${statusCanTakeOut}"
                }
                rtn.toInt() -> {
                    "${statusCanTakeOut}"
                }
                takeOut.toInt() -> {
                    "${statusTakeOut}"
                }
                finishInstall.toInt() -> {
                    "${statusFinishInstall}"
                }
                notTakeOut.toInt() -> {
                    "${statusNotTakeOut}"
                }
                else -> {
                }
            } + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_kenpin_org)}</td>"
            value += "<td>" + setValue(rcd.fix_org_name) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_kenpin_user)}</td>"
            value += "<td>" + setValue(rcd.fix_user_name) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_kenpin_date)}</td>"
            value += "<td>" + setValue(rcd.fix_date) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_takeout_org)}</td>"
            value += "<td>" + setValue(rcd.takeout_org_name) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_takeout_user)}</td>"
            value += "<td>" + setValue(rcd.takeout_user_name) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_takeout_date)}</td>"
            value += "<td>" + setValue(rcd.takeout_date) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_return_org)}</td>"
            value += "<td>" + setValue(rcd.rtn_org_name) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_return_user)}</td>"
            value += "<td>" + setValue(rcd.rtn_user_name) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_return_date)}</td>"
            value += "<td>" + setValue(rcd.rtn_date) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_item_org)}</td>"
            value += "<td>" + setValue(rcd.item_org_name) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_item_user)}</td>"
            value += "<td>" + setValue(rcd.item_user_name) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_item_date)}</td>"
            value += "<td>" + setValue(rcd.item_date) + "</td>"
            value += "</tr>"

            value += "<tr>"
            value += "$td${getString(R.string.col_item_id)}</td>"

            if( rcd.item_id < 0L ) {
                value += "<td>" + setValue("????????????") + "</td>"
            }
            else if( 0L == rcd.item_id ) {
                value += "<td>" + setValue("") + "</td>"
            }
            else {
                value += "<td>" + setValue(rcd.item_id.toString()) + "</td>"
            }
            value += "</tr>"

        }catch (e:Exception){

        }

        value += "</table>"
        Log.d("Table", value)

        return value

    }


    private fun setValue(value:String): String {
        if(value == "null"){
            return "  "
        }else{
            return value
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        disposables.dispose()
        super.onDestroyView()
    }


    //?????????????????????
    private fun parseJson(){
        if(jsonParseList != null ) {
            val chk = jsonParseList?.length()
            if(chk != null) {
                if (chk <= parseFinNum) {
                     valueMap =  helper.createJsonFix(jsonParseList!!,valueMap,parseStartNum,chk)
                }
                else{
                    valueMap = helper.createJsonFix(jsonParseList!!,valueMap,parseStartNum,parseFinNum)
                }
                parseFinNum += paresUnitNum
                parseStartNum += paresUnitNum
            }
        }
    }

}
