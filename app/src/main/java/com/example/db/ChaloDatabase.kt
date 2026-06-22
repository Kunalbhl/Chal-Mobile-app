package com.example.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// --- Entities ---

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val phone: String,
    val dob: String,
    val gender: String,
    val savedAddressesJson: String,
    val currentAddress: String,
    val walletPoints: Int,
    val referralCode: String,
    val referralPointsEarned: Int,
    val connectedAccountsJson: String,
    val preferencesJson: String
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String,
    val platform: String,
    val category: String, // "Food", "Mart", "Stays"
    val price: Double,
    val deliveryFee: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String = ""
)

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: String,
    val platform: String,
    val category: String, // "Rides", "Intercity", "Food", "Mart", "Stays"
    val date: String,
    val time: String,
    val amount: Double,
    val status: String, // "Ongoing", "Completed", "Cancelled", "Upcoming"
    val details: String,
    val paymentMethod: String = "Chalo Wallet"
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val points: Int,
    val isCredit: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserEntity)

    @Update
    suspend fun updateProfile(profile: UserEntity)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items")
    suspend fun getCartItemsSync(): List<CartItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateCartQuantity(id: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY id DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities ORDER BY id DESC")
    suspend fun getAllActivitiesSync(): List<ActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)

    @Query("UPDATE activities SET status = :status WHERE id = :id")
    suspend fun updateActivityStatus(id: Int, status: String)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteActivityById(id: Int)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)
}

// --- Database instance ---

@Database(
    entities = [
        UserEntity::class,
        CartItemEntity::class,
        ActivityEntity::class,
        WalletTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChaloDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val cartDao: CartDao
    abstract val activityDao: ActivityDao
    abstract val walletDao: WalletDao

    companion object {
        @Volatile
        private var INSTANCE: ChaloDatabase? = null

        fun getInstance(context: Context): ChaloDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChaloDatabase::class.java,
                    "chalo_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Launch pre-population on a background thread
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            prepopulateData(database)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateData(db: ChaloDatabase) {
            // Build default user profile
            val defaultUser = UserEntity(
                name = "Kunal Pareek",
                email = "kunalpareekusa@gmail.com",
                phone = "+91 98765 43210",
                dob = "1995-05-15",
                gender = "Male",
                savedAddressesJson = "Home: H-25, Connaught Place, New Delhi; Work: DLF Cyber City, Gurugram; Hotel: Trident, Nariman Point, Mumbai",
                currentAddress = "H-25, Connaught Place, New Delhi",
                walletPoints = 3500, // Rs. 175
                referralCode = "CHALO350",
                referralPointsEarned = 2000,
                connectedAccountsJson = "{\"Uber\":true,\"Ola\":true,\"Rapido\":false,\"Swiggy\":true,\"Zomato\":true,\"Blinkit\":true,\"Zepto\":true,\"Booking\":true,\"Agoda\":false}",
                preferencesJson = "{\"Food\":[\"Zomato\",\"Swiggy\",\"EatSure\",\"Zepto Cafe\"],\"Mart\":[\"Blinkit\",\"Zepto\",\"Instamart\",\"JioMart\"],\"Rides\":[\"Uber\",\"Ola\",\"Rapido\",\"Namma Yatri\"],\"Stays\":[\"Agoda\",\"Booking.com\",\"MakeMyTrip\",\"Cleartrip\"],\"Mode\":\"AI Recommended\"}"
            )
            db.userDao.insertProfile(defaultUser)

            // Populate initial activities for a rich user experience
            val initialActivities = listOf(
                ActivityEntity(
                    orderId = "CH-928172",
                    platform = "Swiggy",
                    category = "Food",
                    date = "Today",
                    time = "10:30 AM",
                    amount = 320.0,
                    status = "Completed",
                    details = "Paneer Butter Masala x1, Tandoori Roti x2"
                ),
                ActivityEntity(
                    orderId = "CH-841029",
                    platform = "Uber",
                    category = "Rides",
                    date = "Today",
                    time = "12:45 PM",
                    amount = 210.0,
                    status = "Completed",
                    details = "Connaught Place to DLF Cyber City"
                ),
                ActivityEntity(
                    orderId = "CH-372910",
                    platform = "Blinkit",
                    category = "Mart",
                    date = "Today",
                    time = "03:15 PM",
                    amount = 450.0,
                    status = "Completed",
                    details = "Amul Organic Milk x2, Harvest Gold Bread x1, Fresh Bananas x1"
                ),
                ActivityEntity(
                    orderId = "CH-491023",
                    platform = "Agoda",
                    category = "Stays",
                    date = "Yesterday",
                    time = "06:00 PM",
                    amount = 4200.0,
                    status = "Completed",
                    details = "Goa Beachfront Resort (1 Night Stay)"
                )
            )
            for (act in initialActivities) {
                db.activityDao.insertActivity(act)
            }

            // Populate initial wallet ledger
            val initialLedger = listOf(
                WalletTransactionEntity(description = "Welcome bonus points", points = 1000, isCredit = true),
                WalletTransactionEntity(description = "Completed trip cash-back", points = 200, isCredit = true),
                WalletTransactionEntity(description = "Referred Kunal's Friend", points = 2000, isCredit = true),
                WalletTransactionEntity(description = "Redeemed on Swiggy Cafe", points = 300, isCredit = false)
            )
            for (tx in initialLedger) {
                db.walletDao.insertTransaction(tx)
            }
        }
    }
}
