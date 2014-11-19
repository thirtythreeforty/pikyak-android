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

    public static class RunWithAuthorizationRequestEvent {
        public RunnableWithAuthorization runnable;
        public String accountName;
        public String accountType;

        public RunWithAuthorizationRequestEvent(RunnableWithAuthorization runnable, String accountName, String accountType) {
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
    public void onAuthTokenGetRequest(RunWithAuthorizationRequestEvent requestEvent) {
        final RunnableWithAuthorization runnable = requestEvent.runnable;
        final String accountName = requestEvent.accountName;
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
