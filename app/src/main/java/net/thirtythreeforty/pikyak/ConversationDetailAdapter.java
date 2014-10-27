package net.thirtythreeforty.pikyak;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.thirtythreeforty.pikyak.networking.PikyakAPIFactory;
import net.thirtythreeforty.pikyak.networking.model.ConversationModel;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A subclass of {@link VotableImageAdapter} that handles {@link ConversationModel}s.
 * It must be given a conversationID upon construction.
 */
public class ConversationDetailAdapter extends VotableImageAdapter {
    static final String TAG = "ConversationDetailAdapter";

    final int mConversationID;

    public ConversationDetailAdapter(Context context, int conversationID) {
        super(context);
        mConversationID = conversationID;
    }

    @Override
    public void reload() {
        PikyakAPIFactory.getAPI().getConversation(
                mConversationID,
                0,
                new Callback<ConversationModel>() {
                    @Override
                    public void success(ConversationModel conversationList, Response response) {
                        replaceConversation(conversationList);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(
                                getContext(),
                                "Downloading the conversation failed! Why: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                        Log.e(TAG, "Download failed!", error.getCause());
                    }
                });
    }

    private void replaceConversation(ConversationModel conversation) {
        clear();
        addConversation(conversation);
    }

    private void addConversation(ConversationModel conversation) {
        addAll(conversation.posts);
        notifyDataSetChanged();
    }
}
