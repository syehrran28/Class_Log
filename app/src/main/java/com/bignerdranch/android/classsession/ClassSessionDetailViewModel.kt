package com.bignerdranch.android.classsession

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class ClassSessionDetailViewModel : ViewModel() {
    private val classSessionRepository: ClassSessionRepository = ClassSessionRepository.get()
    private val sessionIdLiveData = MutableLiveData<UUID>()

    var classSessionLiveData: LiveData<ClassSession?> = Transformations.switchMap(sessionIdLiveData) {
        classSessionRepository.getClassSession(it)
    }

    fun loadSession(sessionId: UUID) {
        sessionIdLiveData.value = sessionId
    }

    fun saveSession(classSession: ClassSession) {
        classSessionRepository.updateClassSession(classSession)
    }
}
