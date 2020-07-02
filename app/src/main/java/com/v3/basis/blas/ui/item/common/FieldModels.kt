package com.v3.basis.blas.ui.item.common

import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt

data class FieldText(
    override val cellNumber: Int,
	override val title: String,
	val text: ObservableField<String>): FieldModel

data class FieldSingleSelect(
	override val cellNumber: Int,
	override val title: String,
	val selectedIndex: ObservableInt): FieldModel

data class FieldMultiSelect(
	override val cellNumber: Int,
	override val title: String,
	val defaultSelectedIndex: List<Int>): FieldModel

data class FieldSigFox(
	override val cellNumber: Int,
	override val title: String = "シグフォックスは使用できません"): FieldModel

