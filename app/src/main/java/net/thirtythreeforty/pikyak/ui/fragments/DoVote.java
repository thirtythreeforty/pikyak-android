package net.thirtythreeforty.pikyak.ui.fragments;

import net.thirtythreeforty.pikyak.BusProvider;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.AuthorizationRetriever;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.CreateVoteRequestEvent;
import net.thirtythreeforty.pikyak.networking.PikyakAPIService.DeleteVoteRequestEvent;
import net.thirtythreeforty.pikyak.ui.fragments.headless.AuthorizationGetterFragment.RunnableWithAuthorization;

class DoVote implements RunnableWithAuthorization {
        private final int conversation_id;
        private final int value;

        public DoVote(int conversation_id, int value) {
            this.conversation_id = conversation_id;
            this.value = value;
        }

        @Override
        public void onGotAuthorization(AuthorizationRetriever retriever) {
            Object request;
            if(value != 0) {
                request = new CreateVoteRequestEvent(retriever, conversation_id, value);
            } else {
                request = new DeleteVoteRequestEvent(retriever, conversation_id);
            }
            BusProvider.getBus().post(request);
        }
    }