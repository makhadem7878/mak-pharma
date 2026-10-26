package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        Medicine::class,
        MedicineBatch::class,
        Supplier::class,
        Customer::class,
        Sale::class,
        SaleItem::class,
        Purchase::class,
        PurchaseItem::class,
        StockMovement::class,
        CustomerDuePayment::class,
        Expense::class,
        CashierShift::class,
        AuditLog::class,
        PharmacySettings::class,
        AppUser::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pharmacyDao(): PharmacyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mak_pharma_bd.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
