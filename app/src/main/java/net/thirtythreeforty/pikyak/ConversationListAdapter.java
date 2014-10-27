package net.thirtythreeforty.pikyak;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.networking.PikyakAPIService;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.GetConversationListRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.GetConversationListResultEvent;
import net.thirtythreeforty.pikyak.networking.model.ConversationListModel;

/**
 * A subclass of {@link VotableImageAdapter} that handles {@link ConversationListModel}s.
 */
public class ConversationListAdapter extends VotableImageAdapter {
    static final String TAG = "ConversationPreviewAdapter";

    public ConversationListAdapter(Context context) {
        super(context);
    }

    public void reload() {
        BusProvider.getBus().post(new GetConversationListRequestEvent(
                0,
                PikyakAPIService.SORT_METHOD_HOT,
                ""));
    }

    @Subscribe
    public void onConversationListResultEvent(GetConversationListResultEvent resultEvent) {
        if(resultEvent.success) {
            replaceConversationList(resultEvent.conversationList);
        } else {
            Toast.makeText(
                    getContext(),
                    "Downloading the conversation list failed! Why: " + resultEvent.error.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
            Log.e(TAG, "Download failed!", resultEvent.error.getCause());
        }
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
