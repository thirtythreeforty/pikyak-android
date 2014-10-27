package net.thirtythreeforty.pikyak;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.networking.PikyakAPIService.GetConversationRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.GetConversationResultEvent;
import net.thirtythreeforty.pikyak.networking.model.ConversationModel;

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
        BusProvider.getBus().post(new GetConversationRequestEvent(mConversationID, 0));
    }

    @Subscribe
    public void onGetConversationResultEvent(GetConversationResultEvent resultEvent) {
        if(resultEvent.success) {
            replaceConversation(resultEvent.conversation);
        } else {
            Toast.makeText(
                    getContext(),
                    "Downloading the conversation failed! Why: " + resultEvent.error.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
            Log.e(TAG, "Download failed!", resultEvent.error.getCause());
        }
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
