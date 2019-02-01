package instantiator.dailykittybot2.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import instantiator.dailykittybot2.data.RuleTriplet;

@Dao
public interface RuleTripletDao {

    @Transaction
    @Query("SELECT * FROM rule WHERE username LIKE :username ORDER BY rulename")
    LiveData<List<RuleTriplet>> loadAllByUsername(String username);

}
