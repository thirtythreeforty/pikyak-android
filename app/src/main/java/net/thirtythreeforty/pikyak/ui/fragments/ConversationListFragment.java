package net.thirtythreeforty.pikyak.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.R;
import net.thirtythreeforty.pikyak.auth.AuthTokenGetterService.RunnableWithAuthorization;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.CreateConversationVoteRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.DeleteConversationVoteRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.FlagConversationRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.UnflagConversationRequestEvent;
import net.thirtythreeforty.pikyak.networking.model.ImageModel;
import net.thirtythreeforty.pikyak.ui.adapters.ConversationListAdapter;

/**
 * A list fragment representing a list of Conversations. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ConversationDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ConversationListFragment
    extends VotableImageListFragment
    implements OnItemClickListener,
               OnRefreshListener
{

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private AbsListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(View view, int id);
    }
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(View view, int id) {}
    };
    @Override
    protected Callbacks getDefaultCallbacks() {
        return sDummyCallbacks;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConversationListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.rgb(0x5D, 0x40, 0x37));

        mListView = (AbsListView)mSwipeRefreshLayout.findViewById(R.id.listView);
        ConversationListAdapter listAdapter = new ConversationListAdapter(getActivity());
        listAdapter.setCallbacks(this);
        mListView.setAdapter(listAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(mListView.getAdapter());

        reloadConversationList();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(mListView.getAdapter());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ImageModel convPreview
                = (ImageModel)parent.getAdapter().getItem(position);
        ((Callbacks)mCallbacks).onItemSelected(view, convPreview.id);
    }

    @Override
    public void onRefresh() {
        doReloadConversationList();
    }

    @Override
    public void onRefreshCompleted(boolean success) {
        mSwipeRefreshLayout.setRefreshing(false);
    }

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
                request = new CreateConversationVoteRequestEvent(retriever, conversation_id, value);
            } else {
                request = new DeleteConversationVoteRequestEvent(retriever, conversation_id);
            }
            BusProvider.getBus().post(request);
        }
    }
    @Override
    protected RunnableWithAuthorization getVotingRunnable(int id, int user_score) {
        return new DoVote(id, user_score);
    }


    private static class DoFlag implements RunnableWithAuthorization {
        private final int conversation_id;
        private final boolean value;

        public DoFlag(int conversation_id, boolean value) {
            this.conversation_id = conversation_id;
            this.value = value;
        }

        @Override
        public void onGotAuthorization(AuthorizationRetriever retriever) {
            Object request;
            if(value) {
                request = new FlagConversationRequestEvent(retriever, conversation_id);
            } else {
                request = new UnflagConversationRequestEvent(retriever, conversation_id);
            }
            BusProvider.getBus().post(request);
        }
    }
    @Override
    protected RunnableWithAuthorization getFlaggingRunnable(int id, boolean flag) {
        return new DoFlag(id, flag);
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        mListView.setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    public void reloadConversationList() {
        mSwipeRefreshLayout.setRefreshing(true);
        doReloadConversationList();
}

    private void doReloadConversationList() {
        // This method is required to avoid messing with the mSwipeRefreshLayout if it is the View
        // that triggered the refresh.
        ((ConversationListAdapter)mListView.getAdapter()).reload();
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            mListView.setItemChecked(mActivatedPosition, false);
        } else {
            mListView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
