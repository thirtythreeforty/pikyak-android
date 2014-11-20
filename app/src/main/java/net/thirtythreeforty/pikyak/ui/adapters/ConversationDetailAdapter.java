package net.thirtythreeforty.pikyak.ui.adapters;

import android.content.Context;
import android.preference.PreferenceManager;

import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.auth.AccountAuthenticator;
import net.thirtythreeforty.pikyak.auth.AuthTokenGetterService.RunWithOptionalAuthorizationRequestEvent;
import net.thirtythreeforty.pikyak.auth.AuthTokenGetterService.RunnableWithAuthorization;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
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
        BusProvider.getBus().post(new RunWithOptionalAuthorizationRequestEvent(
                new RunnableWithAuthorization() {
                    @Override
                    public void onGotAuthorization(AuthorizationRetriever retriever) {
                        BusProvider.getBus().post(new GetConversationRequestEvent(
                                retriever, mConversationID, 0
                        ));
                    }
                },
                PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_default_account",""),
                AccountAuthenticator.ACCOUNT_TYPE
        ));
    }

    @Subscribe
    public void onGetConversationResultEvent(GetConversationResultEvent resultEvent) {
        replaceConversation(resultEvent.conversation);
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
