package instatiator.dailykittybot2.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

import instatiator.dailykittybot2.data.OutcomeType;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

@Entity(tableName = "recommendation",
        foreignKeys = {
            @ForeignKey(entity = Rule.class,
                parentColumns = "uuid",
                childColumns = "ruleUuid",
                onDelete = NO_ACTION),
            @ForeignKey(entity = Outcome.class,
                parentColumns = "uuid",
                childColumns = "outcomeUuid",
                onDelete = NO_ACTION) },
        indices = { @Index("ruleUuid"), @Index("outcomeUuid") })
public class Recommendation {

    @PrimaryKey
    @NonNull
    public UUID uuid;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "outcomeUuid")
    public UUID outcomeUuid;

    @ColumnInfo(name = "ruleUuid")
    public UUID ruleUuid;

    @ColumnInfo(name = "type")
    public OutcomeType type;

    @ColumnInfo(name = "modifier")
    public String modifier;

    @ColumnInfo(name = "created")
    public Date created;

    @ColumnInfo(name = "is_complete")
    public boolean complete;

    @ColumnInfo(name = "completed")
    public Date completed;

    @ColumnInfo(name = "accepted")
    public boolean accepted;
}
