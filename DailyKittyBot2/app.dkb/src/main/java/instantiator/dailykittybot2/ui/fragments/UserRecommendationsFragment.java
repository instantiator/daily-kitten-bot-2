package instantiator.dailykittybot2.ui.fragments;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import butterknife.BindView;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.ui.adapters.LiveRecommendationsAdapter;
import instantiator.dailykittybot2.ui.viewmodels.UserOverviewViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class UserRecommendationsFragment extends AbstractBotFragment<UserOverviewViewModel, UserRecommendationsFragment.Listener> {

    private static final String KEY_username = "username";

    private String username;

    private LiveRecommendationsAdapter adapter;

    @BindView(R.id.recycler) public RecyclerView recycler;
    @BindView(R.id.card_no_recommendations) public CardView card_no_recommendations;

    public static UserRecommendationsFragment create(String username) {
        UserRecommendationsFragment fragment = new UserRecommendationsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_username, username);
        fragment.setArguments(bundle);
        return fragment;
    }

    public UserRecommendationsFragment() {
        super(true, false);
    }

    @Override
    protected Class<UserOverviewViewModel> getViewModelClass() {
        return UserOverviewViewModel.class;
    }

    @Override
    protected void extractArguments(Bundle arguments) {
        username = getArguments().getString(KEY_username);
    }

    @Override
    protected int getLayout() { return R.layout.fragment_user_recommendations; }

    @Override
    protected boolean initialise() {
        adapter = new LiveRecommendationsAdapter(bot_activity, model.getRecommendations(), recycler, recommendations_listener, card_no_recommendations);
        LinearLayoutManager layout = new LinearLayoutManager(bot_activity);
        recycler.setLayoutManager(layout);
        recycler.setAdapter(adapter);
        return true;
    }

    @Override
    protected void denitialise() {
        recycler.setAdapter(null);
    }

    @Override
    protected void updateUI() {
        boolean no_adapter = adapter == null;
        boolean no_items = no_adapter || adapter.getItemCount() == 0;
        card_no_recommendations.setVisibility(no_items ? VISIBLE : GONE);
    }

    private LiveRecommendationsAdapter.Listener recommendations_listener = new LiveRecommendationsAdapter.Listener() {

        @Override
        public void recommendation_selected(Recommendation recommendation) {
            listener.recommendation_selected(recommendation);
        }

        @Override
        public void request_recommendation_run(Recommendation recommendation) {
            listener.accept_recommendation(recommendation);
        }

        @Override
        public void request_recommendation_delete(Recommendation recommendation) {
            listener.reject_recommendation(recommendation);
        }
    };

    public interface Listener {
        void recommendation_selected(Recommendation recommendation);
        void accept_recommendation(Recommendation recommendation);
        void reject_recommendation(Recommendation recommendation);
    }
}
