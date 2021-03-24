package com.v3.basis.blas.blasclass.service

import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.v3.basis.blas.blasclass.log.BlasLog

class BlasSyncMessenger() {

    companion object {
        private var instance:Messenger? = null
        private var service:IBinder? = null

        fun getInstance(service: IBinder):Messenger? {
            this.service = service
            if(instance == null) {
                instance = Messenger(service);
            }
            return instance
        }

        fun getInstance():Messenger? {
            return instance
        }

        fun notifyBlasFixtures(token:String, projectId:String) {
            instance?.send(
                    Message.obtain(null, 0, MsgParams(token, projectId,
                    SenderHandler.FIXTURE
                )))
            BlasLog.trace("I", "機器管理再送イベントを発行しました")
        }

        fun notifyBlasItems(token:String, projectId:String) {
            instance?.send(
                Message.obtain(null, 0, MsgParams(token, projectId,
                    SenderHandler.ITEM
                )))
            BlasLog.trace("I", "データ管理再送イベントを発行しました")
        }

        fun notifyBlasImages(token:String, projectId:String) {
            instance?.send(
                Message.obtain(null, 0, MsgParams(token, projectId,
                    SenderHandler.IMAGE
                )))
            BlasLog.trace("I", "画像再送イベントを発行しました")
        }

        fun notifyBlasEvents(token:String, projectId:String) {
            instance?.send(
                Message.obtain(null, 0, MsgParams(token, projectId,
                    SenderHandler.EVENT
                )))
            BlasLog.trace("I", "データ管理再送イベントを発行しました")
        }

        fun syncBlasAll(token:String, projectId:String) {
            instance?.send(
                Message.obtain(null, 0, MsgParams(token, projectId,
                    SenderHandler.FIXTURE or SenderHandler.ITEM or SenderHandler.IMAGE
                )))
            BlasLog.trace("I", "再送イベントを発行しました")
        }
    }
}