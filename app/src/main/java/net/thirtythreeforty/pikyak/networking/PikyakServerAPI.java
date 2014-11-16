package net.thirtythreeforty.pikyak.networking;

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
import net.thirtythreeforty.pikyak.networking.model.UserResponseModel;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

interface PikyakServerAPI {
    public static final String SORT_METHOD_HOT = "hot";

    @GET("/users/{username}")
    public void getUser(
            @Header("Authorization") String auth,
            @Path("username") String username,
            Callback<UserResponseModel> callback);

    @PUT("/users/{username}")
    public void register(
            @Header("Authorization") String auth,
            @Path("username") String username,
            @Body RegistrationRequestBodyModel body,
            Callback<RegistrationResponseModel> callback);

    @DELETE("/users/{username}")
    public void unregister(
            @Header("Authorization") String auth,
            @Path("username") String username,
            Callback<UnregistrationResponseModel> callback);

    @GET("/conversations")
    public void getConversationList(
            @Query("first") int first_conversation,
            @Query("sort") String sort_method,
            @Query("geo") String lat_and_long,
            Callback<ConversationListModel> callback);

    @GET("/conversations/{conversation_id}")
    public void getConversation(
            @Path("conversation_id") int conversation_id,
            @Query("first") int first_post,
            Callback<ConversationModel> callback);

    @Multipart
    @POST("/conversations")
    public void createConversation(
            @Header("Authorization") String auth,
            @Part("geo") String geo,
            @Part("image") TypedFile image,
            Callback<CreatePostResponseModel> callback);

    @Multipart
    @POST("/conversations/{conversation_id}")
    public void createPost(
            @Header("Authorization") String auth,
            @Path("conversation_id") int conversation_id,
            @Part("geo") String geo,
            @Part("image") TypedFile image,
            Callback<CreatePostResponseModel> callback);

    @PUT("/posts/{post_id}/user_score")
    public void createVote(
            @Header("Authorization") String auth,
            @Path("post_id") int post_id,
            @Body CreateVoteRequestBodyModel body,
            Callback<CreateVoteResponseModel> callback
    );

    @DELETE("/posts/{post_id}/user_score")
    public void deleteVote(
            @Header("Authorization") String auth,
            @Path("post_id") int post_id,
            Callback<DeleteVoteResponseModel> callback
    );

    @PUT("/posts/{post_id}/block")
    public void createBlock(
            @Header("Authorization") String auth,
            @Path("post_id") int post_id,
            Callback<CreateBlockResponseModel> callback
    );

    @DELETE("/posts/{post_id}/block")
    public void deleteBlock(
            @Header("Authorization") String auth,
            @Path("post_id") int post_id,
            Callback<DeleteBlockResponseModel> callback
    );
}
