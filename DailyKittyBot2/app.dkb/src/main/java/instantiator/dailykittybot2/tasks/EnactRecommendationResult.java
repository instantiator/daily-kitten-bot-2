package instantiator.dailykittybot2.tasks;

import java.util.List;

import instantiator.dailykittybot2.db.entities.Enaction;
import instantiator.dailykittybot2.db.entities.Recommendation;

public class EnactRecommendationResult {

    List<Enaction> enactions;
    List<Recommendation> successes;
    List<Recommendation> failures;

}
