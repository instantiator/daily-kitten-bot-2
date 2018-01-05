package instatiator.dailykittybot2.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.service.IBotService;
import instatiator.dailykittybot2.ui.AbstractBotActivity;

public abstract class AbstractBotFragment<Listener> extends Fragment {
    protected final String TAG;

    protected AbstractBotActivity activity;
    protected Listener listener;
    protected IBotService service;
    protected boolean initialised;

    protected final boolean uses_butterknife;
    protected final boolean uses_events;

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
        initialised = false;
    }

    protected abstract void extractArguments(Bundle args);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        if (uses_butterknife) {
            ButterKnife.bind(this, view);
        }
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
        reinit();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deinit();
    }

    private void init() {
        if (!initialised) {
            initialise();
            initialised = true;
        }
    }

    protected abstract void initialise();
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

    protected abstract void updateUI();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (Listener) context;
        activity = (AbstractBotActivity) context;
        service = activity.getService();
        reinit();
        updateUI();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        activity = null;
        service = null;
    }


}
