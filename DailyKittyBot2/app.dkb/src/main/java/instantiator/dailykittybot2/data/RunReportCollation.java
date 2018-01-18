package instantiator.dailykittybot2.data;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.db.entities.RunReport;

public class RunReportCollation {

    @Embedded
    public RunReport report;

    @Relation(parentColumn = "uuid", entityColumn = "run_report_uuid")
    public List<Recommendation> recommendations;

}
