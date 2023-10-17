package com.bignerdranch.android.classsession

import androidx.lifecycle.ViewModel

class ClassSessionListViewModel : ViewModel() {
    private val classSessionRepository = ClassSessionRepository.get()
    val sessionListLiveData = classSessionRepository.getClassSessions()

    fun addClassSession(classSession: ClassSession) {
        classSessionRepository.addClassSession(classSession)
    }
    fun removeAllClassSessions() {
        classSessionRepository.removeAllClassSessions()
    }

}
