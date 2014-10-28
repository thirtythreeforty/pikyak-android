package net.thirtythreeforty.pikyak;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.networking.PikyakAPIService;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.APIErrorEvent;

public class PikyakApplication extends Application {
    private PikyakAPIService mPikyakAPIService;
    private Bus mBus = BusProvider.getBus();

    @Override
    public void onCreate() {
        super.onCreate();

        mPikyakAPIService = new PikyakAPIService();
        mBus.register(mPikyakAPIService);

        mBus.register(this); //listen for "global" events
    }

    @Subscribe
    public void onApiError(APIErrorEvent event) {
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        Log.e("PikyakApplication", "An API error occurred.", event.error);
    }
}
