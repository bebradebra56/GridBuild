package com.gridibuild.sfobud.data.local.dao

import androidx.room.*
import com.gridibuild.sfobud.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getFirstUser(): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE userId = :userId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getProjectsByUser(userId: Long): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE userId = :userId AND isActive = 1 AND isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveProjectsByUser(userId: Long): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE userId = :userId AND isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedProjectsByUser(userId: Long): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: Long): ProjectEntity?

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    fun getProjectByIdFlow(projectId: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE userId = :userId")
    suspend fun getProjectsByUserOnce(userId: Long): List<ProjectEntity>

    @Query("DELETE FROM projects WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: Long)
}

@Dao
interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity): Long

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Delete
    suspend fun deleteRoom(room: RoomEntity)

    @Query("SELECT * FROM rooms WHERE projectId = :projectId ORDER BY createdAt ASC")
    fun getRoomsByProject(projectId: Long): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :roomId LIMIT 1")
    suspend fun getRoomById(roomId: Long): RoomEntity?

    @Query("SELECT * FROM rooms WHERE id = :roomId LIMIT 1")
    fun getRoomByIdFlow(roomId: Long): Flow<RoomEntity?>

    @Query("SELECT * FROM rooms WHERE projectId = :projectId")
    suspend fun getRoomsByProjectOnce(projectId: Long): List<RoomEntity>

    @Query("DELETE FROM rooms WHERE projectId = :projectId")
    suspend fun deleteAllByProject(projectId: Long)
}

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY dueDate ASC, createdAt ASC")
    fun getTasksByProject(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE roomId = :roomId ORDER BY createdAt ASC")
    fun getTasksByRoom(roomId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND status != 'DONE' ORDER BY dueDate ASC")
    fun getPendingTasksByProject(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId")
    suspend fun getTaskCountByProject(projectId: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId AND status = 'DONE'")
    suspend fun getCompletedTaskCountByProject(projectId: Long): Int

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND date(dueDate/1000,'unixepoch') = date('now')")
    fun getTodayTasksByProject(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND dueDate < :now AND status != 'DONE'")
    fun getOverdueTasksByProject(projectId: Long, now: Long = System.currentTimeMillis()): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    suspend fun getTasksByProjectOnce(projectId: Long): List<TaskEntity>

    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    suspend fun deleteAllByProject(projectId: Long)
}

@Dao
interface MaterialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity): Long

    @Update
    suspend fun updateMaterial(material: MaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)

    @Query("SELECT * FROM materials WHERE projectId = :projectId ORDER BY category ASC, name ASC")
    fun getMaterialsByProject(projectId: Long): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE roomId = :roomId ORDER BY name ASC")
    fun getMaterialsByRoom(roomId: Long): Flow<List<MaterialEntity>>

    @Query("SELECT SUM(quantity * pricePerUnit) FROM materials WHERE projectId = :projectId")
    suspend fun getTotalMaterialsCost(projectId: Long): Double?

    @Query("DELETE FROM materials WHERE projectId = :projectId")
    suspend fun deleteAllByProject(projectId: Long)
}

@Dao
interface ShoppingItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItemEntity): Long

    @Update
    suspend fun updateShoppingItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItemEntity)

    @Query("SELECT * FROM shopping_items WHERE projectId = :projectId ORDER BY urgency ASC, createdAt ASC")
    fun getShoppingItemsByProject(projectId: Long): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE projectId = :projectId AND status = 'PENDING' ORDER BY urgency ASC")
    fun getPendingItemsByProject(projectId: Long): Flow<List<ShoppingItemEntity>>

    @Query("DELETE FROM shopping_items WHERE projectId = :projectId")
    suspend fun deleteAllByProject(projectId: Long)
}

@Dao
interface BudgetExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: BudgetExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: BudgetExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: BudgetExpenseEntity)

    @Query("SELECT * FROM budget_expenses WHERE projectId = :projectId ORDER BY date DESC")
    fun getExpensesByProject(projectId: Long): Flow<List<BudgetExpenseEntity>>

    @Query("SELECT SUM(amount) FROM budget_expenses WHERE projectId = :projectId AND isPlanned = 0")
    suspend fun getTotalSpent(projectId: Long): Double?

    @Query("SELECT SUM(amount) FROM budget_expenses WHERE projectId = :projectId AND isPlanned = 1")
    suspend fun getTotalPlanned(projectId: Long): Double?

    @Query("SELECT * FROM budget_expenses WHERE projectId = :projectId")
    suspend fun getExpensesByProjectOnce(projectId: Long): List<BudgetExpenseEntity>

    @Query("DELETE FROM budget_expenses WHERE projectId = :projectId")
    suspend fun deleteAllByProject(projectId: Long)
}

@Dao
interface MeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long

    @Update
    suspend fun updateMeasurement(measurement: MeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(measurement: MeasurementEntity)

    @Query("SELECT * FROM measurements WHERE roomId = :roomId ORDER BY type ASC, name ASC")
    fun getMeasurementsByRoom(roomId: Long): Flow<List<MeasurementEntity>>

    @Query("DELETE FROM measurements WHERE roomId = :roomId")
    suspend fun deleteAllByRoom(roomId: Long)
}

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE projectId = :projectId ORDER BY date DESC")
    fun getPhotosByProject(projectId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE roomId = :roomId ORDER BY date DESC")
    fun getPhotosByRoom(roomId: Long): Flow<List<PhotoEntity>>

    @Query("DELETE FROM photos WHERE projectId = :projectId")
    suspend fun deleteAllByProject(projectId: Long)

    @Query("DELETE FROM photos WHERE roomId = :roomId")
    suspend fun deleteAllByRoom(roomId: Long)
}

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("SELECT * FROM contacts WHERE projectId = :projectId ORDER BY role ASC, name ASC")
    fun getContactsByProject(projectId: Long): Flow<List<ContactEntity>>

    @Query("DELETE FROM contacts WHERE projectId = :projectId")
    suspend fun deleteAllByProject(projectId: Long)
}
