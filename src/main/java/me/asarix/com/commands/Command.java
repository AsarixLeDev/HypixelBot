package me.asarix.com.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class Command {
    public abstract String run(@NotNull SlashCommandInteractionEvent event);

    public abstract CommandData data();

    public final void execute(@NotNull SlashCommandInteractionEvent event) {
        CompletableFuture.supplyAsync(() -> {
            String instant = run(event);
            if (instant != null) event.getHook().sendMessage(instant).queue();
            return "Done.";
        });
    }
}
