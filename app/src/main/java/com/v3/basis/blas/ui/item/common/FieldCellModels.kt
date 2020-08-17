package com.v3.basis.blas.ui.item.common

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import org.json.JSONObject
import java.lang.StringBuilder

data class FieldText(
	override val fieldNumber: Int,
	override val col:Int,
	override val title: String,
	override val mustInput: Boolean,
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	override fun convertToString(): String? {
		return text.get()
	}

	override fun setValue(value: String?) {
		value?.also { text.set(it) }
	}
}

data class FieldSingleSelect(
	override val fieldNumber: Int,
	override val col:Int,
	override val title: String,
	override val mustInput: Boolean,
	val values: MutableList<String> = mutableListOf(),
	val selectedIndex: ObservableInt = ObservableInt(-1)): FieldModel {

	fun selected(idx: Int) {
		selectedIndex.set(idx)
	}

	override fun convertToString(): String? {
		val idx = selectedIndex.get()
		return if (idx == -1) { null } else { values.get(idx) }
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
	override val fieldNumber: Int,
	override val col:Int,
	override val title: String,
	override val mustInput: Boolean,
	val values: MutableList<String> = mutableListOf(),
	val selectedIndexes: MutableMap<Int, ObservableBoolean> = mutableMapOf()
): FieldModel {

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

data class FieldSigFox(
	override val fieldNumber: Int,
	override val col:Int,
	override val title: String = "シグフォックスは使用できません",
	override val mustInput: Boolean): FieldModel {

	override fun convertToString(): String? = null
	override fun setValue(value: String?) {}
}

data class FieldCheckText(
	override val fieldNumber: Int,
	override val col:Int,
	override val title: String,
	override val mustInput: Boolean,
	val text: ObservableField<String> = ObservableField(""),
	val memo: ObservableField<String> = ObservableField("")
): FieldModel {

	override fun convertToString(): String? {
		return if (text.get().isNullOrBlank().not() && memo.get().isNullOrBlank().not()) {
			"{\"value\":\"${text.get()}\",\"memo\":\"${memo.get()}\"}"
		} else null
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
