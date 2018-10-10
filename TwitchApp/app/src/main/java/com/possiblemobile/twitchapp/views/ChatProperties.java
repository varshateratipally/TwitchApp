package com.possiblemobile.twitchapp.views;

import java.util.List;
import java.util.Random;

class ChatProperties {

    private List<String> chatServers;
    private Random random = new Random();

    public ChatProperties(List<String> chatServers) {
        this.chatServers = chatServers;
    }

    public String getChatIp() {
        int positions = random.nextInt(chatServers.size());
        return chatServers.get(positions);
    }
}
