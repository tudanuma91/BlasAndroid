package com.v3.basis.blas.ui.item.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.QRActivity
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.databinding.*
import com.v3.basis.blas.ui.ext.startActivityWithResult
import com.v3.basis.blas.ui.item.item_editor.ItemEditorFragment
import org.json.JSONObject

/**
 * ItemEditorViewに表示する各型のフィールド
 */
class FieldText(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField1Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field1, null, false)

	init {
		layout.model = this
	}
}


class FieldMultiText(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField2Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field2, null, false)

	init {
		layout.model = this
	}
}


class FieldDate(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField3Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field3, null, false)

	init {
		layout.model = this
	}
}

class FieldTime(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField4Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field4, null, false)

	init {
		layout.model = this
	}
}


class FieldSingleSelect (
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	val layout: InputField5Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field5, null, false)
	var choiceList: MutableList<String>
	var adapter:ArrayAdapter<String>

	init {
		//fieldのchoiceに選択肢が"a,b,c"のように指定されているので、アダプターに指定するため配列に変換。
		if(field.parent_field_id != 0) {
			//従属パラメーターあり
			//親の選択肢が決まるまで、子供は表示できない
			choiceList = mutableListOf("")
		}
		else {
			choiceList = field.choice!!.split(",").toMutableList()
			if(choiceList == null) {
				choiceList = mutableListOf("")
			}
		}

		//アダプター作成
		adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, choiceList)
		//ドロップダウンの表示領域を大きくする
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

		//ViewModelに自分を登録
		layout.model = this

		//SpinnerViewのアダプターに接続
		layout.spinner.adapter = adapter

		//選択されたときのイベントを取得する
		layout.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {

			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				//選択されたとき
				val spinnerParent = p0 as Spinner
				//選択された文字をメンバ変数に保持
				val selectedItemStr = spinnerParent.selectedItem as String

				//親fieldsテーブルを調べて、親の選択肢を子供に通知する必要がある
				childFieldList.forEach {child->
					if (selectedItemStr != null) {
						//親の値が設定されたら子供のフィールドに値を通知する。
						//現時点ではsingleSelectFieldのための機能。
						child.notifyedFromParent(selectedItemStr)
					}
				}
			}
			
			override fun onNothingSelected(p0: AdapterView<*>?) {
				//アイテムが選択されなかったとき。現時点では使用していない
			}
		}
	}


	override fun convertToString(): String? {
		return layout.spinner.selectedItem.toString()
	}

	override fun setValue(value: String?) {
		if(field.parent_field_id != 0) {
			val choiceJson = JSONObject(field.choice)
			val names = choiceJson.names()
			for (i in 0 until names.length()) {
				val name = names[i].toString()
				val childChoiceStr = choiceJson.getString(name)
				val tokens = childChoiceStr.split(",").toMutableList()
				for (j in 0 until tokens.size) {
					if (tokens[j] == value) {
						choiceList.clear()
						tokens.forEach {
							choiceList.add(it)
						}
						layout.spinner.setSelection(j)
						adapter.notifyDataSetChanged()
						return
					}
				}
			}
		}
		else {
			//親子関係のないシングルセレクト
			val tokens = field.choice?.split(",")?.toMutableList()
			if(tokens != null) {
				for(i in 0 until tokens.size) {
					if(tokens[i] == value) {
						layout.spinner.setSelection(i)
						break
					}
				}
			}
		}

		BlasLog.trace("I","end convertToString()")

	}

	/**
	 * 親フィールドからの変更を受信する
	 */
	override fun notifyedFromParent(value: String) {
		//親からの変更を受信する
		//{"野菜":"キャベツ,ニンジン","果物":"みかん,リンゴ","肉":"牛肉,鶏肉"}
		val choiceJson = JSONObject(field.choice)
		val childChoiceList = choiceJson.getString(value)
		val tokens = childChoiceList.split(",").toMutableList()
		//親項目が変更されたため、リストの差し替えを行う
		choiceList.clear()
		tokens.forEach {
			choiceList.add(it)
		}
		adapter.notifyDataSetChanged()
	}
}

