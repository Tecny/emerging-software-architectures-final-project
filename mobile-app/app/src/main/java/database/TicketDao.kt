package database

import Beans.tickets.TicketEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY createdAt DESC")
    suspend fun getAllTickets(): List<TicketEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)
}