package com.v3.basis.blas.blasclass.service

import android.os.IBinder
import android.os.Message
import android.os.Messenger

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
        }

        fun notifyBlasItems(token:String, projectId:String) {
            instance?.send(
                Message.obtain(null, 0, MsgParams(token, projectId,
                    SenderHandler.ITEM
                )))
        }

        fun notifyBlasImages(token:String, projectId:String) {
            instance?.send(
                Message.obtain(null, 0, MsgParams(token, projectId,
                    SenderHandler.IMAGE
                )))
        }

        fun syncBlasAll(token:String, projectId:String) {
            instance?.send(
                Message.obtain(null, 0, MsgParams(token, projectId,
                    SenderHandler.FIXTURE or SenderHandler.ITEM or SenderHandler.IMAGE
                )))
        }
    }
}