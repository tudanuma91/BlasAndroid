package com.v3.basis.blas.blasclass.analytics

/**
 * APIのカテゴリ定義クラス
 * [その他]
 * 必要に応じてイベントを追加してください
 */
enum class APIType(val type: String) {
    REST_API_GET("rest_get"),
    REST_API_CREATE("rest_create"),
    REST_API_EDIT("rest_edit"),
    REST_API_DELETE("rest_delete")
}
