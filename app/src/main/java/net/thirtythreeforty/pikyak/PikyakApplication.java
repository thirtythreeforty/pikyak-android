package net.thirtythreeforty.pikyak;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.auth.AuthTokenGetterService;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.APIErrorEvent;

public class PikyakApplication extends Application {
    private PikyakAPIService mPikyakAPIService;
    private AuthTokenGetterService mAuthTokenGetterService;

    @Override
    public void onCreate() {
        super.onCreate();

        mPikyakAPIService = new PikyakAPIService(this);
        mAuthTokenGetterService = new AuthTokenGetterService(this);

        Bus bus = BusProvider.getBus();
        bus.register(mPikyakAPIService);
        bus.register(mAuthTokenGetterService);
        bus.register(this); //listen for "global" events
    }

    @Subscribe
    public void onApiError(APIErrorEvent event) {
        Toast.makeText(this, getString(R.string.message_API_error), Toast.LENGTH_SHORT).show();
        Log.e("PikyakApplication", "An API error occurred.", event.error);
    }
}
