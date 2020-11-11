package com.v3.basis.blas.blasclass.ldb

import com.v3.basis.blas.blasclass.db.BaseController


open class LdbSyncBase {
    var sync_status: Int = 0 //何も弄ってない0 仮登録中(新規追加)1、仮登録中(編集)2、仮登録集(削除)3,送信待ち4, 送信完了5
    var error_msg:String = ""
}

open class LdbFixtureRecord : LdbSyncBase() {
    var fixture_id: Long = 0             //主キー
    var project_id: Int = 0               //プロジェクトID
    var fix_org_id: Int = 0               //検品した会社ID
    var fix_user_id: Int = 0             //検品したユーザID
    var fix_date: String = ""               //検品した日時
    var takeout_org_id: Int = 0       //持ち出した会社ID
    var takeout_user_id: Int = 0        //持出者のID
    var takeout_date: String = ""       //持ち出した日時
    var rtn_org_id: Int = 0               //返却した会社ID
    var rtn_user_id: Int = 0             //返却したユーザＩＤ
    var rtn_date: String = ""               //返却した日時
    var item_id: Long = 0L                  //設置した機器が登録されているデータ管理の外部キー
    var item_org_id: Int = 0             //設置した会社のＩＤ
    var item_user_id: Int = 0           //設置したユーザのＩＤ
    var item_date: String = ""             //設置した日時
    var serial_number: String = ""     //シリアルナンバー
    var status: Int = 0                       //0:検品済み(持ち出し可), 1:持ち出し中,2:設置済み, 3:持出不可 4:返却
    var create_date: String = ""         //レコード作成日付
    var update_date: String = ""         //レコード更新日付

    fun toPayLoad():MutableMap<String, String> {
        val payload = mutableMapOf<String, String>()
        payload["fixture_id"] = fixture_id.toString()
        payload["project_id"] = project_id.toString()
        payload["serial_number"] = serial_number
        payload["update_date"] = update_date
        payload["sync_status"] = sync_status.toString()

        when(status){
            BaseController.KENPIN_FIN -> {
                payload["fix_org_id"] = fix_org_id.toString()
                payload["fix_user_id"] =  fix_user_id.toString()
                payload["fix_date"] =  fix_date
            }
            BaseController.TAKING_OUT ->{
                payload["takeout_org_id"] = takeout_org_id.toString()
                payload["takeout_user_id"] = takeout_user_id.toString()
                payload["takeout_date"] = takeout_date
            }
            BaseController.RTN->{
                payload["rtn_org_id"] = rtn_org_id.toString()
                payload["rtn_user_id"] = rtn_user_id.toString()
                payload["rtn_date"] = rtn_date
            }
        }


        if( BaseController.SYNC_STATUS_NEW  == sync_status ) {
            payload["create_date"] = create_date
        }

        return payload
    }

}

class LdbFixtureDispRecord : LdbFixtureRecord() {
    var fix_org_name: String = ""       //検品した会社名
    var fix_user_name: String = ""     //検品したユーザ名
    var takeout_org_name: String = ""   //持ち出した会社前伊
    var takeout_user_name: String = ""  //持出者の名前
    var rtn_org_name: String = ""       //返却した会社の名前
    var rtn_user_name: String = ""     //返却したユーザ名
    var item_org_name: String = ""     //設置した会社の名前
    var item_user_name: String = ""   //設置したユーザ名
}

class LdbUserRecord {
    var user_id: Int = 0                //主キー
    var username: String = ""            //ログイン時のユーザ名
    var org_id: Int = 0                 //所属会社のID
    var name: String = ""                //表示名
    var group_id: Int = 0               //システム管理者:1 統括監理者:2 一般管理者:3 作業者:4
    var image: String = ""               //アイコン画像
    var sign: String = ""                //印影画像
    var status: Int = 0                 //ロック状態 0:正常　0以外:アカウントロック
    var w_count: Int = 0                //パスワード誤り回数
    var remark: String = ""              //備考
    var en_org_id: String = ""              //統括監理者の管理対象会社のID
    var alive_date: String = ""          //最終ログイン日
    var lat: String = ""                 //経度
    var lng: String = ""                 //緯度
    var use_polemap: Int = 0            //?
    var working_item_id: Int = 0        //?
    var active_date: String = ""         //最終ログイン日
    var create_user_id: Int = 0         //ユーザ登録者
}

