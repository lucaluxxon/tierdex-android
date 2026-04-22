package com.example.tierdex

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalFindingDao {
    @Query("SELECT * FROM animal_findings ORDER BY id DESC")
    fun getAllFindings(): Flow<List<AnimalFindingEntity>>

    @Query("SELECT * FROM animal_findings WHERE ownerId = :ownerId ORDER BY id DESC")
    fun getAllFindingsByOwner(ownerId: String): Flow<List<AnimalFindingEntity>>

    @Query("SELECT * FROM animal_findings WHERE ownerId = :ownerId OR ownerId IS NULL ORDER BY id DESC")
    fun getAllFindingsVisibleForOwner(ownerId: String): Flow<List<AnimalFindingEntity>>

    @Query("SELECT * FROM animal_findings WHERE animalId = :animalId")
    fun getFindingsByAnimal(animalId: String): Flow<List<AnimalFindingEntity>>

    @Query("SELECT * FROM animal_findings WHERE animalId = :animalId AND ownerId = :ownerId")
    fun getFindingsByAnimalAndOwner(
        animalId: String,
        ownerId: String
    ): Flow<List<AnimalFindingEntity>>

    @Query("SELECT * FROM animal_findings")
    suspend fun getAllFindingsOnce(): List<AnimalFindingEntity>

    @Query("SELECT * FROM animal_findings WHERE ownerId = :ownerId")
    suspend fun getAllFindingsByOwnerOnce(ownerId: String): List<AnimalFindingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinding(finding: AnimalFindingEntity)

    @Update
    suspend fun updateFinding(finding: AnimalFindingEntity)

    @Delete
    suspend fun deleteFinding(finding: AnimalFindingEntity)
}
