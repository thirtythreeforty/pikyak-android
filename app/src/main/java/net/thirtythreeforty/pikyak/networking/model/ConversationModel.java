package net.thirtythreeforty.pikyak.networking.model;

import java.util.ArrayList;
import java.util.List;

public class ConversationModel {
    public static class PostModel extends ImageModel {
        public int score = 0;
        public int user_score = 0;
    }

    public List<PostModel> posts = new ArrayList<>();
}
