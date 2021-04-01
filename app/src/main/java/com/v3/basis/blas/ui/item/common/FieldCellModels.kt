package com.v3.basis.blas.ui.item.common

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.ldb.LdbUserRecord
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.databinding.*
import org.json.JSONObject

/**
 * ItemEditorViewに表示する各型のフィールド
 */

/**
 * 自由入力(1行)フォーム
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

/**
 * 自由入力(複数行)フォーム
 */
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

/**
 * 日付フォーム
 */
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

/**
 * 時刻フォーム
 */
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

/**
 * 単一選択フォーム
 */
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

	override fun validate(itemId:String): Boolean {
		var ret = true
		if(!this.field.case_required.isNullOrBlank()) {
			val durtyText = this.field.case_required
			val json = durtyText?.replace("\\", "")
			var parentFieldName = ""
			val choice = convertToString()
			if(choice.isNullOrBlank()) {
				//選択肢が選ばれていないときはＯＫとする
				return true
			}

			if (json != null && json.isNotBlank()) {
				val obj = JSONObject(json)
				try{
					//指定された選択肢に対応するフィールド名を取得する
					parentFieldName = obj.getString(choice)
				}
				catch(e:Exception) {
					validationMsg.set("不正な親フィールド名です")
					ret = false
					return ret
				}

				//親フィールドの値を取得する
				try{
					val parentFieldModel = parentFieldList.first { parentFieldName == it.field.name }
					if(parentFieldModel.text.get().isNullOrBlank()) {
						validationMsg.set("${choice}の場合は、${parentFieldName}を入力してください")
						ret = false
					}
				}
				catch(e:Exception) {
					//親フィールドが見つからない場合
					BlasLog.trace("E", "親フィールドが見つかりません", e)
					ret = false
				}

			}
		}

		return ret
	}
}

/**
 * 複数選択フォーム
 */
class FieldMultiSelect(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField6Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field6, null, false)
	val choiceList: MutableMap<String, ObservableBoolean> = mutableMapOf()
	val values: MutableList<String> = mutableListOf()
	init {
		field.choice?.split(",")?.forEach { choice ->
			val checkBoxLayout = DataBindingUtil.inflate<ViewItemsCheckboxBinding>(layoutInflater, R.layout.view_items_checkbox, null, false)
			choiceList[choice] = ObservableBoolean(false)
			checkBoxLayout.selected = choiceList[choice]
			checkBoxLayout.checkBox.text = choice
			layout.checkBoxGroup.addView(checkBoxLayout.root)
		}

		layout.model = this
	}


	override fun convertToString(): String? {
		return if (choiceList.isEmpty()) {
			null
		} else {
			val list = mutableListOf<String>()
			choiceList.forEach {
				if (it.value.get()) {
					list.add(it.key)
				}
			}
			list.joinToString(",")
		}
	}

	override fun setValue(value: String?) {

		val vals = value?.split(",")
		vals?.forEach {choice->
			choiceList[choice]?.set(true)
		}
	}

	override fun notifyedFromParent(value: String) {
	}
}

/**
 * 緯度フォーム
 */
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

/**
 * 経度フォーム
 */
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

/**
 * 場所フォーム
 */
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

/**
 * QRコード(検品と連動)
 */
class FieldQRCodeWithKenpin(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldQRBaseModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField8Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field8, null, false)

	init {
		layout.model = this
	}

	override fun validate(itemId:String): Boolean {

		if(!super.validate(itemId)) {
			return false
		}

		//検品チェック
		if(!validateKenpinRendou(itemId)) {
			return false
		}
		return true
	}

}

/**
 * QRコードフォーム
 */
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

/**
 * QRコード(撤去と連動)フォーム
 */
class FieldQRCodeWithTekkyo(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldQRBaseModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField11Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field11, null, false)

	init {
		layout.model = this
	}

	override fun validate(itemId:String): Boolean {

		if(!super.validate(itemId)) {
			return false
		}

		// 撤去連動
		if( !validateTekkyoRendou(itemId) ) {
			return false
		}

		return true
	}


}

/**
 * アカウント名フォーム
 */
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

/**
 * シグフォックス型。現在使用していない
 */
class FieldSigFox(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	//使う気のないクラス
}

/**
 * 入力値チェック連動
 */
