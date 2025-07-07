package database

import Beans.tickets.TicketEntity
import Beans.sportspaces.SportSpaceEntity
import Beans.suscription.SuscriptionsEntity
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TicketEntity::class,
        SportSpaceEntity::class,
        SuscriptionsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
    abstract fun sportSpaceDao(): SportSpaceDao
    abstract fun subscriptionDao(): SubscriptionDao
}

