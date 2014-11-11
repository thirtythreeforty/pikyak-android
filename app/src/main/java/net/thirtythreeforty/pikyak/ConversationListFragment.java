package net.thirtythreeforty.pikyak;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import net.thirtythreeforty.pikyak.networking.model.ImageModel;

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
    extends BaseFragment
    implements OnItemClickListener,
               OnRefreshListener,
               VotableImageAdapter.Callbacks
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

    private ListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(int id);
    }
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(int id) {}
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

        mListView = (ListView)mSwipeRefreshLayout.findViewById(R.id.listView);
        ConversationListAdapter listAdapter = new ConversationListAdapter(getActivity());
        listAdapter.setCallbacks(this);
        mListView.setAdapter(listAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
        BusProvider.getBus().register(mListView.getAdapter());

        reloadConversationList();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
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
        ((Callbacks)mCallbacks).onItemSelected(convPreview.id);
    }

    @Override
    public void onRefresh() {
        doReloadConversationList();
    }

    @Override
    public void onRefreshCompleted(boolean success) {
        mSwipeRefreshLayout.setRefreshing(false);
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
