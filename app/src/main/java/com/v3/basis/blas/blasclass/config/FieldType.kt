package com.v3.basis.blas.blasclass.config

open class FieldType {
    companion object {
        //データ型の定義
        const val TEXT_FIELD = "1"
        const val TEXT_AREA = "2"
        const val DATE_TIME = "3"
        const val TIME = "4"
        const val SINGLE_SELECTION = "5"
        const val MULTIPLE_SELECTION = "6"
        const val LOCATION = "7"
        const val KENPIN_RENDOU_QR = "8"
        const val SIG_FOX = "9"
        const val QR_CODE = "10"
        const val TEKKILYO_RENDOU_QR = "11"
        const val ACOUNT_NAME = "12"
        const val CHECK_VALUE = "13"

        //ユニークチェック
        const val UNIQUE_CHK = "unique_chk"
        const val TURE = "1"
        const val FALSE = "0"

        //ゴミ箱のデータ関連
        const val NORMAL = "0"
        const val END = "1"
        const val ENDTEXT = "(ゴミ箱のデータ)"

        //エラーメッセージ
        const val SEARCH_ERROR = "（※入力エラーです。!や\\等の記号は入力できません。）"
        const val REQUIRED = "(入力必須項目です。)"
    }
}
