package instatiator.dailykittybot2.ui.fragments;

import android.app.AlertDialog;
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
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.ui.AbstractBotActivity;
import instatiator.dailykittybot2.ui.adapters.AuthDataAdapter;
import instatiator.dailykittybot2.ui.adapters.LiveRulesAdapter;
import instatiator.dailykittybot2.ui.viewmodels.UserOverviewViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class UserRulesFragment extends AbstractBotFragment<UserOverviewViewModel, UserRulesFragment.Listener> {

    private static final String KEY_username = "username";

    private String username;

    private LiveRulesAdapter adapter;

    @BindView(R.id.recycler) public RecyclerView recycler;
    @BindView(R.id.card_no_rules) public CardView card_no_rules;
    @BindView(R.id.fab_add_rule) public FloatingActionButton fab_add_rule;

    public static UserRulesFragment create(String username) {
        UserRulesFragment fragment = new UserRulesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_username, username);
        fragment.setArguments(bundle);
        return fragment;
    }

    public UserRulesFragment() {
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
    protected int getLayout() { return R.layout.fragment_user_rules; }

    @Override
    protected boolean initialise() {
        adapter = new LiveRulesAdapter(bot_activity, model.getTriplets(), recycler, rules_listener, card_no_rules);
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
        card_no_rules.setVisibility(no_items ? VISIBLE : GONE);
    }

    @OnClick(R.id.fab_add_rule)
    public void add_rule_click() {
        listener.request_create_rule();
    }

    private LiveRulesAdapter.Listener rules_listener = new LiveRulesAdapter.Listener() {
        @Override
        public void rule_selected(Rule rule) {
            listener.rule_selected(rule);
        }

        @Override
        public void request_delete(Rule rule) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dialog_title_confirm_delete_rule)
                    .setMessage(R.string.dialog_message_confirm_delete_rule)
                    .setPositiveButton(R.string.btn_delete, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        listener.request_delete(rule);
                    })
                    .setNegativeButton(R.string.btn_cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .create()
                    .show();
        }

        @Override
        public void request_run(Rule rule) {
            listener.request_run(rule);
        }
    };

    public interface Listener {
        void rule_selected(Rule rule);
        void request_create_rule();
        void request_delete(Rule rule);
        void request_run(Rule rule);
    }
}
