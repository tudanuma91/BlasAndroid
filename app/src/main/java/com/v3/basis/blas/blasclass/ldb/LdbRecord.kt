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
    var item_id: Int = 0                     //設置した機器が登録されているデータ管理の外部キー
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
