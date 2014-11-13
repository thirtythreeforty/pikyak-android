package net.thirtythreeforty.pikyak.ui.fragments;

import android.app.Activity;
import android.app.Fragment;

import net.thirtythreeforty.pikyak.BuildConfig;

abstract class BaseFragment extends Fragment {
    protected Object mCallbacks = getDefaultCallbacks();
    protected abstract Object getDefaultCallbacks();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(BuildConfig.DEBUG) {
            // Activities containing this fragment must implement its callbacks.
            // Unfortunately this check cannot be performed very cleanly with abstracts.
            if(!getDefaultCallbacks().getClass().getInterfaces()[0].isInstance(activity)) {
                throw new IllegalStateException("Activity must implement fragment's callbacks.");
            }
        }
        mCallbacks = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = getDefaultCallbacks();
    }
}
