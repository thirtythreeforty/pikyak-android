package net.thirtythreeforty.pikyak.networking.model;

import java.util.ArrayList;
import java.util.List;

public class ConversationListModel {
    public static class ConversationPreviewModel {
        public int id = 0;
        public String url = "";
        public String thumbnail = "";
    }

    public List<ConversationPreviewModel> conversations = new ArrayList<>();
}
