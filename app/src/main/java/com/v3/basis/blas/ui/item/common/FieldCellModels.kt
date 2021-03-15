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
	var selectedItemStr = ""
	var choiceList: Array<String>
	var adapter:ArrayAdapter<String>

	init {
		//fieldのchoiceに選択肢が"a,b,c"のように指定されているので、アダプターに指定するため配列に変換。
		if(field.parent_field_id != 0) {
			//従属パラメーターあり
			//親の選択肢が決まるまで、子供は表示できない

			//親が決まらないと子供のリストは決められない
			choiceList = arrayOf("")
		}
		else {
			choiceList = field.choice!!.split(",").toTypedArray()
		}

		if(choiceList == null) {
			choiceList = arrayOf("")
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
				selectedItemStr = spinnerParent.selectedItem as String

				//親fieldsテーブルを調べて、親の選択肢を子供に通知する必要がある
				childFieldList.forEach {child->
					if (selectedItemStr != null) {
						//親の値が設定されたら子供のフィールドに値を通知する。
						//現時点ではsingleSelectFieldのための機能。
						child.notifyFromParent(selectedItemStr)
					}
				}
			}
			
			override fun onNothingSelected(p0: AdapterView<*>?) {
				//アイテムが選択されなかったとき。現時点では使用していない
			}
		}
	}


	override fun convertToString(): String? {
		return selectedItemStr
//		val idx = selectedIndex.get()
//		return if (idx == -1) { null } else { values.get(idx) }
//		return if (idx == -1) { values.get(0) } else { values.get(idx) }
	}

	override fun setValue(value: String?) {
		BlasLog.trace("I","setValue(" + value + ") start!!")

		if(field.parent_field_id != 0) {
			var childChoiceList:String = ""
			val choiceJson = JSONObject(field.choice)
			val names = choiceJson.names()
			var findFlg = false

			for(i in 0 until names.length()) {
				val name = names[i].toString()
				BlasLog.trace("I","name:" + name)

				//ここまではあっている
//				var child_list = choiceJson.getJSONObject(name)//ここがおかしい
				// valueStr = "みかん,リンゴ","肉":"牛肉,鶏肉"
				val valueStr = choiceJson.getString(name)
				BlasLog.trace("I","values:" + valueStr)
				val values = valueStr.split(",")

				BlasLog.trace("I","values.size:" + values.size)
				for(j in 0 until values.size ) {

					BlasLog.trace("I","value[" + j + "] :" + values[j])
					if(value == values[j].toString()) {
						// childChoiceList = choiceJson.getString(value)
						childChoiceList = valueStr
						findFlg = true
					}
				}
				if(findFlg) {
					break
				}

			}

			choiceList = childChoiceList.split(",").toTypedArray()
		}

		choiceList.forEachIndexed {index, s ->
			if(s == value) {
				selectedItemStr = s
				layout.spinner.setSelection(index)

				BlasLog.trace("I","selectedItemStr:" + selectedItemStr)
				BlasLog.trace("I","index:" + index)
			}
		}

		BlasLog.trace("I","end convertToString()")

	}

	override fun notifyFromParent(value: String) {
		BlasLog.trace("I","start notifyFromParent()")

		//親からの変更を受信する
		//{"野菜":"キャベツ,ニンジン","果物":"みかん,リンゴ","肉":"牛肉,鶏肉"}
		val choiceJson = JSONObject(field.choice)
		val childChoiceList = choiceJson.getString(value)
		choiceList = childChoiceList.split(",").toTypedArray()
		adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, choiceList)
		layout.spinner.adapter = adapter
	}

	// TODO:三代川　↓↓↓↓↓ この人たちをここに持ってきたいのだが Spinner.createChildren() が持って来れない T_T
	/*
	fun createOption(
		field: LdbFieldRecord,inputField : FieldSingleSelect,context:Context,singleSelectSpinner:Map<Int,Spinner>
	) : String {
		BlasLog.trace("I","createOption() start field:" + field.name)
		var ret = ""

		if( 0 != field.parent_field_id ) {
			// 連動パラメータの時
			val jsonChoice = JSONObject(field.choice)
			val parents = jsonChoice.names()
			// とりあえず一番最初のchildを入れておく
			ret = jsonChoice.getString(parents[0].toString())
			BlasLog.trace("I","child:::" + ret)

			// 親を取得
			val parentSpinner = singleSelectSpinner[field.parent_field_id!!]

			if( parentSpinner != null ) {
				val parentValue = parentSpinner?.selectedItem as String
				BlasLog.trace("I","parent value::::" + parentValue)
				// childをちゃんとしたものに入替える
				ret = jsonChoice.getString(parentValue)

				// 親項目が変更されたら子も変える。ここでやるしかない！
				val listener = parentSpinner.onItemSelectedListener as ItemEditorFragment.SpinnerItemSelectedListener
				listener.optionalAction = { parent, position ->

					BlasLog.trace("I","親変更！！！  position:" + position)
					val newChoice = jsonChoice.getString(parents[ position ].toString())
					inputField.layout.spinner.createChildren(newChoice, inputField,context)
				}
			}
		}
		else {
			// 連動パラメータではない時
			// choiceに入ってる文字列をそのまま使用
			ret = field.choice.toString()
		}

		return ret
	}
	*/

	/**
	 * セレクタの選択肢を設定する
	 */
	/*
	fun Spinner.createChildren(separatedText: String?, model: FieldSingleSelect,context:Context) {
		separatedText?.also {
			val list = it.split(",")
			model.values.addAll(list)
			val ad = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item)
			ad.addAll(list)
			this.adapter = ad
		}
	}
	 */



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

	override fun notifyFromParent(value: String) {
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

	override fun notifyFromParent(value: String) {
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

	override fun notifyFromParent(value: String) {
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

class FieldWorkerName(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField19Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field19, null, false)

	init {
		layout.model = this
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