package instatiator.dailykittybot2.ui.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Recommendation;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.ui.AbstractBotActivity;
import instatiator.dailykittybot2.ui.adapters.AuthDataAdapter;
import instatiator.dailykittybot2.ui.adapters.LiveRecommendationsAdapter;
import instatiator.dailykittybot2.ui.adapters.LiveRulesAdapter;
import instatiator.dailykittybot2.ui.viewmodels.UserOverviewViewModel;

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
            listener.request_recommendation_run(recommendation);
        }

        @Override
        public void request_recommendation_delete(Recommendation recommendation) {
            listener.request_recommendation_delete(recommendation);
        }
    };

    public interface Listener {
        void recommendation_selected(Recommendation recommendation);
        void request_recommendation_run(Recommendation recommendation);
        void request_recommendation_delete(Recommendation recommendation);
    }
}