open class LdbRmFixtureRecord : LdbSyncBase() {
    var rm_fixture_id: Int = 0          //主キー
    var project_id: Int = 0             //プロジェクトID
    var rm_org_id: Int = 0              //撤去した会社ID
    var rm_user_id: Int = 0             //撤去したユーザID
    var rm_date: String = ""             //撤去した日時
    var rm_tmp_org_id: Int = 0          //一時保管した会社ID
    var rm_tmp_user_id: Int = 0         //一時保管したユーザID
    var rm_tmp_date: String = ""         //一時保管した日付
    var rm_comp_org_id: Int = 0         //撤去完了した会社ID
    var rm_comp_user_id: Int = 0        //撤去完了したユーザID
    var rm_comp_date: String = ""        //撤去完了した日時
    var item_id: Long = 0                //撤去した機器のシリアルナンバーを含むデータ管理のレコード(外部キー)
    var item_org_id: Int = 0            //撤去した機器のシリアルナンバーを含むデータ管理の会社ID
    var item_user_id: Int = 0           //撤去した機器のシリアルナンバーを含むデータ管理のユーザID
    var serial_number: String = ""       //シリアルナンバー
    var status: Int = 0                 //0:未撤去 1:現場撤去 2:一時保管 3:撤去完了
}

class LdbRmFixtureDispRecord : LdbRmFixtureRecord() {
    var rm_org_name: String = ""         //撤去した会社の名前
    var rm_user_name: String = ""        //撤去したユーザ名
    var rm_tmp_org_name: String = ""     //一時保管した会社名
    var rm_tmp_user_name: String = ""    //一時保管したユーザ名
    var rm_comp_org_name: String = ""    //撤去完了した会社名
    var rm_comp_user_name: String = ""   //撤去完了したユーザ名
    var item_org_name: String = ""       //撤去した機器のシリアルナンバーを含むデータ管理の会社名
    var item_user_name: String = ""      //撤去した機器のシリアルナンバーを含むデータ管理のユーザ名

}


class LdbFieldRecord(
    var field_id:Int? = 0,
    var project_id:Int? = 0,
    var col:Int? = 0,
    var name:String? = null,
    var type:Int? = 0,
    var choice:String? = null,
    var alnum:Int? = 0,
    var notify:Int? = 0,
    var essential:Int? = 0,
    var input:Int? = 0,
    var export:Int? = 0,
    var other:Int? = 0,
    var map:Int? = 0,
    var address:Int? = 0,
    var filename:Int? = 0,
    var parent_field_id:Int? = 0,
    var summary:Int? = 0,
    var create_date:String? = null,
    var update_date:String? = null,
    var unique_chk:Int? = 0,
    var work_day:Int? = 0,
    var edit_id:Int? = 0
)

class LdbImageRecord(
    var image_id: Long? = 0,
    var project_id: Int? = 0,
    var project_image_id: Int? = 0,
    var item_id: Long? = 0,
    var filename: String = "",
    var hash: String = "",
    var moved: Int? = 0,
    var create_date: String = "",
    var sync_status: Int? = 0,
    var error_msg:String = ""
){
    fun toPayLoad():MutableMap<String, String> {
        val payload = mutableMapOf<String, String>()
        payload["image_id"] = image_id.toString()
        payload["project_id"] = project_id.toString()
        payload["item_id"] = item_id.toString()
        payload["filename"] = filename
        payload["hash"] = hash
        payload["moved"] = moved.toString()
        payload["create_date"] = create_date
        payload["sync_status"] = sync_status.toString()
        payload["error_msg"] = error_msg

        return payload
    }
}