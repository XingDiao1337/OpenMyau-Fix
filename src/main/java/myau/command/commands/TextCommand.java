package myau.command.commands;

import myau.Myau;
import myau.command.Command;
import myau.module.modules.HUD;
import myau.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class TextCommand extends Command {
    public TextCommand() {
        super(new ArrayList<>(Arrays.asList("text")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() < 2) {
            // No arguments — clear custom text
            HUD.customText = null;
            ChatUtil.sendFormatted(
                    String.format("%sCustom HUD text &ccleared&r.", Myau.clientName)
            );
        } else {
            // Join all remaining args as the text
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.size(); i++) {
                if (i > 1) sb.append(" ");
                sb.append(args.get(i));
            }
            HUD.customText = sb.toString();
            ChatUtil.sendFormatted(
                    String.format("%sCustom HUD text set to: &f%s&r", Myau.clientName, HUD.customText)
            );
        }
    }
}
