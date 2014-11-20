package net.thirtythreeforty.pikyak.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.R;
import net.thirtythreeforty.pikyak.auth.AuthTokenGetterService.RunnableWithAuthorization;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.CreatePostVoteRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.DeletePostVoteRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.FlagPostRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.UnflagPostRequestEvent;
import net.thirtythreeforty.pikyak.ui.adapters.ConversationDetailAdapter;

/**
 * A list fragment representing a list of Conversations. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ConversationDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ConversationDetailFragment extends VotableImageListFragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_CONVERSATION_ID = "conversation_id";

    public interface Callbacks {
    }
    private static Callbacks sDummyCallbacks = new Callbacks() {};
    @Override
    protected Callbacks getDefaultCallbacks() {
        return sDummyCallbacks;
    }

    private ListView mListView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConversationDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView = (ListView)view.findViewById(R.id.listView);
        ConversationDetailAdapter listAdapter = new ConversationDetailAdapter(
                getActivity(),
                getArguments().getInt(ARG_CONVERSATION_ID));
        listAdapter.setCallbacks(this);
        mListView.setAdapter(listAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getBus().register(mListView.getAdapter());
        reloadConversation();
    }

    @Override
    public void onPause() {
        super.onPause();

        BusProvider.getBus().unregister(mListView.getAdapter());
    }

    @Override
    public void onRefreshCompleted(boolean success) {}


    private static class DoVote implements RunnableWithAuthorization {
        private final int post_id;
        private final int value;

        public DoVote(int post_id, int value) {
            this.post_id = post_id;
            this.value = value;
        }

        @Override
        public void onGotAuthorization(AuthorizationRetriever retriever) {
            Object request;
            if(value != 0) {
                request = new CreatePostVoteRequestEvent(retriever, post_id, value);
            } else {
                request = new DeletePostVoteRequestEvent(retriever, post_id);
            }
            BusProvider.getBus().post(request);
        }
    }
    @Override
    protected RunnableWithAuthorization getVotingRunnable(int id, int user_score) {
        return new DoVote(id, user_score);
    }


    private static class DoFlag implements RunnableWithAuthorization {
        private final int post_id;
        private final boolean value;

        public DoFlag(int post_id, boolean value) {
            this.post_id = post_id;
            this.value = value;
        }

        @Override
        public void onGotAuthorization(AuthorizationRetriever retriever) {
            Object request;
            if(value) {
                request = new FlagPostRequestEvent(retriever, post_id);
            } else {
                request = new UnflagPostRequestEvent(retriever, post_id);
            }
            BusProvider.getBus().post(request);
        }
    }
    @Override
    protected RunnableWithAuthorization getFlaggingRunnable(int id, boolean flag) {
        return new DoFlag(id, flag);
    }

    public void reloadConversation() {
        ((ConversationDetailAdapter)mListView.getAdapter()).reload();
    }
}
