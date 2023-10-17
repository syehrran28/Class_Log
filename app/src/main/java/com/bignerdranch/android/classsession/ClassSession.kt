package com.bignerdranch.android.classsession

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class ClassSession(
    @PrimaryKey var id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var time: Date = Date(),
    var isCompleted: Boolean = false,
    var student: String = "",
    var description: String = ""
)
