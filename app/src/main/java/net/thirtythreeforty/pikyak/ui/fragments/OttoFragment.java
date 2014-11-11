package net.thirtythreeforty.pikyak.ui.fragments;

import net.thirtythreeforty.pikyak.BusProvider;

public abstract class OttoFragment extends BaseFragment {

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }
}
