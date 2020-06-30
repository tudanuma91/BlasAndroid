package com.v3.basis.blas.blasclass.config

class FieldType {
    companion object {
        //データ型の定義
        val TEXT_FIELD = "1"
        val TEXT_AREA = "2"
        val DATE_TIME = "3"
        val TIME = "4"
        val SINGLE_SELECTION = "5"
        val MULTIPLE_SELECTION = "6"
        val KENPIN_RENDOU_QR = "8"
        val SIG_FOX = "9"
        val QR_CODE = "10"
        val TEKKILYO_RENDOU_QR = "11"
        val ACOUNT_NAME = "12"
        val CHECK_VALUE = "13"

        //ユニークチェック
        val UNIQUE_CHK = "unique_chk"
        val TURE = "1"
        val FALSE = "0"

        //ゴミ箱のデータ関連
        val NORMAL = "0"
        val END = "1"
        val ENDTEXT = "(ゴミ箱のデータ)"

        //エラーメッセージ
        val SEARCH_ERROR = "（※入力エラーです。!や\\等の記号は入力できません。）"
        val REQUIRED = "(入力必須項目です。)"
    }
}
