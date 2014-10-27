package net.thirtythreeforty.pikyak.networking.model;

import java.util.ArrayList;
import java.util.List;

public class ConversationModel {
    public static class PostPreviewModel extends ImageModel {
        public int score = 0;
        public int user_score = 0;
    }

    List<PostPreviewModel> posts = new ArrayList<>();
}
