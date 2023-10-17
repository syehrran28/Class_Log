package com.bignerdranch.android.classsession

import android.app.Application

class ClassSessionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ClassSessionRepository.initialize(this)
    }
}
