package me.asarix.com.commands;

import me.asarix.com.PermLevel;
import me.asarix.com.UserManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

public class AdminCommand extends Command {
    @Override
    public String run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        OptionMapping optionMapping = event.getOption("user");
        if (optionMapping == null) {
            return "Veuillez spécifier un utilisateur !";
        }
        User user = optionMapping.getAsUser();
        if (!UserManager.hasAccess(user))
            UserManager.addAccess(user);
        if (UserManager.hasAdmin(user))
            return user.getName() + " : ce joueur a déjà les permissions administrateur !";
        UserManager.addAdmin(user);
        return user.getName() + " a désormais les permissions administrateur !";
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.USER, "user",
                "utilisateur", true);
        return Commands.slash("admin", "add admin perms to user").addOptions(data);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ADMIN;
    }
}
