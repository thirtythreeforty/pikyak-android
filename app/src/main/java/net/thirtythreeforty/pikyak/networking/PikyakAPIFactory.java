package net.thirtythreeforty.pikyak.networking;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.client.OkClient;

public final class PikyakAPIFactory {

    public static PikyakServerAPI getAPI() {
        return pikyakServerAPI;
    }

    private PikyakAPIFactory() {}

    // For now, this will use the emulator's host machine.  Change this when we deploy.
    private static final String PIKYAK_SERVER = "http://10.0.2.2:8888";
    // Connection timeout in seconds
    private static final int CONNECT_TIMEOUT_SEC = 5;

    private static final PikyakServerAPI pikyakServerAPI;
    static {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS);

        RestAdapter restAdapter = new Builder()
                .setEndpoint(PIKYAK_SERVER)
                .setClient(new OkClient(okHttpClient))
                .build();
        pikyakServerAPI = restAdapter.create(PikyakServerAPI.class);
    }

}
