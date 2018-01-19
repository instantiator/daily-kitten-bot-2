package instantiator.dailykittybot2.ui.adapters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.db.entities.Recommendation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class LiveRecommendationsAdapter extends RecyclerView.Adapter<LiveRecommendationsAdapter.RecommendationHolder> {

    private final AppCompatActivity activity;
    private List<Recommendation> recommendations;
    private RecyclerView recyclerView;
    private Listener listener;
    private CardView empty_card;

    public LiveRecommendationsAdapter(AppCompatActivity activity, LiveData<List<Recommendation>> live_recommendations, RecyclerView recyclerView, Listener listener, CardView empty_card) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.listener = listener;
        this.empty_card = empty_card;

        this.recommendations = live_recommendations.getValue();
        update_empty_card();

        live_recommendations.observe(activity, new Observer<List<Recommendation>>() {
            @Override
            public void onChanged(@Nullable List<Recommendation> recommendations) {
                LiveRecommendationsAdapter.this.recommendations = recommendations;
                notifyDataSetChanged();
                update_empty_card();
            }
        });
    }

    private void update_empty_card() {
        boolean empty = recommendations == null || recommendations.size() == 0;
        empty_card.setVisibility(empty ? VISIBLE : GONE);
    }

    @Override
    public RecommendationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, null);
        return new RecommendationHolder(view);
    }

    @Override
    public void onBindViewHolder(RecommendationHolder holder, int position) {
        Recommendation recommendation = recommendations.get(position);
        holder.recommendation = recommendation;
        holder.icon_current.setImageResource(recommendation.type.getIcon());
        holder.text_recommendation_name.setText(recommendation.type.getDescription());
        holder.text_recommendation_summary.setText(recommendation.modifier);
        holder.text_recommendation_summary.setVisibility(recommendation.type.requiresSpecifics() ? VISIBLE : GONE);
        holder.text_recommendation_location.setText(recommendation.targetSummary);
        holder.text_recommendation_subreddit.setText(
                activity.getString(R.string.text_recommendation_subreddit, recommendation.targetSubreddit));
        holder.text_recommendation_created.setReferenceTime(recommendation.created.getTime());
        holder.text_submission_posted.setReferenceTime(recommendation.targetSubmissionPosted.getTime());
    }

    @Override
    public int getItemCount() {
        return recommendations != null ? recommendations.size() : 0;
    }

    public class RecommendationHolder extends RecyclerView.ViewHolder {
        public Recommendation recommendation;

        @BindView(R.id.icon_current) public ImageView icon_current;
        @BindView(R.id.text_recommendation_name) public TextView text_recommendation_name;
        @BindView(R.id.text_recommendation_summary) public TextView text_recommendation_summary;
        @BindView(R.id.text_recommendation_location_summary) public TextView text_recommendation_location;
        @BindView(R.id.text_recommendation_created) public RelativeTimeTextView text_recommendation_created;
        @BindView(R.id.text_recommendation_subreddit) public TextView text_recommendation_subreddit;
        @BindView(R.id.text_recommendation_submission_posted) public RelativeTimeTextView text_submission_posted;
        @BindView(R.id.icon_menu) public ImageView icon_menu;
        @BindView(R.id.icon_accept) public ImageView icon_accept;
        @BindView(R.id.icon_reject) public ImageView icon_reject;

        PopupMenu popup;

        public RecommendationHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.recommendation_selected(recommendation);
                }
            });

            popup = new PopupMenu(icon_menu.getContext(), icon_menu);
            popup.getMenu().add(0, R.string.menu_recommendation_view, 0, R.string.menu_recommendation_view);
            popup.getMenu().add(0, R.string.menu_recommendation_visit_url, 0, R.string.menu_recommendation_visit_url);
            popup.getMenu().add(0, R.string.menu_recommendation_accept, 0, R.string.menu_recommendation_accept);
            popup.getMenu().add(0, R.string.menu_recommendation_reject, 0, R.string.menu_recommendation_reject);

            popup.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.string.menu_recommendation_view:
                        listener.recommendation_selected(recommendation);
                        return true;

                    case R.string.menu_recommendation_visit_url:
                        listener.request_view_post(recommendation);
                        return true;

                    case R.string.menu_recommendation_accept:
                        listener.request_recommendation_accept(recommendation);
                        return true;

                    case R.string.menu_recommendation_reject:
                        listener.request_recommendation_reject(recommendation);
                        return true;

                    default:
                        return false;
                }
            });

        }
    }

    public interface Listener {
        void request_view_post(Recommendation recommendation);
        void recommendation_selected(Recommendation recommendation);
        void request_recommendation_accept(Recommendation recommendation);
        void request_recommendation_reject(Recommendation recommendation);
    }
}