package com.example.tierdex

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tierdex.AnimalFindingDao
import com.example.tierdex.AnimalFindingEntity

@Database(
    entities = [AnimalFindingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AnimalFindingDatabase : RoomDatabase() {
    abstract fun animalFindingDao(): AnimalFindingDao
}