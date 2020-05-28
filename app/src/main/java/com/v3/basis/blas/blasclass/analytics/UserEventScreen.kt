package com.v3.basis.blas.blasclass.analytics

/**
 * ユーザーイベントの画面定義クラス
 * [その他]
 * 必要に応じて画面タイプを追加してください
 */
enum class UserEventScreen(val screenType: String) {
    DASHBOARD("dashboard"),
    PROJECT_LIST("project_list"),
    FIXTURE_PROJECT_LIST("fixture_project_list"),
    LOGOUT("logout"),
    LOGIN("login"),
    FIXTURE_LIST("fixture_data_list"),
    FIXTURE_KENPIN("fixture_kenpin"),
    FIXTURE_TAKEOUT("fixture_takeout"),
    FIXTURE_RETURN("fixture_return"),
    FIXTURE_SEARCH("fixture_search"),
    FIXTURE_SEARCH_RESULT("fixture_search_result"),
    ITEM_LIST("item_list"),
    ITEM_CREATE("item_create"),
    ITEM_EDIT("item_edit"),
    ITEM_SEARCH("item_search"),
    ITEM_SEARCH_RESULT("item_search_result"),
    ITEM_IMAGE("item_image"),
    DIALOG("dialog"),
}
