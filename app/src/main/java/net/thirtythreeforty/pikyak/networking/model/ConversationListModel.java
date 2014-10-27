package net.thirtythreeforty.pikyak.networking.model;

import java.util.ArrayList;
import java.util.List;

public class ConversationListModel {
    public static class ConversationPreviewModel extends ImageModel {
        public String url = "";
    }

    public List<ConversationPreviewModel> conversations = new ArrayList<>();
}
