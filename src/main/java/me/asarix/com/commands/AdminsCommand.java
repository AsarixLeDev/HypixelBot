package me.asarix.com.commands;

import me.asarix.com.PermLevel;
import me.asarix.com.UserManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdminsCommand extends Command {
    @Override
    public String run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        List<User> admins = UserManager.getAdmins();
        StringBuilder builder = new StringBuilder("**__Liste des admins__**\n\n");
        for (User user : admins) {
            builder.append("**")
                    .append(user.getName())
                    .append("** (").append(user.getAsTag())
                    .append(")\n");
        }
        return builder.toString();
    }

    @Override
    public CommandData data() {
        return Commands.slash("admins", "list of admins");
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.NONE;
    }
}
