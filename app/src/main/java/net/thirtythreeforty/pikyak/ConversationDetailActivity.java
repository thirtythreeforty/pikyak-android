package net.thirtythreeforty.pikyak;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.CreatePostRequestEvent;


/**
 * An activity representing a single Conversation detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ConversationListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ConversationDetailFragment}.
 */
public class ConversationDetailActivity extends Activity
        implements ConversationDetailFragment.Callbacks,
                   ImageDispatcherFragment.Callbacks
{
    private static final String TAG = "ConversationDetailActivity";

    private ImageDispatcherFragment mImageDispatcherFragment;
    private static final String IMAGEDISPATCHER_TAG = "dispatcher";

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
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(ConversationDetailFragment.ARG_CONVERSATION_ID,
                    getIntent().getIntExtra(ConversationDetailFragment.ARG_CONVERSATION_ID, 0));
            ConversationDetailFragment conversationFragment = new ConversationDetailFragment();
            conversationFragment.setArguments(arguments);

            mImageDispatcherFragment = ImageDispatcherFragment.newInstance();

            getFragmentManager().beginTransaction()
                    .add(R.id.conversation_detail_container, conversationFragment)
                    .add(mImageDispatcherFragment, IMAGEDISPATCHER_TAG)
                    .commit();
        } else {
            mImageDispatcherFragment = (ImageDispatcherFragment)getFragmentManager()
                    .findFragmentByTag(IMAGEDISPATCHER_TAG);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_add:
                mImageDispatcherFragment.takePicture();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void doUpload(String imagePath) {
        BusProvider.getBus().post(new CreatePostRequestEvent(
                new AuthorizationRetriever() {
                    @Override
                    public String getUsername() {
                        return "test";
                    }

                    @Override
                    public String getPassword() {
                        return "test";
                    }
                },
                getIntent().getIntExtra(ConversationDetailFragment.ARG_CONVERSATION_ID, 0),
                imagePath
        ));
    }
}
