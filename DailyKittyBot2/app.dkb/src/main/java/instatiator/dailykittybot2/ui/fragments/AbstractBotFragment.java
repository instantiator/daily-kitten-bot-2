package instatiator.dailykittybot2.ui.fragments;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import instatiator.dailykittybot2.service.IBotService;
import instatiator.dailykittybot2.ui.AbstractBotActivity;

public abstract class AbstractBotFragment<VM extends ViewModel, Listener> extends Fragment {
    protected final String TAG;

    protected AbstractBotActivity bot_activity;
    protected Listener listener;
    protected IBotService service;
    protected boolean initialised;

    protected final boolean uses_butterknife;
    protected final boolean uses_events;

    protected boolean resumed;
    protected boolean attached;
    protected boolean view_ready;

    protected VM model;

    protected AbstractBotFragment(boolean uses_butterknife, boolean uses_events) {
        super();
        this.TAG = getClass().getName();
        this.uses_butterknife = uses_butterknife;
        this.uses_events = uses_events;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractArguments(getArguments());
        model = ViewModelProviders.of(getActivity()).get(getViewModelClass());
        initialised = false;
    }

    protected abstract Class<VM> getViewModelClass();

    protected abstract void extractArguments(Bundle args);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        if (uses_butterknife) {
            ButterKnife.bind(this, view);
        }
        view_ready = true;
        return view;
    }

    protected abstract int getLayout();

    @Override
    public void onStart() {
        super.onStart();
        if (uses_events) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (uses_events) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumed = true;
        reinit();
        safeUpdateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        resumed = false;
        deinit();
    }

    private void init() {
        if (!initialised && view_ready && attached && resumed) {
            initialised = initialise();
        }
    }

    protected abstract boolean initialise();
    protected abstract void denitialise();

    protected void reinit() {
        initialised = false;
        init();
    }

    private void deinit() {
        if (initialised) {
            denitialise();
            initialised = false;
        }
    }

    private void safeUpdateUI() {
        if (initialised && view_ready && attached && resumed) {
            updateUI();
        }
    }

    protected abstract void updateUI();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (Listener) context;
        bot_activity = (AbstractBotActivity) context;
        service = bot_activity.getService();
        attached = true;
        reinit();
        safeUpdateUI();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        attached = false;
        listener = null;
        bot_activity = null;
        service = null;
    }


}
