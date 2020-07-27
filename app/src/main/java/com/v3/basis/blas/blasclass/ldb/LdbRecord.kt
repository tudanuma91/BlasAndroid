package com.v3.basis.blas.blasclass.ldb

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType


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
    var status: Int = 0                       //0:検品済み(持ち出し可), 1:持ち出し中,2:設置済み, 3:持出不可
    var create_date: String = ""         //レコード作成日付
    var update_date: String = ""         //レコード更新日付
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

/*
class LdbItemRecord : LdbSyncBase() {
    var item_id: Long = 0
    var project_id: Int = 0
    var org_id: Int = 0
    var user_id: Int = 0
    var lat: Float = 0.0f
    var lng: Float = 0.0f
    var fld1: String = ""
    var fld2: String = ""
    var fld3: String = ""
    var fld4: String = ""
    var fld5: String = ""
    var fld6: String = ""
    var fld7: String = ""
    var fld8: String = ""
    var fld9: String = ""
    var fld10: String = ""
    var fld11: String = ""
    var fld12: String = ""
    var fld13: String = ""
    var fld14: String = ""
    var fld15: String = ""
    var fld16: String = ""
    var fld17: String = ""
    var fld18: String = ""
    var fld19: String = ""
    var fld20: String = ""
    var fld21: String = ""
    var fld22: String = ""
    var fld23: String = ""
    var fld24: String = ""
    var fld25: String = ""
    var fld26: String = ""
    var fld27: String = ""
    var fld28: String = ""
    var fld29: String = ""
    var fld30: String = ""
    var fld31: String = ""
    var fld32: String = ""
    var fld33: String = ""
    var fld34: String = ""
    var fld35: String = ""
    var fld36: String = ""
    var fld37: String = ""
    var fld38: String = ""
    var fld39: String = ""
    var fld40: String = ""
    var fld41: String = ""
    var fld42: String = ""
    var fld43: String = ""
    var fld44: String = ""
    var fld45: String = ""
    var fld46: String = ""
    var fld47: String = ""
    var fld48: String = ""
    var fld49: String = ""
    var fld50: String = ""
    var fld51: String = ""
    var fld52: String = ""
    var fld53: String = ""
    var fld54: String = ""
    var fld55: String = ""
    var fld56: String = ""
    var fld57: String = ""
    var fld58: String = ""
    var fld59: String = ""
    var fld60: String = ""
    var fld61: String = ""
    var fld62: String = ""
    var fld63: String = ""
    var fld64: String = ""
    var fld65: String = ""
    var fld66: String = ""
    var fld67: String = ""
    var fld68: String = ""
    var fld69: String = ""
    var fld70: String = ""
    var fld71: String = ""
    var fld72: String = ""
    var fld73: String = ""
    var fld74: String = ""
    var fld75: String = ""
    var fld76: String = ""
    var fld77: String = ""
    var fld78: String = ""
    var fld79: String = ""
    var fld80: String = ""
    var fld81: String = ""
    var fld82: String = ""
    var fld83: String = ""
    var fld84: String = ""
    var fld85: String = ""
    var fld86: String = ""
    var fld87: String = ""
    var fld88: String = ""
    var fld89: String = ""
    var fld90: String = ""
    var fld91: String = ""
    var fld92: String = ""
    var fld93: String = ""
    var fld94: String = ""
    var fld95: String = ""
    var fld96: String = ""
    var fld97: String = ""
    var fld98: String = ""
    var fld99: String = ""
    var fld100: String = ""
    var fld101: String = ""
    var fld102: String = ""
    var fld103: String = ""
    var fld104: String = ""
    var fld105: String = ""
    var fld106: String = ""
    var fld107: String = ""
    var fld108: String = ""
    var fld109: String = ""
    var fld110: String = ""
    var fld111: String = ""
    var fld112: String = ""
    var fld113: String = ""
    var fld114: String = ""
    var fld115: String = ""
    var fld116: String = ""
    var fld117: String = ""
    var fld118: String = ""
    var fld119: String = ""
    var fld120: String = ""
    var fld121: String = ""
    var fld122: String = ""
    var fld123: String = ""
    var fld124: String = ""
    var fld125: String = ""
    var fld126: String = ""
    var fld127: String = ""
    var fld128: String = ""
    var fld129: String = ""
    var fld130: String = ""
    var fld131: String = ""
    var fld132: String = ""
    var fld133: String = ""
    var fld134: String = ""
    var fld135: String = ""
    var fld136: String = ""
    var fld137: String = ""
    var fld138: String = ""
    var fld139: String = ""
    var fld140: String = ""
    var fld141: String = ""
    var fld142: String = ""
    var fld143: String = ""
    var fld144: String = ""
    var fld145: String = ""
    var fld146: String = ""
    var fld147: String = ""
    var fld148: String = ""
    var fld149: String = ""
    var fld150: String = ""
    var ee_enter: String = ""
    var ee_exit: String = ""
    var ee_enter_location: String = ""
    var ee_exit_location: String = ""
    var temp: String = ""
    var end_flg: Int = 0
    var work_flg: Int = 0
    var modified_user: Int = 0
    var create_date: String = ""
    var update_date: String = ""
}
*/



data class LDBRmFixtureRecord(
    var rm_fixture_id:Int? =0,          //主キー
    var project_id:Int? =0,             //プロジェクトID
    var rm_org_id:Int? = 0,              //撤去した会社ID
    var rm_org_name:String? = null,         //撤去した会社の名前
    var rm_user_id:Int? = 0,             //撤去したユーザID
    var rm_user_name:String? = null,        //撤去したユーザ名
    var rm_date:String? = null,             //撤去した日時
    var rm_tmp_org_id:Int? = 0,          //一時保管した会社ID
    var rm_tmp_org_name:String? = null,     //一時保管した会社名
    var rm_tmp_user_id:Int? = 0,         //一時保管したユーザID
    var rm_tmp_user_name:String? = null,    //一時保管したユーザ名
    var rm_tmp_date:String? = null,         //一時保管した日付
    var rm_comp_org_id:Int? = 0,         //撤去完了した会社ID
    var rm_comp_org_name:String? = null,    //撤去完了した会社名
    var rm_comp_user_id:Int? = 0,        //撤去完了したユーザID
    var rm_comp_user_name:String? = null,   //撤去完了したユーザ名
    var rm_comp_date:String? = null,        //撤去完了した日時
    var item_id:Long? = 0,                //撤去した機器のシリアルナンバーを含むデータ管理のレコード(外部キー)
    var item_org_id:Int? = 0,            //撤去した機器のシリアルナンバーを含むデータ管理の会社ID
    var item_org_name:String? = null,       //撤去した機器のシリアルナンバーを含むデータ管理の会社名
    var item_user_id:Int? = 0,           //撤去した機器のシリアルナンバーを含むデータ管理のユーザID
    var item_user_name:String? = null,      //撤去した機器のシリアルナンバーを含むデータ管理のユーザ名
    var serial_number:String? = null,       //シリアルナンバー
    var status:Int? = 0,                 //0:未撤去 1:現場撤去 2:一時保管 3:撤去完了
    var sync_status:Int? = 0
)


data class LdbFieldRecord(
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

