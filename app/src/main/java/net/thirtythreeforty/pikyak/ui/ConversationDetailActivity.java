package net.thirtythreeforty.pikyak.ui;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.R;
import net.thirtythreeforty.pikyak.auth.AuthTokenGetterService.RunnableWithAuthorization;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.CreatePostRequestEvent;
import net.thirtythreeforty.pikyak.ui.fragments.ConversationDetailFragment;
import net.thirtythreeforty.pikyak.ui.fragments.headless.AuthorizationGetterFragment;
import net.thirtythreeforty.pikyak.ui.fragments.headless.ImageDispatcherFragment;


/**
 * An activity representing a single Conversation detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ConversationListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link net.thirtythreeforty.pikyak.ui.fragments.ConversationDetailFragment}.
 */
public class ConversationDetailActivity
        extends OttoActivity
        implements ConversationDetailFragment.Callbacks,
                   ImageDispatcherFragment.Callbacks
{
    private static final String TAG = "ConversationDetailActivity";

    public static final String ARG_CONVERSATION_ID = "conversation_id";

    private ImageDispatcherFragment mImageDispatcherFragment;
    private static final String IMAGEDISPATCHER_TAG = "dispatcher";

    private AuthorizationGetterFragment mAuthorizationGetterFragment;
    private static final String AUTHGETTER_TAG = "authGetter";

    ConversationDetailFragment mConversationDetailFragment;

    static class DoUpload implements RunnableWithAuthorization {
        private final String imagePath;
        private final int conversation_id;

        public DoUpload(int conversation_id, String imagePath) {
            this.imagePath = imagePath;
            this.conversation_id = conversation_id;
        }

        @Override
        public void onGotAuthorization(AuthorizationRetriever retriever) {
            BusProvider.getBus().post(new CreatePostRequestEvent(
                    retriever,
                    conversation_id,
                    imagePath
            ));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_detail);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.

        final FragmentManager fragmentManager = getFragmentManager();

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(ConversationDetailFragment.ARG_CONVERSATION_ID,
                    getIntent().getIntExtra(ARG_CONVERSATION_ID, 0));
            mConversationDetailFragment = new ConversationDetailFragment();
            mConversationDetailFragment.setArguments(arguments);

            mImageDispatcherFragment = ImageDispatcherFragment.newInstance();
            mAuthorizationGetterFragment = AuthorizationGetterFragment.newInstance();

            getFragmentManager().beginTransaction()
                    .add(R.id.conversation_detail_container, mConversationDetailFragment)
                    .add(mImageDispatcherFragment, IMAGEDISPATCHER_TAG)
                    .add(mAuthorizationGetterFragment, AUTHGETTER_TAG)
                    .commit();
        } else {
            mImageDispatcherFragment = (ImageDispatcherFragment)fragmentManager
                    .findFragmentByTag(IMAGEDISPATCHER_TAG);
            mAuthorizationGetterFragment = (AuthorizationGetterFragment)fragmentManager
                    .findFragmentByTag(AUTHGETTER_TAG);
            mConversationDetailFragment = (ConversationDetailFragment)fragmentManager
                    .findFragmentById(R.id.conversation_detail_container);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_detail_activity, menu);
        inflater.inflate(R.menu.conversation_detail, menu);
        inflater.inflate(R.menu.conversation_common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_reply:
                mImageDispatcherFragment.takePicture();
                return true;
            case R.id.action_refresh:
                mConversationDetailFragment.reloadConversation();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void doUpload(final String imagePath) {
        final int conversation_id = getIntent().getIntExtra(ConversationDetailFragment.ARG_CONVERSATION_ID, 0);
        mAuthorizationGetterFragment.withMandatoryAuthorization(new DoUpload(conversation_id, imagePath));
    }
}
