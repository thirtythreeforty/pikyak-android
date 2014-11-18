package net.thirtythreeforty.pikyak.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.R;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.CreatePostVoteRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.DeletePostVoteRequestEvent;
import net.thirtythreeforty.pikyak.ui.adapters.ConversationDetailAdapter;
import net.thirtythreeforty.pikyak.ui.fragments.headless.AuthorizationGetterFragment.RunnableWithAuthorization;

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
        private final int conversation_id;
        private final int value;

        public DoVote(int conversation_id, int value) {
            this.conversation_id = conversation_id;
            this.value = value;
        }

        @Override
        public void onGotAuthorization(AuthorizationRetriever retriever) {
            Object request;
            if(value != 0) {
                request = new CreatePostVoteRequestEvent(retriever, conversation_id, value);
            } else {
                request = new DeletePostVoteRequestEvent(retriever, conversation_id);
            }
            BusProvider.getBus().post(request);
        }
    }
    @Override
    protected RunnableWithAuthorization getVotingRunnable(int id, int user_score) {
        return new DoVote(id, user_score);
    }

    public void reloadConversation() {
        ((ConversationDetailAdapter)mListView.getAdapter()).reload();
    }
}
