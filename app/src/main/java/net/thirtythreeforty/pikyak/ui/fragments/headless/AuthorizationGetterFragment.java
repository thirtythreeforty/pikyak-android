package net.thirtythreeforty.pikyak.ui.fragments.headless;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import net.thirtythreeforty.pikyak.auth.AccountAuthenticator;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;

import java.io.IOException;

public class AuthorizationGetterFragment extends Fragment
    implements AccountManagerCallback<Bundle>
{
    private static final String TAG = "AccountChooserFragment";

    private static final int REQUEST_ACCOUNT = 1;

    public static interface Callbacks {
        public void onGetAuthorization(AuthorizationRetriever authorizationRetriever);
    }
    static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onGetAuthorization(AuthorizationRetriever authorizationRetriever) {}
    };
    private Callbacks mCallbacks = sDummyCallbacks;

    public static AuthorizationGetterFragment newInstance() {
        return new AuthorizationGetterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active mCallbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    public void getAuthorization() {
        Intent intent = AccountManager.newChooseAccountIntent(
                null,
                null,
                new String[] {AccountAuthenticator.ACCOUNT_TYPE},
                false,
                null,
                AccountAuthenticator.AUTHTOKEN_TYPE,
                null,
                null
        );
        startActivityForResult(intent, REQUEST_ACCOUNT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ACCOUNT) {
            if(resultCode == Activity.RESULT_OK) {
                String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                String accountType = data.getExtras().getString(AccountManager.KEY_ACCOUNT_TYPE);
                Account account = new Account(accountName, accountType);
                AccountManager.get(getActivity())
                        .getAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE,
                                null, getActivity(), this, null);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void run(AccountManagerFuture<Bundle> bundleAccountManagerFuture) {
        try {
            Bundle result = bundleAccountManagerFuture.getResult();
            final String username = result.getString(AccountManager.KEY_ACCOUNT_NAME);
            final String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
            mCallbacks.onGetAuthorization(new AuthorizationRetriever() {
                @Override
                public String getUsername() {
                    return username;
                }

                @Override
                public String getAuthorization() {
                    return authToken;
                }
            });
        } catch(IOException | AuthenticatorException e) {
            Log.wtf(TAG, e);
        } catch(OperationCanceledException e) {
            Log.i(TAG, "Account operation was cancelled.", e);
        }
    }
}
