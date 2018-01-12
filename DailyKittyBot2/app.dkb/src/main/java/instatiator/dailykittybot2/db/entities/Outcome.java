package instatiator.dailykittybot2.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;

import instatiator.dailykittybot2.db.data.OutcomeType;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "outcome",
        foreignKeys = @ForeignKey(entity = Rule.class,
                parentColumns = "uuid",
                childColumns = "ruleUuid",
                onDelete = CASCADE))
public class Outcome {

    @PrimaryKey
    @NonNull
    public UUID uuid;

    @ColumnInfo(name = "ruleUuid")
    public UUID ruleUuid;

    @ColumnInfo(name = "type")
    public OutcomeType type;

    @ColumnInfo(name = "modifier")
    public String modifier;

    @ColumnInfo(name = "ordering")
    public int ordering;
}
