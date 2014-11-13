package net.thirtythreeforty.pikyak.networking;

import android.util.Base64;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.networking.model.ConversationListModel;
import net.thirtythreeforty.pikyak.networking.model.ConversationModel;
import net.thirtythreeforty.pikyak.networking.model.CreateBlockResponseModel;
import net.thirtythreeforty.pikyak.networking.model.CreatePostResponseModel;
import net.thirtythreeforty.pikyak.networking.model.CreateVoteRequestBodyModel;
import net.thirtythreeforty.pikyak.networking.model.CreateVoteResponseModel;
import net.thirtythreeforty.pikyak.networking.model.DeleteBlockResponseModel;
import net.thirtythreeforty.pikyak.networking.model.DeleteVoteResponseModel;
import net.thirtythreeforty.pikyak.networking.model.RegistrationRequestBodyModel;
import net.thirtythreeforty.pikyak.networking.model.RegistrationResponseModel;
import net.thirtythreeforty.pikyak.networking.model.UnregistrationResponseModel;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public final class PikyakAPIService {
    private static final String TAG = "PikyakAPIService";

    public static final String SORT_METHOD_HOT = PikyakServerAPI.SORT_METHOD_HOT;

    private static final String PIKYAK_SERVER = "http://104.131.167.43:5000/";
    // Connection timeout in seconds
    private static final int CONNECT_TIMEOUT_SEC = 5;

    private static PikyakServerAPI pikyakServerAPI = null;
    private static PikyakServerAPI getAPI() {
        if(pikyakServerAPI == null) {
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setConnectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS);

            RestAdapter restAdapter = new Builder()
                    .setEndpoint(PIKYAK_SERVER)
                    .setClient(new OkClient(okHttpClient))
                    .build();
            pikyakServerAPI = restAdapter.create(PikyakServerAPI.class);
        }
        return pikyakServerAPI;
    }

    public static interface AuthorizationRetriever {
        public String getUsername();
        public String getAuthorization();
    }

    public static class APIErrorEvent {
        public RetrofitError error;
        public Object requestEvent = null;

        protected APIErrorEvent(RetrofitError error, Object requestEvent) {
            this.error = error;
            this.requestEvent = requestEvent;
        }
    }

    public static class RegistrationRequestEvent {
        public AuthorizationRetriever authorizationRetriever;

        public RegistrationRequestEvent(AuthorizationRetriever authorizationRetriever) {
            this.authorizationRetriever = authorizationRetriever;
        }
    }
    public static class RegistrationResultEvent {
    }
    private boolean mRegistrationRequestInProgress = false;
    @Subscribe
    public void onRegistrationRequest(final RegistrationRequestEvent requestEvent) {
        logRequest(requestEvent);
        if(!mRegistrationRequestInProgress) {
            mRegistrationRequestInProgress = true;
            RegistrationRequestBodyModel registrationBody = new RegistrationRequestBodyModel();
            registrationBody.email = requestEvent.authorizationRetriever.getUsername();
            // TODO GCM ID
            getAPI().register(
                    requestEvent.authorizationRetriever.getAuthorization(),
                    requestEvent.authorizationRetriever.getUsername(),
                    registrationBody,
                    new Callback<RegistrationResponseModel>() {
                        @Override
                        public void success(RegistrationResponseModel registrationResponse, Response response) {
                            mRegistrationRequestInProgress = false;
                            BusProvider.getBus().post(logResult(new RegistrationResultEvent()));
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            mRegistrationRequestInProgress = false;
                            BusProvider.getBus().post(logResult(new APIErrorEvent(error, requestEvent)));
                        }
                    }
            );
        }
    }

    public static class UnregistrationRequestEvent {
        public AuthorizationRetriever authorizationRetriever;

        public UnregistrationRequestEvent(AuthorizationRetriever authorizationRetriever) {
            this.authorizationRetriever = authorizationRetriever;
        }
    }
    public static class UnregistrationResultEvent {
    }
    private boolean mUnregistrationRequestInProgress = false;
    @Subscribe
    public void onUnregistrationRequest(final UnregistrationRequestEvent requestEvent) {
        logRequest(requestEvent);
        if(!mUnregistrationRequestInProgress) {
            mUnregistrationRequestInProgress = true;
            getAPI().unregister(
                    requestEvent.authorizationRetriever.getAuthorization(),
                    requestEvent.authorizationRetriever.getUsername(),
                    new Callback<UnregistrationResponseModel>() {
                        @Override
                        public void success(UnregistrationResponseModel unregistrationResponse, Response response) {
                            mUnregistrationRequestInProgress = false;
                            BusProvider.getBus().post(logResult(new UnregistrationResultEvent()));
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            mUnregistrationRequestInProgress = false;
                            BusProvider.getBus().post(logResult(new APIErrorEvent(error, requestEvent)));
                        }
                    }
            );
        }
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
                            BusProvider.getBus().post(logResult(new APIErrorEvent(error, requestEvent)));
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
                        BusProvider.getBus().post(logResult(new APIErrorEvent(error, requestEvent)));
                    }
                }
        );
    }

    public static class CreateConversationRequestEvent {
        public AuthorizationRetriever authorizationRetriever;
        public String filename;

        public CreateConversationRequestEvent(AuthorizationRetriever authorizationRetriever, String filename) {
            this.authorizationRetriever = authorizationRetriever;
            this.filename = filename;
        }
    }
    public static class CreateConversationResultEvent {
    }
    @Subscribe
    public void onCreateConversationRequest(final CreateConversationRequestEvent requestEvent) {
        logRequest(requestEvent);
        getAPI().createConversation(
                requestEvent.authorizationRetriever.getAuthorization(),
                "",
                getTypedFile(requestEvent.filename),
                new Callback<CreatePostResponseModel>() {
                    @Override
                    public void success(CreatePostResponseModel createPostResponse, Response response) {
                        BusProvider.getBus().post(logResult(new CreateConversationResultEvent()));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BusProvider.getBus().post(logResult(new APIErrorEvent(error, requestEvent)));
                    }
                }
        );
    }

    public static class CreatePostRequestEvent {
        public AuthorizationRetriever authorizationRetriever;
        public int conversation_id;
        public String filename;

        public CreatePostRequestEvent(AuthorizationRetriever authorizationRetriever, int conversation_id, String filename) {
            this.authorizationRetriever = authorizationRetriever;
            this.conversation_id = conversation_id;
            this.filename = filename;
        }
    }
    public static class CreatePostResultEvent {
    }
    @Subscribe
    public void onCreatePostRequest(final CreatePostRequestEvent requestEvent) {
        logRequest(requestEvent);
        getAPI().createPost(
                requestEvent.authorizationRetriever.getAuthorization(),
                requestEvent.conversation_id,
                "",
                getTypedFile(requestEvent.filename),
                new Callback<CreatePostResponseModel>() {
                    @Override
                    public void success(CreatePostResponseModel createPostResponse, Response response) {
                        BusProvider.getBus().post(logResult(new CreatePostResultEvent()));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BusProvider.getBus().post(logResult(new APIErrorEvent(error, requestEvent)));
                    }
                }
        );
    }

    public static class CreateVoteRequestEvent {
        public AuthorizationRetriever authorizationRetriever;
        public int conversation_id;
        public int value;

        public CreateVoteRequestEvent(AuthorizationRetriever authorizationRetriever, int conversation_id, int value) {
            this.authorizationRetriever = authorizationRetriever;
            this.conversation_id = conversation_id;
            this.value = value;
        }
    }
    public static class CreateVoteResultEvent {
    }
    @Subscribe
    public void onCreateVoteRequest(final CreateVoteRequestEvent requestEvent) {
        logRequest(requestEvent);
        CreateVoteRequestBodyModel body = new CreateVoteRequestBodyModel();
        body.value = requestEvent.value;
        getAPI().createVote(
                requestEvent.authorizationRetriever.getAuthorization(),
                requestEvent.conversation_id,
                body,
                new Callback<CreateVoteResponseModel>() {
                    @Override
                    public void success(CreateVoteResponseModel createVoteResponse, Response response) {
                        BusProvider.getBus().post(logResult(new CreateVoteResultEvent()));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BusProvider.getBus().post(logResult(new APIErrorEvent(error, requestEvent)));
                    }
                }
        );
    }

    public static class DeleteVoteRequestEvent {
        public AuthorizationRetriever authorizationRetriever;
        public int conversation_id;

        public DeleteVoteRequestEvent(AuthorizationRetriever authorizationRetriever, int conversation_id) {
            this.authorizationRetriever = authorizationRetriever;
            this.conversation_id = conversation_id;
        }
    }
    public static class DeleteVoteResultEvent {
    }
    @Subscribe
    public void onDeleteVoteRequest(final DeleteVoteRequestEvent requestEvent) {
        logRequest(requestEvent);
        getAPI().deleteVote(
                requestEvent.authorizationRetriever.getAuthorization(),
                requestEvent.conversation_id,
                new Callback<DeleteVoteResponseModel>() {
                    @Override
                    public void success(DeleteVoteResponseModel deleteVoteResponse, Response response) {
                        BusProvider.getBus().post(logResult(new DeleteVoteResultEvent()));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BusProvider.getBus().post(logResult(new APIErrorEvent(error, requestEvent)));
                    }
                }
        );
    }

    public static class CreateBlockRequestEvent {
        public AuthorizationRetriever authorizationRetriever;
        public int conversation_id;

        public CreateBlockRequestEvent(AuthorizationRetriever authorizationRetriever, int conversation_id) {
            this.authorizationRetriever = authorizationRetriever;
            this.conversation_id = conversation_id;
        }
    }
    public static class CreateBlockResultEvent {
    }
    @Subscribe
    public void onCreateBlockRequest(final CreateBlockRequestEvent requestEvent) {
        logRequest(requestEvent);
        getAPI().createBlock(
                requestEvent.authorizationRetriever.getAuthorization(),
                requestEvent.conversation_id,
                new Callback<CreateBlockResponseModel>() {
                    @Override
                    public void success(CreateBlockResponseModel createBlockResponse, Response response) {
                        BusProvider.getBus().post(logResult(new CreateBlockResultEvent()));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BusProvider.getBus().post(logResult(new APIErrorEvent(error, requestEvent)));
                    }
                }
        );
    }

    public static class DeleteBlockRequestEvent {
        public AuthorizationRetriever authorizationRetriever;
        public int conversation_id;

        public DeleteBlockRequestEvent(AuthorizationRetriever authorizationRetriever, int conversation_id) {
            this.authorizationRetriever = authorizationRetriever;
            this.conversation_id = conversation_id;
        }
    }
    public static class DeleteBlockResultEvent {
    }
    @Subscribe
    public void onDeleteBlockRequest(final DeleteBlockRequestEvent requestEvent) {
        logRequest(requestEvent);
        getAPI().deleteBlock(
                requestEvent.authorizationRetriever.getAuthorization(),
                requestEvent.conversation_id,
                new Callback<DeleteBlockResponseModel>() {
                    @Override
                    public void success(DeleteBlockResponseModel deleteBlockResponse, Response response) {
                        BusProvider.getBus().post(logResult(new DeleteBlockResultEvent()));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BusProvider.getBus().post(logResult(new APIErrorEvent(error, requestEvent)));
                    }
                }
        );
    }

    // Utility methods

    public static String computeAuthorization(String username, String password) {
        return "Basic " + Base64.encodeToString(
                (username + ":" + password).getBytes(Charset.forName("UTF-8")),
                Base64.NO_WRAP);
    }

    private static TypedFile getTypedFile(String s) {
        return new TypedFile("application/octet-stream", new File(s));
    }

    private static void logRequest(Object o) {
        Log.d(TAG, "Received request of type " + o.getClass().getSimpleName());
    }
    private static Object logResult(Object o) {
        Log.d(TAG, "Sending result of type " + o.getClass().getSimpleName());
        return o;
    }
}
