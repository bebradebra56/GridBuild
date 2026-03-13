package com.gridibuild.sfobud.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "projects",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val description: String = "",
    val address: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val totalBudget: Double = 0.0,
    val isActive: Boolean = true,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "rooms",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val type: String = "",
    val width: Double = 0.0,
    val height: Double = 0.0,
    val length: Double = 0.0,
    val ceilingHeight: Double = 0.0,
    val stage: String = "Planning",
    val colorHex: String = "#FFC83A",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(entity = ProjectEntity::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RoomEntity::class, parentColumns = ["id"], childColumns = ["roomId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("projectId"), Index("roomId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long? = null,
    val title: String,
    val description: String = "",
    val priority: String = "MEDIUM",
    val status: String = "TODO",
    val dueDate: Long? = null,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "materials",
    foreignKeys = [
        ForeignKey(entity = ProjectEntity::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RoomEntity::class, parentColumns = ["id"], childColumns = ["roomId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("projectId"), Index("roomId")]
)
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long? = null,
    val name: String,
    val category: String = "Other",
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val pricePerUnit: Double = 0.0,
    val purchased: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shopping_items",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val category: String = "",
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val estimatedPrice: Double = 0.0,
    val actualPrice: Double? = null,
    val urgency: String = "THIS_WEEK",
    val status: String = "PENDING",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "budget_expenses",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class BudgetExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val category: String = "Other",
    val description: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val isPlanned: Boolean = false,
    val isUnexpected: Boolean = false
)

@Entity(
    tableName = "measurements",
    foreignKeys = [ForeignKey(
        entity = RoomEntity::class,
        parentColumns = ["id"],
        childColumns = ["roomId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("roomId")]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val type: String,
    val name: String = "",
    val value: Double,
    val unit: String = "m",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(entity = ProjectEntity::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RoomEntity::class, parentColumns = ["id"], childColumns = ["roomId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("projectId"), Index("roomId")]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long? = null,
    val stage: String = "DURING",
    val imagePath: String,
    val caption: String = "",
    val date: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "contacts",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val role: String = "",
    val phone: String = "",
    val email: String = "",
    val company: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
