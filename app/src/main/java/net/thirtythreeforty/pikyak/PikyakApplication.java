package net.thirtythreeforty.pikyak;

import android.app.Application;

import com.squareup.okhttp.OkHttpClient;

import net.thirtythreeforty.pikyak.networking.PikyakServerAPI;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.client.OkClient;

public class PikyakApplication extends Application {
    // For now, this will use the emulator's host machine.  Change this when we deploy.
    private static final String PIKYAK_SERVER = "http://10.0.2.2:8888";

    private PikyakServerAPI pikyakService = null;

    public PikyakServerAPI getPikyakService() {
        if(pikyakService == null) {
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setConnectTimeout(5, TimeUnit.SECONDS);

            RestAdapter restAdapter = new Builder()
                    .setEndpoint(PIKYAK_SERVER)
                    .setClient(new OkClient(okHttpClient))
                    .build();
            pikyakService = restAdapter.create(PikyakServerAPI.class);
        }

        return pikyakService;
    }
}
