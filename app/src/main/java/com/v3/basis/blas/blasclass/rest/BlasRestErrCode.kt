package com.v3.basis.blas.blasclass.rest

class BlasRestErrCode() {
    companion object {
        const val SUCCESS = 0

        //AUTH
        const val AUTH_INVALID_TOKEN = 1
        const val AUTH_TOKEN_EXPIRED = 2
        const val AUTH_INTERNAL_ERROR = 3
        const val AUTH_ALREADY_LOGOUT = 4
        const val AUTH_ACCOUNT_ERROR = 5
        const val AUTH_CREATE_TOKEN_ERROR = 6

        //権限
        const val PERM_NO_RIGHTS = 100

        //DB
        const val DB_NOT_FOUND_RECORD = 200
        const val DB_ERROR_ADD_RECORD = 201
        const val DB_ERROR_UPDATE_RECORD = 202
        const val DB_ERROR_DELETE_RECORD = 203

        //パラメータ―
        const val P_NOT_FOUND_PARAM = 300
        const val P_NOT_FOUND_ID = 301
        const val P_ERROR_SET_PARAM = 302
        const val P_INVALID_PARAM = 303
        const val P_NOT_FOUND_IMAGE = 304
        const val P_OUT_OF_RANGE = 305
        const val DATA_DUPLI_ERROR = 306
        const val SET_FX_ERROR = 307
        const val SET_REFX_ERROR = 308

        //機器管理
        const val FX_NOT_ENTRY_TAKEOUT = 400
        const val FX_DIFF_USER = 401
        const val FX_ALREDY_SET = 402
        const val FX_ALREADY_CHECK = 403
        const val FX_DONT_TAKEOUT = 404
        const val FX_NOT_ENTRY_FIXTURE = 405
        const val FX_ALREADY_TAKEOUT = 406
        const val FX_NOT_FOUND_FIXURE_ID = 407
        const val FX_ALREADY_RTN = 408

        //画像
        const val IMG_CANT_ACCESS = 500
        const val IMG_SAVE_ERROR = 501
        const val IMG_NOT_FOUND_DEL_IMG = 502
        const val IMG_HASH_ERROR = 503

        //通信
        const val NETWORK_ERROR = 1001

        //パース
        const val JSON_PARSE_ERROR = 2001

        //ファイル
        const val FILE_READ_ERROR = 3001

    }
}