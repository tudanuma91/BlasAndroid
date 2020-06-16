package com.v3.basis.blas.blasclass.models

interface LDBRecord {
    var recordStatus:Int    //仮登録中(新規追加)0、仮登録中(編集)1、仮登録集(削除)2,送信待ち3, 送信完了4
    var primary_key:String  //主キーの変数名を文字列で指定する
    var tableName:String    //テーブル名を文字列で指定する
}


data class LDBFixtureRecord(
    var fixture_id: Int = 0,             //主キー
    var project_id: Int = 0,             //プロジェクトID
    var fix_org_id: Int = 0,             //検品した会社ID
    var fix_org_name: String = "",       //検品した会社名
    var fix_user_id: Int = 0,            //検品したユーザID
    var fix_user_name: String = "",      //検品したユーザ名
    var fix_date: String = "",          //検品した日時
    var takeout_org_id: Int = 0,         //持ち出した会社ID
    var takeout_org_name: String = "",   //持ち出した会社前伊
    var takeout_user_id: Int = 0,        //持出者のID
    var takeout_user_name: String = "",  //持出者の名前
    var takeout_date: String = "",       //持ち出した日時
    var rtn_org_id: Int = 0,             //返却した会社ID
    var rtn_org_name: String = "",       //返却した会社の名前
    var rtn_user_id: Int = 0,            //返却したユーザＩＤ
    var rtn_user_name: String = "",      //返却したユーザ名
    var rtn_date: String = "",           //返却した日時
    var item_id: Int = 0,                //設置した機器が登録されているデータ管理の外部キー
    var item_org_id: Int = 0,            //設置した会社のＩＤ
    var item_org_name: String = "",      //設置した会社の名前
    var item_user_id: Int = 0,           //設置したユーザのＩＤ
    var item_user_name: String = "",     //設置したユーザ名
    var item_date: String = "",          //設置した日時
    var serial_number: String = "",      //シリアルナンバー
    var status: Int = 0,                 //0:検品済み(持ち出し可), 1:持ち出し中,2:設置済み, 3:持出不可
    var create_date: String = "",        //レコード作成日付
    var update_date: String = "",        //レコード更新日付
    override var recordStatus:Int = -1,
    override var primary_key:String = "fixture_id",
    override var tableName:String = "fixtures"
): LDBRecord
/*
data class ItemRecord(
    val item_id:Int,
    val project_id:Int,
    val user_id:Int,
    val user_name:String,
    val org_id:Int,
    val org_name:String,
    val lat:String, //何に使うかは不明だが
    val lng:String,
    val flds:Array<String>,
    val ee_enter:String,
    val ee_enter_location:String,
    val end_flg:Int,
    val work_flg:Int,
    val modify_user:String,
    val create_date:String,
    val update_date:String,
    override val recordStatus:Int
):LDBRecord

data class FieldRecord(
    val field_id:Int,
    val project_id:Int,
    val col:Int,
    val name:String,
    val type:Int,
    val choice:String,
    val alnum:Int,
    val notify:Int,
    val essential:Int,
    val input:Int,
    val export:Int,
    val other:Int,
    val map:Int,
    val address:Int,
    val filename:Int,
    val parent_field_id:Int,
    val summary:Int,
    val create_date:String,
    val update_date:String,
    val unique_chk:Int,
    val work_day:Int,
    override val recordStatus:Int
):LDBRecord

data class ProjectImageRecord(
    val project_image_id:Int,       //主キー
    val project_id:Int,             //プロジェクトID
    val name:String,                //ファイル名
    val rank:Int,                   //表示順序
    val list:Int,                   //一覧表示する場合1,一覧表示しない場合0
    val field_id:Int,               //ファイル名に出力する項目
    val create_date:String,         //作成日時
    val update_date:String,         //更新日時
    override val recordStatus:Int
):LDBRecord

data class ProjectFileRecord(
    val project_file_id:Int,        //主キー
    val project_id:Int,             //プロジェクトID
    val name:String,                //ファイル名
    val rank:Int,                   //表示順序
    val list:Int,                   //一覧表示する場合1,一覧表示しない場合0
    val create_date:String,         //作成日時
    val update_date:String,         //更新日時
    override val recordStatus:Int
):LDBRecord


data class FixtureRecord(
    val fixture_id:Int,            //主キー
    val project_id:Int,            //プロジェクトID
    val fix_org_id:Int,            //検品した会社ID
    val fix_org_name:String,       //検品した会社名
    val fix_user_id:Int,           //検品したユーザID
    val fix_user_name:String,      //検品したユーザ名
    val fix_date:String,           //検品した日時
    val takeout_org_id:Int,        //持ち出した会社ID
    val takeout_org_name:String,   //持ち出した会社前伊
    val takeout_user_id:Int,       //持出者のID
    val takeout_user_name:String,  //持出者の名前
    val takeout_date:String,       //持ち出した日時
    val rtn_org_id:Int,            //返却した会社ID
    val rtn_org_name:String,       //返却した会社の名前
    val rtn_user_id:Int,           //返却したユーザＩＤ
    val rtn_user_name:String,      //返却したユーザ名
    val rtn_date:String,           //返却した日時
    val item_id:Int,               //設置した機器が登録されているデータ管理の外部キー
    val item_org_id:Int,           //設置した会社のＩＤ
    val item_org_name:String,      //設置した会社の名前
    val item_user_id:Int,          //設置したユーザのＩＤ
    val item_user_name:String,     //設置したユーザ名
    val item_date:String,          //設置した日時
    val serial_number:String,      //シリアルナンバー
    val status:Int,                //0:検品済み(持ち出し可), 1:持ち出し中,2:設置済み, 3:持出不可
    val create_date:String,        //レコード作成日付
    val update_date:String,        //レコード更新日付
    override val recordStatus:Int
):LDBRecord{
    override fun toString(): String {
        return super.toString()
    }
}

data class RmFixtureRecord(
    val rm_fixture_id:Int,          //主キー
    val project_id:Int,             //プロジェクトID
    val rm_org_id:Int,              //撤去した会社ID
    val rm_org_name:String,         //撤去した会社の名前
    val rm_user_id:Int,             //撤去したユーザID
    val rm_user_name:String,        //撤去したユーザ名
    val rm_date:String,             //撤去した日時
    val rm_tmp_org_id:Int,          //一時保管した会社ID
    val rm_tmp_org_name:String,     //一時保管した会社名
    val rm_tmp_user_id:Int,         //一時保管したユーザID
    val rm_tmp_user_name:String,    //一時保管したユーザ名
    val rm_tmp_date:String,         //一時保管した日付
    val rm_comp_org_id:Int,         //撤去完了した会社ID
    val rm_comp_org_name:String,    //撤去完了した会社名
    val rm_comp_user_id:Int,        //撤去完了したユーザID
    val rm_comp_user_name:String,   //撤去完了したユーザ名
    val rm_comp_date:String,        //撤去完了した日時
    val item_id:Int,                //撤去した機器のシリアルナンバーを含むデータ管理のレコード(外部キー)
    val item_org_id:Int,            //撤去した機器のシリアルナンバーを含むデータ管理の会社ID
    val item_org_name:String,       //撤去した機器のシリアルナンバーを含むデータ管理の会社名
    val item_user_id:Int,           //撤去した機器のシリアルナンバーを含むデータ管理のユーザID
    val item_user_name:String,      //撤去した機器のシリアルナンバーを含むデータ管理のユーザ名
    val serial_number:String,       //シリアルナンバー
    val status:Int,                 //0:未撤去 1:現場撤去 2:一時保管 3:撤去完了
    override val recordStatus:Int
):LDBRecord

data class OrgRecord(
    val org_id:Int,                 //会社ID。主キー
    val name:String,                //会社名
    val kana:String,                //会社名のフリガナ
    val en_name:String,             //会社名の英文
    val type1_id:Int,               //株式会社など
    val type2_id:Int,               //株式会社など
    val zip1:String,                //郵便番号1
    val zip2:String,                //郵便番号2
    val zip:String,                 //郵便番号
    val pref_id:Int,                //都道府県のID
    val city:String,                //町名
    val address:String,             //住所
    val tel1:String,                //電話番号1
    val tel2:String,                //電話番号2
    val tel3:String,                //電話番号3
    val tel:String,                 //電話番号
    val fax1:String,                //FAX番号1
    val fax2:String,                //FAX番号2
    val fax3:String,                //FAX番号3
    val mail:String,                //メールアドレス
    val remark:String,              //備考
    val status_id:Int,              //0?
    val image:String,               //画像
    val create_user_id:Int,         //作成者のID
    val create_date:String,         //作成日
    val update_date:String,         //更新日
    override val recordStatus:Int
):LDBRecord

data class UserRecord(
    val user_id:Int,                //主キー
    val username:String,            //ログイン時のユーザ名
    val password:String,            //パスワードのハッシュ値
    val mail:String,                //メールアドレス
    val org_id:Int,                 //所属会社のID
    val name:String,                //表示名
    val group_id:Int,               //システム管理者:1 統括監理者:2 一般管理者:3 作業者:4
    val image:String,               //アイコン画像
    val sign:String,                //印影画像
    val status:Int,                 //ロック状態 0:正常　0以外:アカウントロック
    val w_count:Int,                //パスワード誤り回数
    val remark:String,              //備考
    val en_org_id:Int,              //統括監理者の管理対象会社のID
    val token:String,               //トークン
    val alive_date:String,          //最終ログイン日
    val lat:String,                 //経度
    val lng:String,                 //緯度
    val use_polemap:Int,            //?
    val working_item_id:Int,        //?
    val active_date:String,         //最終ログイン日
    val create_user_id:Int,         //ユーザ登録者
    val seed:String,                //仮登録パスワードのハッシュ値
    override val recordStatus:Int
):LDBRecord

data class DrawingRecord(
    val drawing_id:Int,             //主キー
    val project_id:Int,             //プロジェクトID
    val name:String,                //図面名
    val filename:String,            //ファイル名
    val user_id:Int,                //ユーザID
    val user_name:String,           //ユーザ名
    val org_id:Int,                 //会社ID
    val org_name:String,            //会社名
    val item_id:Int,                //データ管理ID
    val file_type:String,           //jpg,png,gifのどれか
    val file_path:String,           //図面ファイルのパス
    val spot_volume:Int,            //?
    val create_date:String,         //作成日
    val update_date:String,         //更新日
    val created:String,             //?
    val modified:String,            //?
    val drawing_category_id:Int,    //カテゴリID
    val drawing_sub_category_id:Int,//サブカテゴリID
    override val recordStatus:Int
):LDBRecord

data class DrawingCategoryRecord(
    val drawing_category_id:Int,    //主キー
    val project_id:Int,             //プロジェクトID
    val drawing_id:Int,             //外部キー
    val name:String,                //カテゴリ名
    val rank:String,                //順番
    val create_date:String,         //作成日
    val update_date:String,         //更新日
    override val recordStatus:Int
):LDBRecord

data class DrawingSubCategoryRecord(
    val drawing_sub_category_id: Int,   //主キー
    val drawing_category_id:Int,        //カテゴリの外部キー
    val project_id:Int,                 //プロジェクトID
    val drawing_id:Int,                 //図面の外部キー
    val name:String,                    //サブカテゴリ名
    val rank:Int,                       //表示順序
    val create_date:String,             //作成日
    val update_date:String,             //更新日
    override val recordStatus:Int
):LDBRecord


data class DrawingSpotRecord(
    val spot_id:Int,                    //主キー
    val project_id:Int,                 //プロジェクトID
    val item_id:Int,                    //データ管理ID
    val drawing_id:Int,                 //図面ID
    val user_id:Int,                    //ユーザID
    val org_id:Int,                     //会社ID
    val name:String,                    //ラベル名
    val shape_color:String,             //色
    val comment:String,                 //コメント
    val abscissa:String,                //横座標
    val ordinate:String,                //縦座標
    val link:String,                    //?
    val col:Int,                        //?
    val create_date:String,             //作成日
    val update_date:String,             //更新日
    override val recordStatus:Int
):LDBRecord
*/
