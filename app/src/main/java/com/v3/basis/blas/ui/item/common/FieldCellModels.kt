package com.v3.basis.blas.ui.item.common

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt

data class FieldText(
	override val fieldNumber: Int,
	override val title: String,
	override val mustInput: Boolean,
	val text: ObservableField<String> = ObservableField("")
): FieldModel {

	override fun convertToString(): String? {
		return text.get()
	}
}

data class FieldSingleSelect(
	override val fieldNumber: Int,
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
}

data class FieldMultiSelect(
	override val fieldNumber: Int,
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
			val text = ""
			selectedIndexes.toList().forEach {
				if (it.second.get()) {
					text.plus(values.get(it.first))
				}
			}
			text
		}
	}
}

data class FieldSigFox(
	override val fieldNumber: Int,
	override val title: String = "シグフォックスは使用できません",
	override val mustInput: Boolean): FieldModel {

	override fun convertToString(): String? = null
}

data class FieldCheckText(
	override val fieldNumber: Int,
	override val title: String,
	override val mustInput: Boolean,
	val text: ObservableField<String> = ObservableField(""),
	val memo: ObservableField<String> = ObservableField("")
): FieldModel {

	override fun convertToString(): String? {
		return if (text.get().isNullOrBlank().not() && memo.get().isNullOrBlank().not()) {
			return text.get() + "(備考)" + memo.get()
		} else null
	}
}
