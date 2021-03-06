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

import instantiator.dailykittybot2.db.entities.Recommendation;

@Dao
public interface RecommendationDao {
    @Query("SELECT * FROM recommendation ORDER BY created DESC")
    LiveData<List<Recommendation>> getAll();

    @Query("SELECT * FROM recommendation WHERE uuid IN (:ids) ORDER BY created DESC")
    LiveData<List<Recommendation>> loadAllByIds(UUID[] ids);

    @Query("SELECT * FROM recommendation WHERE uuid LIKE (:recommendation) LIMIT 1")
    LiveData<Recommendation> get(UUID recommendation);

    @Query("SELECT * FROM recommendation WHERE username LIKE :username ORDER BY created DESC")
    LiveData<List<Recommendation>> loadAllByUsername(String username);

    @Query("SELECT * FROM recommendation WHERE username LIKE :username AND NOT succeeded and NOT user_rejected ORDER BY created DESC")
    LiveData<List<Recommendation>> loadUnenactedByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Recommendation... recommendations);

    @Update
    void updateAll(Recommendation... recommendations);

    @Delete
    void delete(Recommendation recommendation);

    @Query("DELETE FROM recommendation WHERE username LIKE :username")
    void delete_all_for(String username);
}