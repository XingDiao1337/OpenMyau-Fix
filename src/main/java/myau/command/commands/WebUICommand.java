package myau.command.commands;

import myau.command.Command;
import myau.web.WebUIManager;
import myau.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class WebUICommand extends Command {
    public WebUICommand() {
        super(new ArrayList<>(Arrays.asList("webui", "web")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() > 1 && args.get(1).equalsIgnoreCase("stop")) {
            WebUIManager.stopServer();
            ChatUtil.sendFormatted("&7[&0M&8y&8a&7u&7]&r &eWebUI server stopped.");
            return;
        }
        WebUIManager.startServer();
        ChatUtil.sendFormatted("&7[&0M&8y&8a&7u&7]&r &aWebUI started at http://localhost:8080/");
    }
}