package database

import Beans.sportspaces.SportSpaceEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SportSpaceDao {
    @Query("SELECT * FROM sportspaces ORDER BY name ASC")
    suspend fun getAllSportSpaces(): List<SportSpaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSportSpaces(sportSpaces: List<SportSpaceEntity>)
}