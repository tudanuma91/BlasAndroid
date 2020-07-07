package com.v3.basis.blas.ui.item.common

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt

data class FieldText(
    override val cellNumber: Int,
	override val title: String,
	override val mustInput: Boolean,
	val text: ObservableField<String> = ObservableField("")
): FieldModel

data class FieldSingleSelect(
	override val cellNumber: Int,
	override val title: String,
	override val mustInput: Boolean,
	val values: MutableList<String> = mutableListOf(),
	val selectedIndex: ObservableInt = ObservableInt(0)): FieldModel {

	fun selected(idx: Int) {
		selectedIndex.set(idx)
	}
}

data class FieldMultiSelect(
	override val cellNumber: Int,
	override val title: String,
	override val mustInput: Boolean,
	val values: MutableList<String> = mutableListOf(),
	val selectedIndexes: MutableMap<Int, ObservableBoolean> = mutableMapOf()
): FieldModel {
	fun selected(idx: Int, selected: ObservableBoolean) {
		selectedIndexes.set(idx, selected)
	}
}

data class FieldSigFox(
	override val cellNumber: Int,
	override val title: String = "シグフォックスは使用できません",
	override val mustInput: Boolean): FieldModel

data class FieldCheckText(
	override val cellNumber: Int,
	override val title: String,
	override val mustInput: Boolean,
	val text: ObservableField<String> = ObservableField("")
): FieldModel
