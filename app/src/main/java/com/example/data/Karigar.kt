package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "karigars")
data class Karigar(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = ""
) : Serializable
