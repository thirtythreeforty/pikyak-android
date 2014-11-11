package net.thirtythreeforty.pikyak.ui;

import android.app.Activity;

import net.thirtythreeforty.pikyak.BusProvider;

public abstract class OttoActivity extends Activity {
    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }
}