class FieldMultiSelect(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField6Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field6, null, false)
	val selectedIndexes: MutableMap<Int, ObservableBoolean> = mutableMapOf()
	val values: MutableList<String> = mutableListOf()
	init {
		layout.model = this
	}

	fun selected(idx: Int, selected: ObservableBoolean) {
		selectedIndexes.set(idx, selected)
	}

	override fun convertToString(): String? {
		return if (selectedIndexes.isEmpty()) {
			null
		} else {
			val list = mutableListOf<String>()
			selectedIndexes.forEach {
				if (it.value.get()) {
					list.add(values.get(it.key))
				}
			}
			list.joinToString(",")
		}
	}

	override fun setValue(value: String?) {
		val vals = value?.split(",")
		vals?.forEachIndexed { index, s ->
			values.forEachIndexed { valuesIndex, valuesS ->
				if (s == valuesS) {
					selectedIndexes.get(valuesIndex)?.set(true)
				}
			}
		}
	}

	override fun notifyedFromParent(value: String) {
	}
}

class FieldLat(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField14Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field14, null, false)

	init {
		layout.model = this
	}
}


class FieldLng(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField15Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field15, null, false)

	init {
		layout.model = this
	}
}

class FieldLocation(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField7Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field7, null, false)

	init {
		layout.model = this
	}
}


class FieldQRCodeWithKenpin(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField8Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field8, null, false)

	init {
		layout.model = this
	}
}


class FieldQRCode(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField10Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field10, null, false)

	init {
		layout.model = this
	}
}


class FieldQRCodeWithTekkyo(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField11Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field11, null, false)

	init {
		layout.model = this
	}

}


class FieldAccount(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField12Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field12, null, false)

	init {
		layout.model = this
	}

}

class FieldSigFox(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	//使う気のないクラス
}

class FieldCheckText(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField13Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field13, null, false)
	//個別パラメーター
	val memo = ObservableField("")

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		//json形式で返却する
		var retStr:String = "";

		var tmpText:String? = ""
		var tmpMemo:String? = ""

		if(!RestHelper().isBlank(text.get())) {
			tmpText = text.get()
		}
		if(!RestHelper().isBlank(memo.get())) {
			tmpMemo = memo.get()
		}
		//ここって備考が入力されていないとデータが保存できないようになっていない？
		retStr = "{\"value\":\"${text.get()}\",\"memo\":\"${memo.get()}\"}"
		return retStr;
	}

	override fun setValue(value: String?) {
		//		この形式で来る
		//		{\"value\":\"aaa\",\"memo\":\"aaaaa\"}
		val json = value?.replace("\\", "")
		if (json != null && json.isNotBlank()) {
			val obj = JSONObject(json)
			val value1 = obj.get("value")
			val value2 = obj.get("memo")
			text.set(value1.toString())
			memo.set(value2.toString())
		}
	}

	override fun notifyedFromParent(value: String) {
	}

}

class FieldQRWithCheckText(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField16Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field16, null, false)
	//個別パラメーター
	val memo = ObservableField("")

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		//json形式で返却する
		var retStr:String = "";

		var tmpText:String? = ""
		var tmpMemo:String? = ""

		if(!RestHelper().isBlank(text.get())) {
			tmpText = text.get()
		}
		if(!RestHelper().isBlank(memo.get())) {
			tmpMemo = memo.get()
		}
		//ここって備考が入力されていないとデータが保存できないようになっていない？
		retStr = "{\"value\":\"${text.get()}\",\"memo\":\"${memo.get()}\"}"
		return retStr;
	}

	override fun setValue(value: String?) {
		//		この形式で来る
		//		{\"value\":\"aaa\",\"memo\":\"aaaaa\"}
		val json = value?.replace("\\", "")
		if (json != null && json.isNotBlank()) {
			val obj = JSONObject(json)
			val value1 = obj.get("value")
			val value2 = obj.get("memo")
			text.set(value1.toString())
			memo.set(value2.toString())
		}
	}

	override fun notifyedFromParent(value: String) {
	}
}


class FieldCurrentDateTime(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField17Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field17, null, false)

	init {
		layout.model = this
	}
}

