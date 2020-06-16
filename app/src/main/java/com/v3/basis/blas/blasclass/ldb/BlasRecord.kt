package com.v3.basis.blas.blasclass.ldb
import androidx.room.*
import io.reactivex.Completable

interface LDBRecord {
    var recordStatus:Int    //何も弄ってない0 仮登録中(新規追加)1、仮登録中(編集)2、仮登録集(削除)3,送信待ち4, 送信完了5
    var primary_key:String  //主キーの変数名を文字列で指定する
    var tableName:String    //テーブル名を文字列で指定する
}

@Entity
data class Fixture(
    @PrimaryKey(autoGenerate = true)
    var fixture_id: Int = 0,             //主キー
    @ColumnInfo(name = "project_id") var project_id: Int = 0,               //プロジェクトID
    @ColumnInfo(name = "fix_org_id") var fix_org_id: Int = 0,               //検品した会社ID
    @ColumnInfo(name = "fix_org_name") var fix_org_name: String = "",       //検品した会社名
    @ColumnInfo(name = "fix_user_id") var fix_user_id: Int = 0,             //検品したユーザID
    @ColumnInfo(name = "fix_user_name") var fix_user_name: String = "",     //検品したユーザ名
    @ColumnInfo(name = "fix_date") var fix_date: String = "",               //検品した日時
    @ColumnInfo(name = "takeout_org_id") var takeout_org_id: Int = 0,       //持ち出した会社ID
    @ColumnInfo(name = "takeout_org_name") var takeout_org_name: String = "",   //持ち出した会社前伊
    @ColumnInfo(name = "takeout_user_id") var takeout_user_id: Int = 0,        //持出者のID
    @ColumnInfo(name = "takeout_user_name") var takeout_user_name: String = "",  //持出者の名前
    @ColumnInfo(name = "takeout_date") var takeout_date: String = "",       //持ち出した日時
    @ColumnInfo(name = "rtn_org_id") var rtn_org_id: Int = 0,               //返却した会社ID
    @ColumnInfo(name = "rtn_org_name") var rtn_org_name: String = "",       //返却した会社の名前
    @ColumnInfo(name = "rtn_user_id") var rtn_user_id: Int = 0,             //返却したユーザＩＤ
    @ColumnInfo(name = "rtn_user_name") var rtn_user_name: String = "",     //返却したユーザ名
    @ColumnInfo(name = "rtn_date") var rtn_date: String = "",               //返却した日時
    @ColumnInfo(name = "item_id") var item_id: Int = 0,                     //設置した機器が登録されているデータ管理の外部キー
    @ColumnInfo(name = "item_org_id") var item_org_id: Int = 0,             //設置した会社のＩＤ
    @ColumnInfo(name = "item_org_name") var item_org_name: String = "",     //設置した会社の名前
    @ColumnInfo(name = "item_user_id") var item_user_id: Int = 0,           //設置したユーザのＩＤ
    @ColumnInfo(name = "item_user_name") var item_user_name: String = "",   //設置したユーザ名
    @ColumnInfo(name = "item_date") var item_date: String = "",             //設置した日時
    @ColumnInfo(name = "serial_number") var serial_number: String = "",     //シリアルナンバー
    @ColumnInfo(name = "status") var status: Int = 0,                       //0:検品済み(持ち出し可), 1:持ち出し中,2:設置済み, 3:持出不可
    @ColumnInfo(name = "create_date") var create_date: String = "",         //レコード作成日付
    @ColumnInfo(name = "update_date") var update_date: String = "",         //レコード更新日付
    @ColumnInfo(name = "recordStatus") var recordStatus:Int = -1            //何も弄ってない0 仮登録中(新規追加)1、仮登録中(編集)2、仮登録集(削除)3,送信待ち4, 送信完了5
)


data class LDBItemRecord(
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
    override var recordStatus:Int = -1,
    override var primary_key:String = "item_id",
    override var tableName:String = "items"
):LDBRecord


data class LDBFieldRecord(
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
    override var recordStatus:Int = -1,
    override var primary_key:String = "field_id",
    override var tableName:String = "fields"
):LDBRecord


data class LDBProjectImageRecord(
    val project_image_id:Int,       //主キー
    val project_id:Int,             //プロジェクトID
    val name:String,                //ファイル名
    val rank:Int,                   //表示順序
    val list:Int,                   //一覧表示する場合1,一覧表示しない場合0
    val field_id:Int,               //ファイル名に出力する項目
    val create_date:String,         //作成日時
    val update_date:String,         //更新日時
    override var recordStatus:Int = -1,
    override var primary_key:String = "project_image_id",
    override var tableName:String = "project_images"
):LDBRecord


data class LDBProjectFileRecord(
    val project_file_id:Int,        //主キー
    val project_id:Int,             //プロジェクトID
    val name:String,                //ファイル名
    val rank:Int,                   //表示順序
    val list:Int,                   //一覧表示する場合1,一覧表示しない場合0
    val create_date:String,         //作成日時
    val update_date:String,         //更新日時
    override var recordStatus:Int = -1,
    override var primary_key:String = "project_file_id",
    override var tableName:String = "project_files"
):LDBRecord


data class LDBRmFixtureRecord(
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
    override var recordStatus:Int = -1,
    override var primary_key:String = "rm_fixture_id",
    override var tableName:String = "rm_fixtures"
):LDBRecord


data class LDBOrgRecord(
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
    override var recordStatus:Int = -1,
    override var primary_key:String = "org_id",
    override var tableName:String = "orgs"
):LDBRecord


data class LDBUserRecord(
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
    override var recordStatus:Int = -1,
    override var primary_key:String = "user_id",
    override var tableName:String = "users"
):LDBRecord


data class LDBDrawingRecord(
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
    override var recordStatus:Int = -1,
    override var primary_key:String = "drawing_id",
    override var tableName:String = "drawings"
):LDBRecord


data class LDBDrawingCategoryRecord(
    val drawing_category_id:Int,    //主キー
    val project_id:Int,             //プロジェクトID
    val drawing_id:Int,             //外部キー
    val name:String,                //カテゴリ名
    val rank:String,                //順番
    val create_date:String,         //作成日
    val update_date:String,         //更新日
    override var recordStatus:Int = -1,
    override var primary_key:String = "drawing_category_id",
    override var tableName:String = "drawing_categories"
):LDBRecord


data class LDBDrawingSubCategoryRecord(
    val drawing_sub_category_id: Int,   //主キー
    val drawing_category_id:Int,        //カテゴリの外部キー
    val project_id:Int,                 //プロジェクトID
    val drawing_id:Int,                 //図面の外部キー
    val name:String,                    //サブカテゴリ名
    val rank:Int,                       //表示順序
    val create_date:String,             //作成日
    val update_date:String,             //更新日
    override var recordStatus:Int = -1,
    override var primary_key:String = "drawing_sub_category_id",
    override var tableName:String = "drawing_sub_categories"
):LDBRecord


data class LDBDrawingSpotRecord(
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
    override var recordStatus:Int = -1,
    override var primary_key:String = "spot_id",
    override var tableName:String = "drawing_spots"
):LDBRecord

