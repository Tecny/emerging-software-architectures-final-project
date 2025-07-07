package database

import Beans.suscription.SuscriptionsEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY id ASC")
    suspend fun getAllSubscriptions(): List<SuscriptionsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptions(subscriptions: List<SuscriptionsEntity>)
}