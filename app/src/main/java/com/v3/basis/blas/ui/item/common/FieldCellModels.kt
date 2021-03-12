package com.v3.basis.blas.ui.item.common

import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.databinding.*
import org.json.JSONObject

class FieldText(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField1Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field1, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}
}


class FieldMultiText(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField2Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field2, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}
}


class FieldDate(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	override val text: ObservableField<String> = ObservableField("")
): FieldDateModel {

	var layout: InputField3Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field3, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}

}

class FieldTime(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField4Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field4, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}

	fun clickTime() {

	}
}


class FieldSingleSelect(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	var values: MutableList<String> = mutableListOf(),
	val selectedIndex: ObservableInt = ObservableInt(-1)
): FieldModel {

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

}

data class FieldMultiSelect(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val values: MutableList<String> = mutableListOf(),
	val selectedIndexes: MutableMap<Int, ObservableBoolean> = mutableMapOf()
): FieldModel {

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
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField14Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field14, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}
}


class FieldLng(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField15Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field15, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}

	fun clickLng() {

	}
}

class FieldLocation(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField7Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field7, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}

	fun clickLocation() {

	}
}


class FieldQRCodeWithKenpin(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField8Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field8, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}

	fun clickQRCodeKenpin() {

	}
}


class FieldQRCode(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField10Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field10, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}

	fun clickQRCode() {

	}
}


class FieldQRCodeWithTekkyo(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField11Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field11, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}

	fun clickQRCodeTekkyo() {

	}
}


class FieldAccount(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField12Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field12, null, false)
	var accountName: String = ""
	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}

	fun clickAccountName() {
		this.text.set(accountName)
	}
}

data class FieldSigFox(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	//override val col:Int,
	//override val title: String = "シグフォックスは使用できません",
	//override val mustInput: Boolean
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField("")
): FieldModel {

	override fun convertToString(): String? = null
	override fun setValue(value: String?) {}
}

data class FieldCheckText(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField(""),
	val memo: ObservableField<String> = ObservableField("")

): FieldModel {

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

data class FieldQRWithCheckText(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField(""),
	val memo: ObservableField<String> = ObservableField("")

): FieldModel {

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
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField17Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field17, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}

	fun clickCurrentDateTime() {

	}
}

class FieldWorkerName(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField19Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field19, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}
}

class FieldScheduleDate(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	override val text: ObservableField<String> = ObservableField("")
): FieldDateModel {

	var layout: InputField20Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field20, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}
}

class FieldAddress(
	override val layoutInflater: LayoutInflater,
	override val fieldNumber: Int,
	override val field: LdbFieldRecord,
	override val validationMsg: ObservableField<String> = ObservableField(""),
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	var layout: InputField22Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field22, null, false)

	init {
		layout.model = this
	}

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}
}