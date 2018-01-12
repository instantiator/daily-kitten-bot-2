package instatiator.dailykittybot2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;
import java.util.UUID;

import instatiator.dailykittybot2.db.entities.Result;
import instatiator.dailykittybot2.db.entities.Rule;

@Dao
public interface ResultDao {
    @Query("SELECT * FROM result ORDER BY ran DESC")
    LiveData<List<Result>> getAll();

    @Query("SELECT * FROM result WHERE uuid IN (:userIds) ORDER BY ran DESC")
    LiveData<List<Result>> loadAllByIds(UUID[] userIds);

    @Query("SELECT * FROM result WHERE uuid LIKE (:result) LIMIT 1")
    LiveData<Result> get(UUID result);

    @Query("SELECT * FROM result WHERE username LIKE :username ORDER BY ran DESC")
    LiveData<List<Result>> loadAllByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Result... results);

    @Update
    void updateAll(Result... results);

    @Delete
    void delete(Result result);
}