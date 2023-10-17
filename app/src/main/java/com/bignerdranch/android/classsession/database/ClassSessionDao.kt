package com.bignerdranch.android.classsession.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.classsession.ClassSession
import java.util.*

@Dao
interface ClassSessionDao {
    @Query("SELECT * FROM classSession")
    fun getClassSessions(): LiveData<List<ClassSession>>

    @Query("SELECT * FROM classSession WHERE id=(:id)")
    fun getClassSession(id: UUID): LiveData<ClassSession?>

    @Query("DELETE FROM classSession")
    fun removeAllClassSessions()

    @Update
    fun updateClassSession(classSession: ClassSession)

    @Insert
    fun addClassSession(classSession: ClassSession)
}
