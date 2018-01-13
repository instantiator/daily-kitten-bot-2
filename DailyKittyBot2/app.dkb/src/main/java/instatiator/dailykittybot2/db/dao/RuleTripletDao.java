package instatiator.dailykittybot2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.entities.Rule;

@Dao
public interface RuleTripletDao {

    @Transaction
    @Query("SELECT * FROM rule WHERE username LIKE :username ORDER BY rulename")
    LiveData<List<RuleTriplet>> loadAllByUsername(String username);

}
