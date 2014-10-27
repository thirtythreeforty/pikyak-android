package net.thirtythreeforty.pikyak.networking;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.networking.model.ConversationListModel;
import net.thirtythreeforty.pikyak.networking.model.ConversationModel;
import net.thirtythreeforty.pikyak.networking.model.RegistrationBodyModel;
import net.thirtythreeforty.pikyak.networking.model.RegistrationResponseModel;

import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public final class PikyakAPIService {
    private static final String TAG = "PikyakAPIService";

    public static final String SORT_METHOD_HOT = PikyakServerAPI.SORT_METHOD_HOT;

    public static PikyakServerAPI getAPI() {
        return pikyakServerAPI;
    }

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

    private static abstract class BaseResultEvent {
        public boolean success;
        public RetrofitError error;

        protected BaseResultEvent(boolean success, RetrofitError error) {
            this.success = success;
            this.error = error;
        }
    }

    public static class RegistrationRequestEvent {
        public String username, password;

        public RegistrationRequestEvent(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
    public static class RegistrationResultEvent extends BaseResultEvent {
        public RegistrationResultEvent(boolean success, RetrofitError error) {
            super(success, error);
        }
    }
    private boolean mRegistrationRequestEventInProgress = false;
    @Subscribe
    public void onRegistrationRequest(RegistrationRequestEvent requestEvent) {
        logRequest(requestEvent);
        if(!mRegistrationRequestEventInProgress) {
            mRegistrationRequestEventInProgress = true;
            RegistrationBodyModel registrationBody = new RegistrationBodyModel();
            registrationBody.email = requestEvent.username;
            // TODO GCM ID
            getAPI().register(requestEvent.username, registrationBody, new Callback<RegistrationResponseModel>() {
                @Override
                public void success(RegistrationResponseModel registrationResponseModel, Response response) {
                    mRegistrationRequestEventInProgress = false;
                    BusProvider.getBus().post(new RegistrationResultEvent(true, null));
                }

                @Override
                public void failure(RetrofitError error) {
                    mRegistrationRequestEventInProgress = false;
                    BusProvider.getBus().post(new RegistrationResultEvent(false, error));
                }
            });
        }
    }

    public static class UnregistrationRequestEvent {
    }
    public static class UnregistrationResultEvent extends BaseResultEvent {
        UnregistrationResultEvent(boolean success, RetrofitError error) {
            super(success, error);
        }
    }
    @Subscribe
    public void onUnregistrationRequest(UnregistrationRequestEvent requestEvent) {
        logRequest(requestEvent);
        // TODO
    }

    public static class GetConversationListRequestEvent {
        public int first_conversation;
        public String sort;
        public String geo;

        public GetConversationListRequestEvent(int first_conversation, String sort, String geo) {
            this.first_conversation = first_conversation;
            this.sort = sort;
            this.geo = geo;
        }
    }
    public static class GetConversationListResultEvent extends BaseResultEvent {
        public ConversationListModel conversationList;

        public GetConversationListResultEvent(boolean success, RetrofitError error, ConversationListModel conversationList) {
            super(success, error);
            this.conversationList = conversationList;
        }
    }
    private boolean mGetConversationListRequestInProgress = false;
    @Subscribe
    public void onGetConversationListRequest(final GetConversationListRequestEvent requestEvent) {
        logRequest(requestEvent);
        if(!mGetConversationListRequestInProgress) {
            mGetConversationListRequestInProgress = true;
            getAPI().getConversationList(
                    requestEvent.first_conversation,
                    requestEvent.sort,
                    requestEvent.geo,
                    new Callback<ConversationListModel>() {
                        @Override
                        public void success(ConversationListModel conversationList, Response response) {
                            mGetConversationListRequestInProgress = false;
                            BusProvider.getBus().post(logResult(new GetConversationListResultEvent(true, null, conversationList)));
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            mGetConversationListRequestInProgress = false;
                            BusProvider.getBus().post(logResult(new GetConversationListResultEvent(false, error, null)));
                        }
                    }
            );
        }
    }

    public static class GetConversationRequestEvent {
        public int first_conversation;
        public int conversation_id;

        public GetConversationRequestEvent(int conversation_id, int first_conversation) {
            this.first_conversation = first_conversation;
            this.conversation_id = conversation_id;
        }
    }
    public static class GetConversationResultEvent extends BaseResultEvent {
        public ConversationModel conversation;

        public GetConversationResultEvent(boolean success, RetrofitError error, ConversationModel conversation) {
            super(success, error);
            this.conversation = conversation;
            this.error = error;
        }
    }
    @Subscribe
    public void onGetConversationRequest(final GetConversationRequestEvent requestEvent) {
        logRequest(requestEvent);
        getAPI().getConversation(
                requestEvent.conversation_id,
                requestEvent.first_conversation,
                new Callback<ConversationModel>() {
                    @Override
                    public void success(ConversationModel conversation, Response response) {
                        BusProvider.getBus().post(logResult(new GetConversationResultEvent(true, null, conversation)));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BusProvider.getBus().post(logResult(new GetConversationResultEvent(false, error, null)));
                    }
                }
        );
    }

    private static void logRequest(Object o) {
        Log.d(TAG, "Received request of type " + o.getClass().getSimpleName());
    }
    private static BaseResultEvent logResult(BaseResultEvent result) {
        Log.d(TAG, "Sending event of type " + result.getClass().getSimpleName() + " (" + result.success + ")");
        return result;
    }
}
