package net.thirtythreeforty.pikyak.networking;

import net.thirtythreeforty.pikyak.networking.model.*;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface PikyakServerAPI {
    @PUT("/users/{username}")
    RegistrationResponseModel register(@Path("username") String username, @Body RegistrationBodyModel body);

    @DELETE("/users/{username}")
    UnregistrationResponseModel unregister(@Path("username") String username);

    @GET("/conversations")
    ConversationListModel getConversationList(@Query("first") int first_conversation, @Query("sort") String sort_method, @Query("geo") String lat_and_long);

    @GET("/conversation/{conversation_id}")
    ConversationModel getConversation(@Path("conversation_id") int conversation_id, @Query("first") int first_post);

    @GET("/post/{post_id}")
    PostModel getPost(@Path("post_id") int post_id);
}