class FieldCheckText(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

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

	override fun notifyedFromParent(value: String) {
	}

	//親データとの整合チェック
	override fun parentValidate():Boolean {
		var ret = true
		parentFieldList.forEach{parent->
			val parentData = parent.convertToString()
			if(parentData == this.text.get()) {
				//親のフィールドの値と自分のフィールドが同じなので
				//入力を受け付ける
				text.set(this.text.get())
				ret = true
			}
			else {
				//親のフィールドと自分の値が異なる
				if(!this.memo.get().isNullOrBlank()) {
					//メモの入力があったので
					//入力を受け付ける
					text.set(this.text.get())
					ret = true
				}
				else {
					//メモの入力がない
					val msg = "相違しています。備考欄を入力してください"
					this.validationMsg.set(msg)
					ret = false
				}
			}
		}

		return ret
	}

}

/**
 * 入力値チェック連動_QRコード(検品と連動)
 */
class FieldQRWithCheckText(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldQRBaseModel(context, layoutInflater, fieldNumber, field) {

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
			var value1 = obj.get("value")
			var value2 = obj.get("memo")
			if(value1 == null) {
				value1 = ""
			}
			if(value2 == null) {
				value2 = ""
			}

			text.set(value1.toString())
			memo.set(value2.toString())
		}
	}

	override fun notifyedFromParent(value: String) {
	}

	override fun validate(itemId:String): Boolean {

		if(!super.validate(itemId)) {
			return false
		}

		//検品チェック
		if(!validateKenpinRendou(itemId)) {
			return false
		}
		return true
	}

	//親データとの整合チェック
	override fun parentValidate():Boolean {
		var ret = true
		parentFieldList.forEach{parent->
			val parentData = parent.convertToString()
			if(parentData == this.text.get()) {
				//親のフィールドの値と自分のフィールドが同じなので
				//入力を受け付ける
				text.set(this.text.get())
				ret = true
			}
			else {
				//親のフィールドと自分の値が異なる
				if(!this.memo.get().isNullOrBlank()) {
					//メモの入力があったので
					//入力を受け付ける
					text.set(this.text.get())
					ret = true
				}
				else {
					//メモの入力がない
					val msg = "相違しています。備考欄を入力してください"
					this.validationMsg.set(msg)
					ret = false
				}
			}
		}

		return ret
	}

}


/**
 * 現在日時型フォーム
 */
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

/**
 * カテゴリフォーム
 */
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

/**
 * 作業者フォーム
 */
class FieldWorkerNameAutoComplete(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord,
	val workers: List<String>?
): FieldModel(context, layoutInflater, fieldNumber, field) {

	val layout: InputField19Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field19, null, false)
	var choiceList: MutableList<String> = mutableListOf("")
	var adapter:ArrayAdapter<String>

	init {

/*
		if(field.choice != null) {
			val tokens = field.choice?.split(",")
			if(tokens != null) {
				choiceList = tokens.toMutableList()
			}
		}
*/
		choiceList = workers as MutableList<String>

		//アダプター作成
		adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, choiceList)
		//ドロップダウンの表示領域を大きくする
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

		//ViewModelに自分を登録
		layout.model = this

		//SpinnerViewのアダプターに接続
		layout.spinner.adapter = adapter


		//AutoCompleteTextViewのアダプターに接続
/*
		layout.autocomplete.threshold = 1
		layout.autocomplete.setAdapter(adapter)
*/
	}


	override fun convertToString(): String? {
//		return layout.autocomplete.text.toString()
		return layout.spinner.selectedItem.toString()

	}

	override fun setValue(value: String?) {
//		layout.autocomplete.setText(value)
		//親子関係のないシングルセレクト
//		val tokens = field.choice?.split(",")?.toMutableList()
		val tokens = workers
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

/*
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
*/

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

/**
 * 予定日フォーム
 */
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

/**
 * 作業内容フォーム
 */
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
//		return selectedItemStr
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

/**
 * 住所フォーム
 */
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

/**
 * バーコードフォーム
 */
class FieldBarCode(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField24Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field24, null, false)

	init {
		layout.model = this
	}
}

/**
 * イベント型フォーム
 */
