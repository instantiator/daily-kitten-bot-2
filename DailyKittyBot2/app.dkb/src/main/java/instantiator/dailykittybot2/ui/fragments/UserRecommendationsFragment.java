package instantiator.dailykittybot2.ui.fragments;

import android.app.AlertDialog;
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

    private LiveRecommendationsAdapter adapter;

    @BindView(R.id.recycler) public RecyclerView recycler;
    @BindView(R.id.card_no_recommendations) public CardView card_no_recommendations;

    public static UserRecommendationsFragment create() {
        return new UserRecommendationsFragment();
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
        // NOP
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
        public void request_view_post(Recommendation recommendation) {
            listener.request_view_post(recommendation);
        }

        @Override
        public void recommendation_selected(Recommendation recommendation) {
            listener.recommendation_selected(recommendation);
        }

        @Override
        public void request_recommendation_accept(Recommendation recommendation) {
            new AlertDialog.Builder(bot_activity)
                    .setTitle(R.string.dialog_title_confirm_accept_recommendation)
                    .setMessage(R.string.dialog_message_confirm_accept_recommendation)
                    .setPositiveButton(R.string.btn_accept, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        listener.accept_recommendation(recommendation);
                    })
                    .setNegativeButton(R.string.btn_close, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .create()
                    .show();
        }

        @Override
        public void request_recommendation_reject(Recommendation recommendation) {
            new AlertDialog.Builder(bot_activity)
                    .setTitle(R.string.dialog_title_confirm_reject_recommendation)
                    .setMessage(R.string.dialog_message_confirm_reject_recommendation)
                    .setPositiveButton(R.string.btn_reject, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        listener.reject_recommendation(recommendation);
                    })
                    .setNegativeButton(R.string.btn_close, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .create()
                    .show();
        }
    };

    public interface Listener {
        void request_view_post(Recommendation recommendation);
        void recommendation_selected(Recommendation recommendation);
        void accept_recommendation(Recommendation recommendation);
        void reject_recommendation(Recommendation recommendation);
    }
}
