package net.thirtythreeforty.pikyak.networking.model;

import java.util.ArrayList;
import java.util.List;

public class ConversationModel {
    public static class PostPreviewModel {
        public int id = 0;
        public String url = "";
    }

    List<PostPreviewModel> posts = new ArrayList<>();
}
