package net.thirtythreeforty.pikyak;

import android.app.Application;

import net.thirtythreeforty.pikyak.networking.PikyakServerAPI;

import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;

public class PikyakApplication extends Application {
    // For now, this will use the emulator's host machine.  Change this when we deploy.
    private static final String PIKYAK_SERVER = "http://10.0.2.2:8888";

    private PikyakServerAPI pikyakService = null;

    public PikyakServerAPI getPikyakService() {
        if(pikyakService == null) {
            RestAdapter restAdapter = new Builder()
                    .setEndpoint(PIKYAK_SERVER)
                    .build();
            pikyakService = restAdapter.create(PikyakServerAPI.class);
        }

        return pikyakService;
    }
}
