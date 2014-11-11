package net.thirtythreeforty.pikyak;

import android.app.Activity;

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
