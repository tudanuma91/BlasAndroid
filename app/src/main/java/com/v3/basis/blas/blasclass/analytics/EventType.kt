package com.v3.basis.blas.blasclass.analytics

/**
 * イベントのカテゴリ定義クラス
 * [その他]
 * 必要に応じてイベントを追加してください
 */
enum class EventType(val eventName: String) {
    LOGIN("login"),
    LOGOUT("logout"),
    NAVIGATE_SCREEN("navigate_screen"),   //画面遷移イベントで指定
    BOTTOM_NAVIGATION("bottom_navigation"),  //Bottomナビゲーションの切り替えボタンイベント
}
