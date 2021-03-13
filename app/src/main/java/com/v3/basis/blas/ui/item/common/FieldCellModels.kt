package com.v3.basis.blas.ui.item.common

import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.databinding.*
import com.v3.basis.blas.ui.item.item_editor.ItemEditorFragment
import org.json.JSONObject

class FieldText(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField1Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field1, null, false)

	init {
		layout.model = this
	}

}


class FieldMultiText(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField2Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field2, null, false)

	init {
		layout.model = this
	}
}


class FieldDate(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField3Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field3, null, false)

	init {
		layout.model = this
	}
}

class FieldTime(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField4Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field4, null, false)

	init {
		layout.model = this
	}


	fun clickTime() {

	}
}


class FieldSingleSelect (
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField(""),
	var values: MutableList<String> = mutableListOf(),
	val selectedIndex: ObservableInt = ObservableInt(-1)
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField5Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field5, null, false)

	init {
		layout.model = this
	}

	fun selected(idx: Int) {
		selectedIndex.set(idx)
	}

	override fun convertToString(): String? {
		val idx = selectedIndex.get()
//		return if (idx == -1) { null } else { values.get(idx) }
		return if (idx == -1) { values.get(0) } else { values.get(idx) }
	}

	override fun setValue(value: String?) {
		values.forEachIndexed { index, s ->
			if (s == value) {
				selectedIndex.set(index)
				return
			}
		}
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
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField(""),

	val values: MutableList<String> = mutableListOf(),
	val selectedIndexes: MutableMap<Int, ObservableBoolean> = mutableMapOf()
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField6Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field6, null, false)

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
}

class FieldLat(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField14Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field14, null, false)

	init {
		layout.model = this
	}

}


class FieldLng(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField15Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field15, null, false)

	init {
		layout.model = this
	}

	fun clickLng() {

	}
}

class FieldLocation(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField7Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field7, null, false)

	init {
		layout.model = this
	}

	fun clickLocation() {

	}
}


class FieldQRCodeWithKenpin(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField8Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field8, null, false)

	init {
		layout.model = this
	}

	fun clickQRCodeKenpin() {

	}
}


class FieldQRCode(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField10Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field10, null, false)

	init {
		layout.model = this
	}

	fun clickQRCode() {

	}
}


class FieldQRCodeWithTekkyo(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField11Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field11, null, false)

	init {
		layout.model = this
	}

	fun clickQRCodeTekkyo() {

	}
}


class FieldAccount(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField12Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field12, null, false)
	var accountName: String = ""
	init {
		layout.model = this
	}

	fun clickAccountName() {
		this.text.set(accountName)
	}
}

class FieldSigFox(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	override fun convertToString(): String? = null
	override fun setValue(value: String?) {}
}

class FieldCheckText(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField(""),
	val memo: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField13Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field13, null, false)

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
}

class FieldQRWithCheckText(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField(""),
	val memo: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField16Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field16, null, false)

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

	fun clickQRCodeWithCheck() {

	}
}


class FieldCurrentDateTime(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField17Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field17, null, false)

	init {
		layout.model = this
	}

	fun clickCurrentDateTime() {

	}
}

class FieldWorkerName(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField19Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field19, null, false)

	init {
		layout.model = this
	}

}

class FieldScheduleDate(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField20Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field20, null, false)

	init {
		layout.model = this
	}

}

class FieldAddress(
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	validationMsg: ObservableField<String> = ObservableField(""),
	text: ObservableField<String> = ObservableField("")
): FieldModel(layoutInflater,fieldNumber,field,validationMsg,text) {

	var layout: InputField22Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field22, null, false)

	init {
		layout.model = this
	}

}