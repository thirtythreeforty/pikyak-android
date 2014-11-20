package net.thirtythreeforty.pikyak.ui.adapters;

import android.content.Context;
import android.preference.PreferenceManager;

import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.auth.AccountAuthenticator;
import net.thirtythreeforty.pikyak.auth.AuthTokenGetterService.RunWithOptionalAuthorizationRequestEvent;
import net.thirtythreeforty.pikyak.auth.AuthTokenGetterService.RunnableWithAuthorization;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.APIErrorEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
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
        BusProvider.getBus().post(
                new RunWithOptionalAuthorizationRequestEvent(new RunnableWithAuthorization() {
                    @Override
                    public void onGotAuthorization(AuthorizationRetriever retriever) {
                        BusProvider.getBus().post(new GetConversationListRequestEvent(
                                retriever,
                                0,
                                PikyakAPIService.SORT_METHOD_HOT,
                                ""));
                    }
                },
                PreferenceManager
                    .getDefaultSharedPreferences(getContext())
                    .getString("pref_default_account", ""),
                AccountAuthenticator.ACCOUNT_TYPE)
        );

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
