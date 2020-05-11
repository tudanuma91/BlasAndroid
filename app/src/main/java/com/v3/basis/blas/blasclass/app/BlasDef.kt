package com.v3.basis.blas.blasclass.app

class BlasDef {
    companion object {
        const val PARAM_FILE_DIR = "app/data/params/"
        const val BLAS_URL = "http://192.168.1.8/api/v1/"
        const val REQUEST_TABLE = "RequestTable"
        const val NOTICE_TABLE = "NoticeTable"
        const val READ_TIME_OUT_POST = 100000
        const val STS_RETRY_MAX = 9

        //APLコード
        const val APL_OK = 0          // queue処理正常終了
        const val APL_QUEUE_SAVE = 900 // queueへの登録を表す
        const val APL_QUEUE_ERR = 901
        const val APL_RETRY_MAX_ERR = 902
        const val APL_SERVER_ERROR = 903

        const val FUNC_NAME_FIXTURE = "機器管理"
        const val FUNC_NAME_ITEM = "データ管理"
        const val OPE_NAME_UPDATE = "更新"
        const val OPE_NAME_ADD = "追加"
        const val OPE_NAME_UPLOAD = "画像アップロード"
        const val OPE_NAME_KENPIN = "検品"
        const val OPE_NAME_TAKEOUT = "持出"
        const val OPE_NAME_RTN = "返却"

        // ボタンのラベル名
        const val BTN_SAVE = "保存"
        const val BTN_FIND = "検索"

    }
}