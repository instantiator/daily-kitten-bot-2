package instantiator.dailykittybot2.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import instantiator.dailykittybot2.data.OutcomeType;
import instantiator.dailykittybot2.data.TargetType;

import static android.arch.persistence.room.ForeignKey.NO_ACTION;

@Entity(tableName = "recommendation",
        indices = { @Index("ruleUuid"), @Index("outcomeUuid") })
public class Recommendation {

    @PrimaryKey
    @NonNull
    public UUID uuid;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "outcomeUuid")
    public UUID outcomeUuid_unsafe;

    @ColumnInfo(name = "ruleUuid")
    public UUID ruleUuid_unsafe;

    @ColumnInfo(name = "ruleName")
    public String ruleName;

    @ColumnInfo(name = "type")
    public OutcomeType type;

    @ColumnInfo(name = "modifier")
    public String modifier;

    @ColumnInfo(name = "targetType")
    public TargetType targetType;

    @ColumnInfo(name = "targetCommentId")
    public String targetCommentId;

    @ColumnInfo(name = "targetSubmissionId")
    public String targetSubmissionId;

    @ColumnInfo(name = "targetSubmissionPosted")
    public Date targetSubmissionPosted;

    @ColumnInfo(name = "targetCommentPosted")
    public Date targetCommentPosted;

    @ColumnInfo(name = "targetSubreddit")
    public String targetSubreddit;

    @ColumnInfo(name = "targetSummary")
    public String targetSummary;

    @ColumnInfo(name = "targetPostUri")
    public Uri targetPostUri;

    @ColumnInfo(name = "targetCommentUri")
    public Uri targetCommentUri;

    @ColumnInfo(name = "created")
    public Date created;

    @ColumnInfo(name = "user_accepted")
    public boolean userAccepted;

    @ColumnInfo(name = "user_rejected")
    public boolean userRejected;

    @ColumnInfo(name = "last_attempted")
    public Date lastAttempted;

    @ColumnInfo(name = "succeeded")
    public boolean succeeded;

    @ColumnInfo(name = "failed")
    public boolean failed;

    @ColumnInfo(name = "fail_messages")
    public List<String> failMessages;

    @ColumnInfo(name = "run_report_uuid")
    public UUID runReportUuid_unsafe;
}
