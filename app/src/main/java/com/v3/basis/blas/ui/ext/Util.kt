package com.v3.basis.blas.ui.ext

import com.v3.basis.blas.blasclass.analytics.BlasLogger


fun traceLog(msg: String) {
    BlasLogger.logEvent(Throwable(), msg)
}
