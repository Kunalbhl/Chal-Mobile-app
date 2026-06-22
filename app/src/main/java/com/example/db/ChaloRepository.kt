package com.example.db

import kotlinx.coroutines.flow.Flow

class ChaloRepository(private val db: ChaloDatabase) {
    val userProfile: Flow<UserEntity?> = db.userDao.getUserProfile()
    val cartItems: Flow<List<CartItemEntity>> = db.cartDao.getCartItems()
    val allActivities: Flow<List<ActivityEntity>> = db.activityDao.getAllActivities()
    val walletTransactions: Flow<List<WalletTransactionEntity>> = db.walletDao.getTransactions()

    suspend fun getUserProfileSync(): UserEntity? {
        return db.userDao.getUserProfileSync()
    }

    suspend fun getCartItemsSync(): List<CartItemEntity> {
        return db.cartDao.getCartItemsSync()
    }

    suspend fun getAllActivitiesSync(): List<ActivityEntity> {
        return db.activityDao.getAllActivitiesSync()
    }

    suspend fun insertProfile(profile: UserEntity) {
        db.userDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: UserEntity) {
        db.userDao.updateProfile(profile)
    }

    suspend fun insertCartItem(item: CartItemEntity) {
        db.cartDao.insertCartItem(item)
    }

    suspend fun updateCartQuantity(id: Int, quantity: Int) {
        db.cartDao.updateCartQuantity(id, quantity)
    }

    suspend fun deleteCartItem(id: Int) {
        db.cartDao.deleteCartItem(id)
    }

    suspend fun clearCart() {
        db.cartDao.clearCart()
    }

    suspend fun insertActivity(activity: ActivityEntity) {
        db.activityDao.insertActivity(activity)
    }

    suspend fun updateActivityStatus(id: Int, status: String) {
        db.activityDao.updateActivityStatus(id, status)
    }

    suspend fun deleteActivityById(id: Int) {
        db.activityDao.deleteActivityById(id)
    }

    suspend fun insertTransaction(transaction: WalletTransactionEntity) {
        db.walletDao.insertTransaction(transaction)
    }
}
