package com.v3.basis.blas.blasclass.component

import android.content.Context

class DrawingImageComponent: ImageComponent() {
    override fun getSavedDir(context: Context, projectId:String):String {
        return context.dataDir.path + "/drawings/${projectId}/"
    }
}