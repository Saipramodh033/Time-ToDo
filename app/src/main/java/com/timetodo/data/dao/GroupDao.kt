package com.timetodo.data.dao

import androidx.room.*
import com.timetodo.data.entity.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<Group>>

    @Query("SELECT * FROM groups WHERE id = :id")
    fun getGroupById(id: Long): Flow<Group?>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupByIdSync(id: Long): Group?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group): Long

    @Update
    suspend fun updateGroup(group: Group)

    @Delete
    suspend fun deleteGroup(group: Group)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)
}
