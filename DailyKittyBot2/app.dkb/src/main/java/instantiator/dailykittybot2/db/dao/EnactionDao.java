package instantiator.dailykittybot2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;
import java.util.UUID;

import instantiator.dailykittybot2.db.entities.Enaction;
import instantiator.dailykittybot2.db.entities.Outcome;

@Dao
public interface EnactionDao {

    @Query("SELECT * FROM enaction")
    LiveData<List<Enaction>> getAll();

    @Query("SELECT * FROM enaction WHERE recommendationUuid LIKE (:recommendationUuid) LIMIT 1")
    public LiveData<List<Enaction>> load_for_recommendation(UUID recommendationUuid);

    @Query("SELECT * FROM enaction WHERE username LIKE (:username) ORDER BY started DESC")
    LiveData<Enaction> load_all_for_user(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertAll(Enaction... outcomes);

    @Update
    public void updateAll(Enaction... outcomes);

    @Delete
    public void delete(Enaction outcome);

    @Query("DELETE FROM enaction WHERE username LIKE (:username)")
    public void delete_all_for(String username);
}
