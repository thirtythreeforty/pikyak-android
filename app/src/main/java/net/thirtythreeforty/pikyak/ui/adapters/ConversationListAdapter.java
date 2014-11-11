package net.thirtythreeforty.pikyak.ui.adapters;

import android.content.Context;

import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.APIErrorEvent;
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
        if(resultEvent.conversationList != null) {
            replaceConversationList(resultEvent.conversationList);
        }
        if(mCallbacks != null) mCallbacks.onRefreshCompleted(true);
    }
    @Subscribe
    public void onApiErrorEvent(APIErrorEvent errorEvent) {
        if(errorEvent.requestEvent instanceof GetConversationListRequestEvent) {
            if(mCallbacks != null) mCallbacks.onRefreshCompleted(false);
        }
    }
    private void replaceConversationList(ConversationListModel conversationList) {
        clear();
        addConversationList(conversationList);
    }

    private void addConversationList(ConversationListModel conversationList) {
        if(conversationList.conversations != null) {
            addAll(conversationList.conversations);
            notifyDataSetChanged();
        }
    }
}