class FieldEvent(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField23Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field23, null, false)

	init {
		layout.model = this
	}

	override fun setValue(value: String?) {
		text.set(value)
		if(value != "処理中") {
			//layout.button.text = value
			layout.button.visibility = View.VISIBLE
			layout.progessImg.visibility = View.GONE
			layout.status.visibility = View.VISIBLE
			layout.status.text = value
		}
		else {
			animationStart(layout)
			layout.button.visibility = View.GONE
			layout.progessImg.visibility = View.VISIBLE
			layout.status.visibility = View.GONE
		}

	}

	@RequiresApi(Build.VERSION_CODES.P)
	private fun getGifAnimationDrawable(): AnimatedImageDrawable {
		//画像ソースを取得(assets直下)
		val source = ImageDecoder.createSource(BlasSQLDataBase.context.assets,"run.gif" )
		return ImageDecoder.decodeDrawable(source) as? AnimatedImageDrawable
			?: throw ClassCastException()
	}


	//gifを表示する処理＋動かす処理
	private fun animationStart(binding: InputField23Binding){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			//pie以降はこっちの処理を使用する
			val drawable = getGifAnimationDrawable()
			binding.progessImg.setImageDrawable(drawable)
			drawable.start()
		}else {
			//pieより前はこっちの処理を使用する
			//後々ここの処理は削除したい。
			Glide.with(BlasSQLDataBase.context).load(R.drawable.run).into(binding.progessImg)
		}
	}
}

/**
 * バーコード(検品と連動)
 */
class FieldBarCodeWithKenpin(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldQRBaseModel(context, layoutInflater, fieldNumber, field) {

	var layout :InputField25Binding = DataBindingUtil.inflate(layoutInflater, R.layout.input_field25, null, false)

	init {
		layout.model = this
	}

	override fun validate(itemId:String): Boolean {

		if(!super.validate(itemId)) {
			return false
		}
		//検品チェック
		if(!validateKenpinRendou(itemId)) {
			return false
		}
		return true
	}

}

/**
 * バーコード(撤去と連動)フォーム
 */
class FieldBarCodeWithTekkyo(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldQRBaseModel(context, layoutInflater, fieldNumber, field) {

	var layout :InputField26Binding = DataBindingUtil.inflate(layoutInflater, R.layout.input_field26, null, false)

	init {
		layout.model = this
	}

	override fun validate(itemId:String): Boolean {

		if(!super.validate(itemId)) {
			return false
		}

		// 撤去連動
		if( !validateTekkyoRendou(itemId) ) {
			return false
		}

		return true
	}

}

/**
 * 入力値チェック連動_バーコード(検品と連動)用フォーム
 */
class FieldBarCodeWithCheckText(
	context: Context,
	layoutInflater: LayoutInflater,
	fieldNumber: Int,
	field: LdbFieldRecord
): FieldQRBaseModel(context, layoutInflater, fieldNumber, field) {

	var layout: InputField27Binding =  DataBindingUtil.inflate(layoutInflater, R.layout.input_field27, null, false)
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
			var value1 = obj.get("value")
			var value2 = obj.get("memo")
			if(value1 == null) {
				value1 = ""
			}
			if(value2 == null) {
				value2 = ""
			}
			text.set(value1.toString())
			memo.set(value2.toString())
		}
	}

	override fun notifyedFromParent(value: String) {
	}

	override fun validate(itemId:String): Boolean {

		if(!super.validate(itemId)) {
			return false
		}
		//検品チェック
		if(!validateKenpinRendou(itemId)) {
			return false
		}
		return true
	}

	//親データとの整合チェック
	override fun parentValidate():Boolean {
		var ret = true
		parentFieldList.forEach{parent->
			val parentData = parent.convertToString()
			if(parentData == this.text.get()) {
				//親のフィールドの値と自分のフィールドが同じなので
				//入力を受け付ける
				text.set(this.text.get())
				ret = true
			}
			else {
				//親のフィールドと自分の値が異なる
				if(!this.memo.get().isNullOrBlank()) {
					//メモの入力があったので
					//入力を受け付ける
					text.set(this.text.get())
					ret = true
				}
				else {
					//メモの入力がない
					val msg = "相違しています。備考欄を入力してください"
					this.validationMsg.set(msg)
					ret = false
				}
			}
		}

		return ret
	}
}

