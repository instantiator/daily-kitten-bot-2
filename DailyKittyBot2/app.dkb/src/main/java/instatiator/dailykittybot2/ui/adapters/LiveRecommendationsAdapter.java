package instatiator.dailykittybot2.ui.adapters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Recommendation;

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
        holder.text_recommendation_name.setText(recommendation.type);
        holder.text_recommendation_summary.setText(recommendation.modifier);
    }

    @Override
    public int getItemCount() {
        return recommendations != null ? recommendations.size() : 0;
    }

    public class RecommendationHolder extends RecyclerView.ViewHolder {
        public Recommendation recommendation;

        @BindView(R.id.text_recommendation_name) public TextView text_recommendation_name;
        @BindView(R.id.text_recommendation_summary)public TextView text_recommendation_summary;

        public RecommendationHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.recommendation_selected(recommendation);
                }
            });
        }
    }

    public interface Listener {
        void recommendation_selected(Recommendation recommendation);
        void request_recommendation_run(Recommendation recommendation);
        void request_recommendation_delete(Recommendation recommendation);
    }
}