class FieldCategorySelect(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	val layout: InputField18Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field18, null, false)
	var selectedItemStr = ""
	var choiceList: MutableList<String>
	var adapter:ArrayAdapter<String>

	init {
		choiceList = field.choice!!.split(",").toMutableList()
		if(choiceList == null) {
			choiceList = mutableListOf("")
		}

		//アダプター作成
		adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, choiceList)
		//ドロップダウンの表示領域を大きくする
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

		//ViewModelに自分を登録
		layout.model = this

		//SpinnerViewのアダプターに接続
		layout.spinner.adapter = adapter

		//選択されたときのイベントを取得する
		layout.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {

			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				//選択されたとき。何もしない
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {
				//アイテムが選択されなかったとき。現時点では使用していない
			}
		}
	}


	override fun convertToString(): String? {
		return layout.spinner.selectedItem.toString()
	}

	override fun setValue(value: String?) {
		//親子関係のないシングルセレクト
		val tokens = field.choice?.split(",")?.toMutableList()
		if(tokens != null) {
			for(i in 0 until tokens.size) {
				if(tokens[i] == value) {
					layout.spinner.setSelection(i)
					break
				}
			}
		}
		BlasLog.trace("I","end convertToString()")

	}

	/**
	 * 親フィールドからの変更を受信する
	 */
	override fun notifyedFromParent(value: String) {
		//親からの変更を受信する
	}
}

class FieldWorkerNameAutoComplete(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	val layout: InputField19Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field19, null, false)
	var choiceList: MutableList<String> = mutableListOf("")
	var adapter:ArrayAdapter<String>

	init {
		if(field.choice != null) {
			val tokens = field.choice?.split(",")
			if(tokens != null) {
				choiceList = tokens.toMutableList()
			}
		}

		//アダプター作成
		adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, choiceList)
		//ドロップダウンの表示領域を大きくする
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

		//ViewModelに自分を登録
		layout.model = this

		//AutoCompleteTextViewのアダプターに接続
		layout.autocomplete.threshold = 1
		layout.autocomplete.setAdapter(adapter)

	}


	override fun convertToString(): String? {
		return layout.autocomplete.text.toString()
	}

	override fun setValue(value: String?) {
		layout.autocomplete.setText(value)
	}

	override fun validate():Boolean {
		//選択肢にある名前以外が指定された場合
		val inputName = layout.autocomplete.text.toString()
		var ret = false
		val name = choiceList.find { it == inputName }
		if(name != null) {
			ret = true
		}

		if(!ret) {
			validationMsg.set("${inputName}は指定できない名前です")
		}

		return ret
	}

	/**
	 * 親フィールドからの変更を受信する
	 */
	override fun notifyedFromParent(value: String) {
		//親からの変更を受信する
	}

	/* ユーザーリストをすべて消す */
	public fun clearUserList() {
		choiceList.clear()
		adapter.notifyDataSetChanged()
	}

	/* リストにユーザを追加する */
	public fun addUser(userName:String) {
		choiceList.add(userName)
		adapter.notifyDataSetChanged()
	}
}


class FieldScheduleDate(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField20Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field20, null, false)

	init {
		layout.model = this
	}

}


class FieldWorkContentSelect(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	val layout: InputField21Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field21, null, false)
	var selectedItemStr = ""
	var choiceList: MutableList<String>
	var adapter:ArrayAdapter<String>

	init {
		choiceList = field.choice!!.split(",").toMutableList()
		if(choiceList == null) {
			choiceList = mutableListOf("")
		}

		//アダプター作成
		adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, choiceList)
		//ドロップダウンの表示領域を大きくする
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

		//ViewModelに自分を登録
		layout.model = this

		//SpinnerViewのアダプターに接続
		layout.spinner.adapter = adapter

		//選択されたときのイベントを取得する
		layout.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {

			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				//選択されたとき。何もしない
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {
				//アイテムが選択されなかったとき。現時点では使用していない
			}
		}
	}


	override fun convertToString(): String? {
		return selectedItemStr
	}

	override fun setValue(value: String?) {
		//親子関係のないシングルセレクト
		val tokens = field.choice?.split(",")?.toMutableList()
		if(tokens != null) {
			for(i in 0 until tokens.size) {
				if(tokens[i] == value) {
					layout.spinner.setSelection(i)
					break
				}
			}
		}
		BlasLog.trace("I","end convertToString()")

	}

	/**
	 * 親フィールドからの変更を受信する
	 */
	override fun notifyedFromParent(value: String) {
		//親からの変更を受信する
	}
}



class FieldAddress(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField22Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field22, null, false)

	init {
		layout.model = this
	}
}
