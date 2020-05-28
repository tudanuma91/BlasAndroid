package com.v3.basis.blas.blasclass.analytics

import android.os.Bundle
import android.text.format.DateUtils.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.v3.basis.blas.blasclass.app.BlasApp


/**
 * ユーザーのイベントログを保存する為のクラス
 * FirebaseAnalyticsを内部で呼び出す
 */
object BlasAnalytics {

    private const val USER_TOKEN = "user_token"
    private const val TRACE_LOG = "trace_log"
    private const val LOG_DETAILS = "log_details"
    private const val EVENT_TYPE_ACTION = "event_action"
    private const val EVENT_TYPE_ACTION_DETAIL = "event_action_detail"
    private const val EVENT_TYPE_DISPLAY_SCREEN = "display_screen"
    private const val SCREEN_NAME = "screen_name"
    private const val SCREEN_NAME_DETAIL = "screen_name_detail"

    private const val REST_API_NAME = "rest_api_name"
    private const val REST_API_CRUD = "rest_api_crud"


    /**
     * ログ保存処理
     * [引き数]
     * screen: UserEventScreen　表示中の画面を指定
     * type: EventType, 実行したイベントを指定
     * option: String　その他のイベントを追加したい場合、追加する
     */
    fun logEvent(t: Throwable, msg: String = "") {

        val date = formatDateTime(
            BlasApp.applicationContext(),
            System.currentTimeMillis(),
            FORMAT_SHOW_YEAR or FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY or FORMAT_ABBREV_ALL
        )
        val trace = t.stackTrace[0]
        val log = "$date ${BlasApp.token} TraceFile:${trace.fileName} TraceClass:${trace.className} function:${trace.methodName} line:${trace.lineNumber} msg:$msg"
//        val bundle = Bundle()
//        bundle.putString(USER_TOKEN, BlasApp.token)
//        bundle.putString(LOG_DETAILS, log)
//        BlasApp.analytics().logEvent(TRACE_LOG, bundle)
        FirebaseCrashlytics.getInstance().log(log)
    }

    /**
     * API実行時のログ保存処理
     * [引き数]
     * screen: UserEventScreen　表示中の画面を指定
     * className: String,
     * crud:String　
     */
    fun logRestGet(screen: UserEventScreen, apiType: APIType, className: String, crud:String) {

        val bundle = Bundle()
        bundle.putString(EVENT_TYPE_ACTION, apiType.type)
        bundle.putString(REST_API_NAME, className)
        bundle.putString(REST_API_CRUD, crud)
        BlasApp.analytics().logEvent(screen.screenType, bundle)
    }

    /**
     * ログ保存処理
     * [引き数]
     * screen: UserEventScreen　表示中の画面を指定
     * type: EventType, 実行したイベントを指定
     * option: String　その他のイベントを追加したい場合、追加する
     */
    fun logBottomNavigation(from: UserEventScreen, to: UserEventScreen) {

        val bundle = Bundle()
        bundle.putString(EVENT_TYPE_ACTION, EventType.BOTTOM_NAVIGATION.eventName)
        bundle.putString(EVENT_TYPE_ACTION_DETAIL, to.screenType)
        BlasApp.analytics().logEvent(from.screenType, bundle)
    }

    /**
     * 画面表示をカウントするためのログ保存処理
     * Activity,Fragment,Dialogで画面表示されたタイミングで利用する想定です
     * [引き数]
     * screen: UserEventScreen　表示中の画面を指定
     * screenDetail: String　詳細を指定したい場合
     */
    fun logScreenDisplayed(screen: UserEventScreen, screenDetail: String?) {

        val bundle = Bundle()
        bundle.putString(SCREEN_NAME, screen.screenType)
        screenDetail?.also { bundle.putString(SCREEN_NAME_DETAIL, screenDetail) }
        BlasApp.analytics().logEvent(EVENT_TYPE_DISPLAY_SCREEN, bundle)
    }
}
