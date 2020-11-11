package com.v3.basis.blas.blasclass.db.data

import android.graphics.Bitmap

data class ItemImage (
  //project_imagesテーブルより
  var project_image_id:Long?=0,
  var project_id:Int?=0,
  var list:Int?=0,
  var field_id:Int?=0,
  var name:String?="",  //カラム名
  //imagesテーブルより
  var image_id:Long?=0,
  var filename:String?="",  //画像ファイル名
  var item_id:Long?=0,
  var moved:Int?=0,
  var rank:Int?=-1,
  var create_date:String?="",
  var bitmap: Bitmap? = null, //画像を取得したら入る
  var downloadProgress:Boolean = true
)
