package me.asarix.com.commands;

import me.asarix.com.PermLevel;
import me.asarix.com.UserManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class Command {
    public abstract String run(@NotNull SlashCommandInteractionEvent event) throws Exception;

    public abstract CommandData data();

    public final void execute(@NotNull SlashCommandInteractionEvent event) {
        CompletableFuture.supplyAsync(() -> {
            String instant;
            try {
                if (!UserManager.hasPerm(event.getUser(), permLevel()))
                    instant = "Tu n'as pas les permissions pour utiliser la commande !";
                else
                    instant = run(event);
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("Il y a eu une erreur ! " + e.getMessage()).queue();
                return "Failure.";
            }
            if (instant != null) event.getHook().sendMessage(instant).queue();
            return "Done.";
        });
    }

    public abstract PermLevel permLevel();
}
