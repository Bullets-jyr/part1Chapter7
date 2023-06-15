package kr.co.bullets.part1chapter7

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity(table)
@Entity(tableName = "word")
data class Word(
    val text: String,
    val mean: String,
    val type: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
