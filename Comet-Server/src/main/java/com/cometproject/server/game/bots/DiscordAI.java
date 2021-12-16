package com.cometproject.server.game.bots;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class DiscordAI {

    public static void main(String[] args) {
        DiscordApi api = new DiscordApiBuilder().setToken("NTY3MzM0NTA0NDgyNjAzMDA5.XLSBgg.5PyARVChlHRpy7ohX7_ygMAn2Xo").login().join();
        System.out.println("Logged in!");
    }

}