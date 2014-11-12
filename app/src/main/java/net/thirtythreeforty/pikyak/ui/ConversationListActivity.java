package net.thirtythreeforty.pikyak.ui;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.R;
import net.thirtythreeforty.pikyak.auth.AccountAuthenticator;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.CreateConversationRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.CreateConversationResultEvent;
import net.thirtythreeforty.pikyak.ui.fragments.headless.AuthorizationGetterFragment;
import net.thirtythreeforty.pikyak.ui.fragments.ConversationDetailFragment;
import net.thirtythreeforty.pikyak.ui.fragments.ConversationListFragment;
import net.thirtythreeforty.pikyak.ui.fragments.headless.ImageDispatcherFragment;

import java.io.IOException;


/**
 * An activity representing a list of Conversations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ConversationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link net.thirtythreeforty.pikyak.ui.fragments.ConversationListFragment} and the item details
 * (if present) is a {@link net.thirtythreeforty.pikyak.ui.fragments.ConversationDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link net.thirtythreeforty.pikyak.ui.fragments.ConversationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ConversationListActivity
        extends OttoActivity
        implements
            ConversationListFragment.Callbacks,
            ConversationDetailFragment.Callbacks,
            ImageDispatcherFragment.Callbacks,
            AccountManagerCallback<Bundle>,
            AuthorizationGetterFragment.Callbacks
{
    private static final String TAG = "ConversationListActivity";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private ImageDispatcherFragment mImageDispatcherFragment;
    private static final String IMAGEDISPATCHER_TAG = "dispatcher";

    private AuthorizationGetterFragment mAuthorizationGetterFragment;
    private static final String AUTHGETTER_TAG = "authGetter";

    private String mImagePath;

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

        if(savedInstanceState == null) {
            mImageDispatcherFragment = ImageDispatcherFragment.newInstance();
            mAuthorizationGetterFragment = AuthorizationGetterFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(mImageDispatcherFragment, IMAGEDISPATCHER_TAG)
                    .add(mAuthorizationGetterFragment, AUTHGETTER_TAG)
                    .commit();
        } else {
            mImageDispatcherFragment = (ImageDispatcherFragment)fragmentManager
                    .findFragmentByTag(IMAGEDISPATCHER_TAG);
            mAuthorizationGetterFragment = (AuthorizationGetterFragment)fragmentManager
                    .findFragmentByTag(AUTHGETTER_TAG);
        }

        displayAlphaDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_list_activity, menu);
        if(mTwoPane) {
            inflater.inflate(R.menu.conversation_detail, menu);
        }
        inflater.inflate(R.menu.conversation_common, menu);
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
                AccountManager.get(this).addAccount(
                        AccountAuthenticator.ACCOUNT_TYPE,
                        AccountAuthenticator.AUTHTOKEN_TYPE,
                        null,
                        null,
                        this,
                        this,
                        null);
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
        mImagePath = imagePath;

        /** Will call {@link onGetAuthorization} with credentials when ready */
        mAuthorizationGetterFragment.getAuthorization();
    }

    @Override
    public void run(AccountManagerFuture<Bundle> bundleAccountManagerFuture) {
        try {
            Bundle result = bundleAccountManagerFuture.getResult();
        } catch(IOException|AuthenticatorException e) {
            Log.wtf(TAG, e);
        } catch(OperationCanceledException e) {
            Log.i(TAG, "Account operation was cancelled.", e);
        }
    }

    @Override
    public void onGetAuthorization(AuthorizationRetriever authorizationRetriever) {
        BusProvider.getBus().post(new CreateConversationRequestEvent(
                authorizationRetriever,
                mImagePath
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
