package net.thirtythreeforty.pikyak.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.squareup.otto.Subscribe;

import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;

import java.io.IOException;

public class AuthTokenGetterService {
    private static final String TAG = "AuthTokenGetterService";

    public static class RunWithOptionalAuthorizationRequestEvent {
        public RunnableWithAuthorization runnable;
        public String accountName;
        public String accountType;

        public RunWithOptionalAuthorizationRequestEvent(RunnableWithAuthorization runnable, String accountName, String accountType) {
            this.runnable = runnable;
            this.accountName = accountName;
            this.accountType = accountType;
        }
    }

    public interface RunnableWithAuthorization {
        public void onGotAuthorization(AuthorizationRetriever retriever);
    }

    private final Application application;

    public AuthTokenGetterService(Application application) {
        this.application = application;
    }

    @Subscribe
    public void onAuthTokenGetRequest(RunWithOptionalAuthorizationRequestEvent requestEvent) {
        final String accountName = requestEvent.accountName;
        final RunnableWithAuthorization runnable = requestEvent.runnable;

        if(accountName == null || accountName.isEmpty()) {
            Log.d(TAG, "Running request without authorization (accountName = " + accountName + ")");
            // Do the request with no authorization.  Hopefully it's not required!
            runnable.onGotAuthorization(new AuthorizationRetriever() {
                @Override
                public String getUsername() {
                    return null;
                }

                @Override
                public String getAuthorization() {
                    return null;
                }
            });
            return;
        }

        Log.d(TAG, "Attempting to get authorization for account " + accountName);

        final String accountType = requestEvent.accountType;
        Account account = new Account(accountName, accountType);
        AccountManager.get(application)
                .getAuthToken(account,
                        AccountAuthenticator.AUTHTOKEN_TYPE,
                        null,
                        false,
                        new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> bundleAccountManagerFuture) {
                                try {
                                    Bundle result = bundleAccountManagerFuture.getResult();
                                    final String username = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                                    final String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
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
                        },
                        null);
    }
}
