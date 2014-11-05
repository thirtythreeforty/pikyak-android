package net.thirtythreeforty.pikyak.networking.model;

import java.util.ArrayList;
import java.util.List;

public class ConversationModel {
    public static class PostModel extends ImageModel {
    }

    public List<PostModel> posts = new ArrayList<>();
}
