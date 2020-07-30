package com.v3.basis.blas.ui.terminal

import androidx.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject

class TerminalViewModel : ViewModel() {

    val filterEvent: PublishSubject<String> = PublishSubject.create()
    val refreshEvent: PublishSubject<Unit> = PublishSubject.create()

    fun filterProject(text: String, refresh: Boolean = false) {
        filterEvent.onNext(text)
    }
}
