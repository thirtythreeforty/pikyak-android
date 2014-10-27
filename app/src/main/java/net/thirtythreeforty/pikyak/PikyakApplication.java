package net.thirtythreeforty.pikyak;

import android.app.Application;

import com.squareup.otto.Bus;

import net.thirtythreeforty.pikyak.networking.PikyakAPIService;

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

//    @Subscribe
//    public void onApiError(ApiErrorEvent event) {
//        toast("Something went wrong, please try again.");
//        Log.e("ReaderApp", event.getErrorMessage());
//    }
}
