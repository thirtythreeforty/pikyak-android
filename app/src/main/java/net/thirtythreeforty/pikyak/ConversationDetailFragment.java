package net.thirtythreeforty.pikyak;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * A list fragment representing a list of Conversations. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link net.thirtythreeforty.pikyak.ConversationDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ConversationDetailFragment extends BaseFragment {
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
        mListView.setAdapter(new ConversationDetailAdapter(
                getActivity(),
                getArguments().getInt(ARG_CONVERSATION_ID)));
        // TODO open fullscreen image when touched?
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

    public void reloadConversation() {
        ((ConversationDetailAdapter)mListView.getAdapter()).reload();
    }
}
