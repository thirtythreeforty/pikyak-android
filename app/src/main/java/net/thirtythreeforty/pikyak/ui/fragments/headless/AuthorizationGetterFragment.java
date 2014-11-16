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
import java.util.HashMap;
import java.util.Random;

public class AuthorizationGetterFragment extends Fragment
    implements AccountManagerCallback<Bundle>
{
    private static final String TAG = "AccountChooserFragment";

    public interface RunnableWithAuthorization {
        public void onGotAuthorization(AuthorizationRetriever retriever);
    }

    private HashMap<Integer, RunnableWithAuthorization> intToFunctionMap;
    private HashMap<AccountManagerFuture<Bundle>, RunnableWithAuthorization> futureToFunctionMap;
    private static Random random = new Random();

    public static AuthorizationGetterFragment newInstance() {
        return new AuthorizationGetterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        intToFunctionMap = new HashMap<>();
        futureToFunctionMap = new HashMap<>();
    }

    public void withAuthorization(RunnableWithAuthorization runnable) {
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

        int key = random.nextInt(Integer.MAX_VALUE - 1) + 1; // Exclude 0
        intToFunctionMap.put(key, runnable);
        startActivityForResult(intent, key);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (intToFunctionMap.containsKey(requestCode)) {
            RunnableWithAuthorization runnable = intToFunctionMap.get(requestCode);
            intToFunctionMap.remove(requestCode);

            if(resultCode == Activity.RESULT_OK) {
                String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                String accountType = data.getExtras().getString(AccountManager.KEY_ACCOUNT_TYPE);
                Account account = new Account(accountName, accountType);
                AccountManagerFuture<Bundle> future = AccountManager.get(getActivity())
                        .getAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE,
                                null, getActivity(), this, null);
                futureToFunctionMap.put(future, runnable);
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
            RunnableWithAuthorization runnable = futureToFunctionMap.get(bundleAccountManagerFuture);
            futureToFunctionMap.remove(bundleAccountManagerFuture);
            runnable.onGotAuthorization(new AuthorizationRetriever() {
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
