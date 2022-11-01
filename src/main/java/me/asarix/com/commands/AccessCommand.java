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

public class AccessCommand extends Command {
    @Override
    public String run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        OptionMapping optionMapping = event.getOption("user");
        if (optionMapping == null) {
            return "Veuillez spécifier un utilisateur !";
        }
        User user = optionMapping.getAsUser();
        if (UserManager.hasAccess(user))
            return user.getName() + " : ce joueur a déjà accès aux commandes !";
        UserManager.addAccess(user);
        return user.getName() + " a désormais accès aux commandes !";
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.USER, "user",
                "utilisateur", true);
        return Commands.slash("access", "add access to user").addOptions(data);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ADMIN;
    }
}
