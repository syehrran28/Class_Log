package com.bignerdranch.android.classsession

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.classsession.database.ClassSessionDao
import com.bignerdranch.android.classsession.database.ClassSessionDatabase
import com.bignerdranch.android.classsession.database.migration_3_4
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val DATABASE_NAME = "class_session-database"

class ClassSessionRepository private constructor(context: Context) {

    private val database: ClassSessionDatabase = Room.databaseBuilder(
        context.applicationContext,
        ClassSessionDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_3_4)
        .build()

    private val classSessionDao: ClassSessionDao = database.classSessionDao()

    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun getClassSessions(): LiveData<List<ClassSession>> = classSessionDao.getClassSessions()

    fun getClassSession(id: UUID): LiveData<ClassSession?> = classSessionDao.getClassSession(id)

    fun updateClassSession(classSession: ClassSession) {
        executor.execute {
            classSessionDao.updateClassSession(classSession)
        }
    }
    fun removeAllClassSessions() {
        executor.execute {
            classSessionDao.removeAllClassSessions()
        }
    }


    fun addClassSession(classSession: ClassSession) {
        executor.execute {
            classSessionDao.addClassSession(classSession)
        }
    }


    companion object {
        private var INSTANCE: ClassSessionRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = ClassSessionRepository(context)
            }
        }

        fun get(): ClassSessionRepository {
            return INSTANCE ?: throw IllegalStateException("ClassSessionRepository must be initialized")
        }
    }

}
