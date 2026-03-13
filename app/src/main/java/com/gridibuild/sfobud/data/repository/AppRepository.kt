package com.gridibuild.sfobud.data.repository

import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class AppRepository(private val db: AppDatabase) {

    // ---- Auth / Users ----
    suspend fun getOrCreateDefaultUser(): Long {
        val existing = db.userDao().getFirstUser()
        if (existing != null) return existing.id
        return db.userDao().insertUser(UserEntity(name = "User", email = "default@gridbuild.app", passwordHash = ""))
    }

    suspend fun getUserById(userId: Long): UserEntity? = db.userDao().getUserById(userId)

    suspend fun updateUser(user: UserEntity) = db.userDao().updateUser(user)

    suspend fun deleteAllUserData(userId: Long) {
        db.projectDao().getProjectsByUserOnce(userId).forEach { project ->
            db.taskDao().deleteAllByProject(project.id)
            db.roomDao().getRoomsByProjectOnce(project.id).forEach { room ->
                db.measurementDao().deleteAllByRoom(room.id)
                db.photoDao().deleteAllByRoom(room.id)
            }
            db.roomDao().deleteAllByProject(project.id)
            db.materialDao().deleteAllByProject(project.id)
            db.shoppingItemDao().deleteAllByProject(project.id)
            db.budgetExpenseDao().deleteAllByProject(project.id)
            db.contactDao().deleteAllByProject(project.id)
            db.photoDao().deleteAllByProject(project.id)
        }
        db.projectDao().deleteAllByUser(userId)
        db.userDao().deleteUser(userId)
    }

    suspend fun exportAllDataAsJson(userId: Long): String {
        val projects = db.projectDao().getProjectsByUserOnce(userId)
        val sb = StringBuilder()
        sb.append("{\n  \"projects\": [\n")
        projects.forEachIndexed { pi, project ->
            sb.append("    {\n")
            sb.append("      \"name\": \"${project.name}\",\n")
            sb.append("      \"budget\": ${project.totalBudget},\n")
            sb.append("      \"description\": \"${project.description}\",\n")
            val rooms = db.roomDao().getRoomsByProjectOnce(project.id)
            sb.append("      \"rooms\": [\n")
            rooms.forEachIndexed { ri, room ->
                sb.append("        {\"name\": \"${room.name}\", \"type\": \"${room.type}\", \"stage\": \"${room.stage}\"}")
                if (ri < rooms.lastIndex) sb.append(",")
                sb.append("\n")
            }
            sb.append("      ],\n")
            val tasks = db.taskDao().getTasksByProjectOnce(project.id)
            sb.append("      \"tasks\": [\n")
            tasks.forEachIndexed { ti, task ->
                sb.append("        {\"title\": \"${task.title}\", \"status\": \"${task.status}\", \"priority\": \"${task.priority}\"}")
                if (ti < tasks.lastIndex) sb.append(",")
                sb.append("\n")
            }
            sb.append("      ],\n")
            val expenses = db.budgetExpenseDao().getExpensesByProjectOnce(project.id)
            sb.append("      \"expenses\": [\n")
            expenses.forEachIndexed { ei, exp ->
                sb.append("        {\"description\": \"${exp.description}\", \"amount\": ${exp.amount}, \"category\": \"${exp.category}\"}")
                if (ei < expenses.lastIndex) sb.append(",")
                sb.append("\n")
            }
            sb.append("      ]\n")
            sb.append("    }")
            if (pi < projects.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ]\n}")
        return sb.toString()
    }

    // ---- Projects ----
    fun getProjectsByUser(userId: Long): Flow<List<ProjectEntity>> = db.projectDao().getProjectsByUser(userId)
    fun getActiveProjectsByUser(userId: Long): Flow<List<ProjectEntity>> = db.projectDao().getActiveProjectsByUser(userId)
    fun getArchivedProjectsByUser(userId: Long): Flow<List<ProjectEntity>> = db.projectDao().getArchivedProjectsByUser(userId)
    suspend fun getProjectById(projectId: Long): ProjectEntity? = db.projectDao().getProjectById(projectId)
    fun getProjectByIdFlow(projectId: Long): Flow<ProjectEntity?> = db.projectDao().getProjectByIdFlow(projectId)
    suspend fun insertProject(project: ProjectEntity): Long = db.projectDao().insertProject(project)
    suspend fun updateProject(project: ProjectEntity) = db.projectDao().updateProject(project)
    suspend fun deleteProject(project: ProjectEntity) = db.projectDao().deleteProject(project)

    // ---- Rooms ----
    fun getRoomsByProject(projectId: Long): Flow<List<RoomEntity>> = db.roomDao().getRoomsByProject(projectId)
    suspend fun getRoomById(roomId: Long): RoomEntity? = db.roomDao().getRoomById(roomId)
    fun getRoomByIdFlow(roomId: Long): Flow<RoomEntity?> = db.roomDao().getRoomByIdFlow(roomId)
    suspend fun insertRoom(room: RoomEntity): Long = db.roomDao().insertRoom(room)
    suspend fun updateRoom(room: RoomEntity) = db.roomDao().updateRoom(room)
    suspend fun deleteRoom(room: RoomEntity) = db.roomDao().deleteRoom(room)

    // ---- Tasks ----
    fun getTasksByProject(projectId: Long): Flow<List<TaskEntity>> = db.taskDao().getTasksByProject(projectId)
    fun getTasksByRoom(roomId: Long): Flow<List<TaskEntity>> = db.taskDao().getTasksByRoom(roomId)
    fun getPendingTasksByProject(projectId: Long): Flow<List<TaskEntity>> = db.taskDao().getPendingTasksByProject(projectId)
    fun getTodayTasksByProject(projectId: Long): Flow<List<TaskEntity>> = db.taskDao().getTodayTasksByProject(projectId)
    fun getOverdueTasksByProject(projectId: Long): Flow<List<TaskEntity>> = db.taskDao().getOverdueTasksByProject(projectId)
    suspend fun getTaskCountByProject(projectId: Long): Int = db.taskDao().getTaskCountByProject(projectId)
    suspend fun getCompletedTaskCountByProject(projectId: Long): Int = db.taskDao().getCompletedTaskCountByProject(projectId)
    suspend fun insertTask(task: TaskEntity): Long = db.taskDao().insertTask(task)
    suspend fun updateTask(task: TaskEntity) = db.taskDao().updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = db.taskDao().deleteTask(task)

    // ---- Materials ----
    fun getMaterialsByProject(projectId: Long): Flow<List<MaterialEntity>> = db.materialDao().getMaterialsByProject(projectId)
    fun getMaterialsByRoom(roomId: Long): Flow<List<MaterialEntity>> = db.materialDao().getMaterialsByRoom(roomId)
    suspend fun getTotalMaterialsCost(projectId: Long): Double = db.materialDao().getTotalMaterialsCost(projectId) ?: 0.0
    suspend fun insertMaterial(material: MaterialEntity): Long = db.materialDao().insertMaterial(material)
    suspend fun updateMaterial(material: MaterialEntity) = db.materialDao().updateMaterial(material)
    suspend fun deleteMaterial(material: MaterialEntity) = db.materialDao().deleteMaterial(material)

    // ---- Shopping ----
    fun getShoppingItemsByProject(projectId: Long): Flow<List<ShoppingItemEntity>> = db.shoppingItemDao().getShoppingItemsByProject(projectId)
    fun getPendingShoppingItems(projectId: Long): Flow<List<ShoppingItemEntity>> = db.shoppingItemDao().getPendingItemsByProject(projectId)
    suspend fun insertShoppingItem(item: ShoppingItemEntity): Long = db.shoppingItemDao().insertShoppingItem(item)
    suspend fun updateShoppingItem(item: ShoppingItemEntity) = db.shoppingItemDao().updateShoppingItem(item)
    suspend fun deleteShoppingItem(item: ShoppingItemEntity) = db.shoppingItemDao().deleteShoppingItem(item)

    // ---- Budget ----
    fun getExpensesByProject(projectId: Long): Flow<List<BudgetExpenseEntity>> = db.budgetExpenseDao().getExpensesByProject(projectId)
    suspend fun getTotalSpent(projectId: Long): Double = db.budgetExpenseDao().getTotalSpent(projectId) ?: 0.0
    suspend fun getTotalPlanned(projectId: Long): Double = db.budgetExpenseDao().getTotalPlanned(projectId) ?: 0.0
    suspend fun insertExpense(expense: BudgetExpenseEntity): Long = db.budgetExpenseDao().insertExpense(expense)
    suspend fun updateExpense(expense: BudgetExpenseEntity) = db.budgetExpenseDao().updateExpense(expense)
    suspend fun deleteExpense(expense: BudgetExpenseEntity) = db.budgetExpenseDao().deleteExpense(expense)

    // ---- Measurements ----
    fun getMeasurementsByRoom(roomId: Long): Flow<List<MeasurementEntity>> = db.measurementDao().getMeasurementsByRoom(roomId)
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long = db.measurementDao().insertMeasurement(measurement)
    suspend fun updateMeasurement(measurement: MeasurementEntity) = db.measurementDao().updateMeasurement(measurement)
    suspend fun deleteMeasurement(measurement: MeasurementEntity) = db.measurementDao().deleteMeasurement(measurement)

    // ---- Photos ----
    fun getPhotosByProject(projectId: Long): Flow<List<PhotoEntity>> = db.photoDao().getPhotosByProject(projectId)
    fun getPhotosByRoom(roomId: Long): Flow<List<PhotoEntity>> = db.photoDao().getPhotosByRoom(roomId)
    suspend fun insertPhoto(photo: PhotoEntity): Long = db.photoDao().insertPhoto(photo)
    suspend fun updatePhoto(photo: PhotoEntity) = db.photoDao().updatePhoto(photo)
    suspend fun deletePhoto(photo: PhotoEntity) = db.photoDao().deletePhoto(photo)

    // ---- Contacts ----
    fun getContactsByProject(projectId: Long): Flow<List<ContactEntity>> = db.contactDao().getContactsByProject(projectId)
    suspend fun insertContact(contact: ContactEntity): Long = db.contactDao().insertContact(contact)
    suspend fun updateContact(contact: ContactEntity) = db.contactDao().updateContact(contact)
    suspend fun deleteContact(contact: ContactEntity) = db.contactDao().deleteContact(contact)
}
