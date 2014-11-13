package net.thirtythreeforty.pikyak.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.R;
import net.thirtythreeforty.pikyak.ui.adapters.ConversationDetailAdapter;
import net.thirtythreeforty.pikyak.ui.adapters.VotableImageAdapter;
import net.thirtythreeforty.pikyak.ui.fragments.headless.AuthorizationGetterFragment;
import net.thirtythreeforty.pikyak.ui.views.VotableImage;

/**
 * A list fragment representing a list of Conversations. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ConversationDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ConversationDetailFragment extends BaseFragment
        implements VotableImageAdapter.Callbacks
{
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

    private AuthorizationGetterFragment mAuthorizationGetterFragment;
    private static final String AUTHGETTER_TAG = "authGetter";

    private ListView mListView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConversationDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            mAuthorizationGetterFragment = AuthorizationGetterFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .add(mAuthorizationGetterFragment, AUTHGETTER_TAG)
                    .commit();
        } else {
            mAuthorizationGetterFragment = (AuthorizationGetterFragment)getFragmentManager()
                    .findFragmentByTag(AUTHGETTER_TAG);
        }
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

    @Override
    public void onImageVote(VotableImage view, int score) {
        mAuthorizationGetterFragment.withAuthorization(new DoVote(
                getArguments().getInt(ARG_CONVERSATION_ID, 0),
                score
        ));
    }

    public void reloadConversation() {
        ((ConversationDetailAdapter)mListView.getAdapter()).reload();
    }
}
