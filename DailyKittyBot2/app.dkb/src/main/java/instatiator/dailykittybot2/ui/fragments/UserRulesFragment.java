package instatiator.dailykittybot2.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.ui.AbstractBotActivity;

public class UserRulesFragment extends AbstractBotFragment<UserRulesFragment.Listener> {

    private Listener listener;
    private AbstractBotActivity activity;

    private static final String KEY_username = "username";
    private static final String KEY_mode = "mode";

    public enum Mode { View }

    private Mode mode;
    private String username;

    @BindView(R.id.recycler) public RecyclerView recycler;

    public static UserRulesFragment create(String username, Mode mode) {
        UserRulesFragment fragment = new UserRulesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_username, username);
        bundle.putString(KEY_mode, mode.name());
        fragment.setArguments(bundle);
        return fragment;
    }

    public UserRulesFragment() {
        super(false, false);
    }

    @Override
    protected void extractArguments(Bundle arguments) {
        mode = Mode.valueOf(getArguments().getString(KEY_mode));
        username = getArguments().getString(KEY_username);
    }

    @Override
    protected int getLayout() { return R.layout.fragment_user_rules; }

    @Override
    protected void initialise() {
        // TODO: recyclers
    }

    @Override
    protected void denitialise() {
        recycler.setAdapter(null);
    }

    @Override
    protected void updateUI() {
        // TODO
    }

    public interface Listener {

    }
}
