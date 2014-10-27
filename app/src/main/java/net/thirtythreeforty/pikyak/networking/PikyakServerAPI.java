package net.thirtythreeforty.pikyak.networking;

import net.thirtythreeforty.pikyak.networking.model.ConversationListModel;
import net.thirtythreeforty.pikyak.networking.model.ConversationModel;
import net.thirtythreeforty.pikyak.networking.model.RegistrationBodyModel;
import net.thirtythreeforty.pikyak.networking.model.RegistrationResponseModel;
import net.thirtythreeforty.pikyak.networking.model.UnregistrationResponseModel;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface PikyakServerAPI {
    public static final String SORT_METHOD_HOT = "hot";

    @PUT("/users/{username}")
    public void register(
            @Path("username") String username,
            @Body RegistrationBodyModel body,
            Callback<RegistrationResponseModel> callback);

    @DELETE("/users/{username}")
    public void unregister(
            @Path("username") String username,
            Callback<UnregistrationResponseModel> callback);

    @GET("/conversations")
    public void getConversationList(
            @Query("first") int first_conversation,
            @Query("sort") String sort_method,
            @Query("geo") String lat_and_long,
            Callback<ConversationListModel> callback);

    @GET("/conversation/{conversation_id}")
    public void getConversation(
            @Path("conversation_id") int conversation_id,
            @Query("first") int first_post,
            Callback<ConversationModel> callback);
}
