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

    public static class APIErrorEvent {
        public RetrofitError error;

        protected APIErrorEvent(RetrofitError error) {
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
    public static class RegistrationResultEvent {
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
                    BusProvider.getBus().post(new RegistrationResultEvent());
                }

                @Override
                public void failure(RetrofitError error) {
                    mRegistrationRequestEventInProgress = false;
                    BusProvider.getBus().post(new APIErrorEvent(error));
                }
            });
        }
    }

    public static class UnregistrationRequestEvent {
    }
    public static class UnregistrationResultEvent {
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
    public static class GetConversationListResultEvent {
        public ConversationListModel conversationList;

        public GetConversationListResultEvent(ConversationListModel conversationList) {
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
                            BusProvider.getBus().post(logResult(new GetConversationListResultEvent(conversationList)));
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            mGetConversationListRequestInProgress = false;
                            BusProvider.getBus().post(logResult(new APIErrorEvent(error)));
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
    public static class GetConversationResultEvent {
        public ConversationModel conversation;

        public GetConversationResultEvent(ConversationModel conversation) {
            this.conversation = conversation;
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
                        BusProvider.getBus().post(logResult(new GetConversationResultEvent(conversation)));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BusProvider.getBus().post(logResult(new APIErrorEvent(error)));
                    }
                }
        );
    }

    private static void logRequest(Object o) {
        Log.d(TAG, "Received request of type " + o.getClass().getSimpleName());
    }
    private static Object logResult(Object o) {
        Log.d(TAG, "Sending result of type " + o.getClass().getSimpleName());
        return o;
    }
}
