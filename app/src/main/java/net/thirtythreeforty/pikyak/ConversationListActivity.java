package net.thirtythreeforty.pikyak;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.CreateConversationRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.CreateConversationResultEvent;


/**
 * An activity representing a list of Conversations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ConversationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ConversationListFragment} and the item details
 * (if present) is a {@link ConversationDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ConversationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ConversationListActivity
        extends Activity
        implements
            ConversationListFragment.Callbacks,
            ConversationDetailFragment.Callbacks,
            SignInDialogFragment.Callbacks,
            ImageDispatcherFragment.Callbacks
{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private SignInDialogFragment mSignInDialogFragment;
    private static final String SIGNINDIALOGFRAGMENT_TAG = "SignInDialogFragment";

    private ImageDispatcherFragment mImageDispatcherFragment;
    private static final String IMAGEDISPATCHER_TAG = "dispatcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        FragmentManager fragmentManager = getFragmentManager();

        if (findViewById(R.id.conversation_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ConversationListFragment) fragmentManager
                    .findFragmentById(R.id.conversation_list))
                    .setActivateOnItemClick(true);
        }

        // Special case here, this fragment is only attached when we want the dialog to appear.
        mSignInDialogFragment = SignInDialogFragment.newInstance();

        if(savedInstanceState == null) {
            mImageDispatcherFragment = ImageDispatcherFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(mImageDispatcherFragment, IMAGEDISPATCHER_TAG)
                    .commit();
        } else {
            mImageDispatcherFragment = (ImageDispatcherFragment)fragmentManager
                    .findFragmentByTag(IMAGEDISPATCHER_TAG);
        }

        displayAlphaDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_add:
                mImageDispatcherFragment.takePicture();
                return true;
            case R.id.action_refresh:
                ((ConversationListFragment) getFragmentManager()
                        .findFragmentById(R.id.conversation_list))
                        .reloadConversationList();
                return true;
            case R.id.action_settings:
                getFragmentManager().beginTransaction()
                        .add(mSignInDialogFragment, SIGNINDIALOGFRAGMENT_TAG)
                        .commit();
                return true;
        }
        return false;
    }

    /**
     * Callback method from {@link ConversationListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(ConversationDetailFragment.ARG_CONVERSATION_ID, id);
            ConversationDetailFragment fragment = new ConversationDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.conversation_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ConversationDetailActivity.class);
            detailIntent.putExtra(ConversationDetailFragment.ARG_CONVERSATION_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public void doUpload(String imagePath) {
        BusProvider.getBus().post(new CreateConversationRequestEvent(
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
                imagePath
        ));
    }

    @Subscribe
    public void onCreateConversationResultEvent(CreateConversationResultEvent resultEvent) {
        ((ConversationListFragment) getFragmentManager()
                .findFragmentById(R.id.conversation_list))
                .reloadConversationList();
    }

    private void displayAlphaDialog() {
        final String welcomeScreenShownPref = "alphaDialogShown";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // second argument is the default to use if the preference can't be found
        Boolean welcomeScreenShown = prefs.getBoolean(welcomeScreenShownPref, false);

        if (!welcomeScreenShown) {
            new Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.message_alpha_warning)
                    .setPositiveButton(
                            R.string.ok, null)
                    .show();
            Editor editor = prefs.edit();
            editor.putBoolean(welcomeScreenShownPref, true);
            editor.commit(); // Very important to save the preference
        }
    }
}
