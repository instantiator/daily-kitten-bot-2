package instatiator.dailykittybot2.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import net.dean.jraw.models.OAuthData;
import net.dean.jraw.models.PersistedAuthData;
import net.dean.jraw.oauth.DeferredPersistentTokenStore;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.service.BotService;
import instatiator.dailykittybot2.service.IBotService;

import static android.view.View.inflate;

public class AuthDataAdapter extends RecyclerView.Adapter<AuthDataAdapter.TokenViewHolder> {
    private final Activity activity;
    private final IBotService service;
    private final DeferredPersistentTokenStore tokenStore;

    private List<String> usernames;
    private TreeMap<String, PersistedAuthData> data;

    private boolean enabled;
    private RecyclerView recyclerView;
    private Listener listener;

    public AuthDataAdapter(Activity activity, IBotService service, RecyclerView recyclerView, Listener listener) {
        this.activity = activity;
        this.service = service;
        this.recyclerView = recyclerView;
        this.tokenStore = service.get_token_store();
        this.listener = listener;
        this.enabled = false;
        update();
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            update();
        }
    }

    public void update() {
        this.data = new TreeMap<>(tokenStore.data());
        this.usernames = new ArrayList<>(this.data.keySet()); // sorted
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return data.size(); }

    @Override
    public TokenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tokenstore_entry, null);
        return new TokenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TokenViewHolder holder, int position) {
        String user = this.usernames.get(position);

        holder.card.setAlpha(enabled ? 1.0f : 0.3f);

        holder.user = user;
        holder.username.setText(user);

        PersistedAuthData auth = data.get(user);
        OAuthData auth_latest = auth.getLatest();

        boolean authorised =
                service.get_account_helper().isAuthenticated() &&
                user.equals(
                        service.get_account_helper()
                                .getReddit()
                                .getAuthManager()
                                .currentUsername());

        if (auth_latest != null) {
            long diffMillis = auth_latest.getExpiration().getTime() - new Date().getTime();
            long diffMinutes = TimeUnit.MINUTES.convert(diffMillis, TimeUnit.MILLISECONDS);
            holder.expiry.setText(activity.getString(R.string.text_access_token_status_short, diffMinutes));
        } else {
            holder.expiry.setText(R.string.text_access_token_expired);
        }

        holder.token_status.setText(
                auth.getRefreshToken() == null ?
                        R.string.text_no_refresh_token :
                        R.string.text_has_refresh_token);

        holder.icon.setImageResource(
            authorised ?
                R.drawable.ic_android_black_24dp :
                R.drawable.ic_hotel_black_24dp);
    }

    public class TokenViewHolder extends RecyclerView.ViewHolder {
        private String user;

        @BindView(R.id.card_entry) public CardView card;
        @BindView(R.id.text_username) public TextView username;
        @BindView(R.id.text_token_status) public TextView token_status;
        @BindView(R.id.text_expiry) public TextView expiry;
        @BindView(R.id.icon_current) public ImageView icon;

        public TokenViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (enabled) {
                        listener.user_selected(user);

                    } else {
                        informUser(R.string.toast_warning_busy_authenticating);
                    }
                }
            });
        }
    }

    private void informUser(int resource) {
        Toast.makeText(activity, resource, Toast.LENGTH_SHORT).show();
    }

    public interface Listener {
        void user_selected(String user);
    }
}