package net.thirtythreeforty.pikyak;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.thirtythreeforty.pikyak.networking.PikyakAPIFactory;
import net.thirtythreeforty.pikyak.networking.PikyakServerAPI;
import net.thirtythreeforty.pikyak.networking.model.ConversationListModel;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A subclass of {@link VotableImageAdapter} that handles {@link ConversationListModel}s.
 */
public class ConversationListAdapter extends VotableImageAdapter {
    static final String TAG = "ConversationPreviewAdapter";

    public ConversationListAdapter(Context context) {
        super(context);
    }

    public void reload() {
        PikyakAPIFactory.getAPI().getConversationList(
                0,
                PikyakServerAPI.SORT_METHOD_HOT,
                "",
                new Callback<ConversationListModel>() {
                    @Override
                    public void success(ConversationListModel conversationList, Response response) {
                        replaceConversationList(conversationList);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(
                                getContext(),
                                "Downloading the conversation list failed! Why: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                        Log.e(TAG, "Download failed!", error.getCause());
                    }
                });
    }

    private void replaceConversationList(ConversationListModel conversationList) {
        clear();
        addConversationList(conversationList);
    }

    private void addConversationList(ConversationListModel conversationList) {
        addAll(conversationList.conversations);
        notifyDataSetChanged();
    }
}
