package me.asarix.com.commands;

import me.asarix.com.Main;
import me.asarix.com.weight.WeightCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CommandHandler extends ListenerAdapter {
    private final HashMap<String, Command> commandMap = new HashMap<>();
    private JDA jda;

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        jda = event.getJDA();
        Guild guild = event.getGuild();
        registerCommand(new CraftFlipCommand(), guild);
        registerCommand(new LowestCommand(), guild);
        registerCommand(new BitsCommand(), guild);
        registerCommand(new WeightCommand(), guild);
    }

    private void registerCommand(Command command, Guild guild) {
        CommandData data = command.data();
        this.commandMap.put(data.getName(), command);
        guild.upsertCommand(data).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Command command = commandMap.get(event.getName());
        if (command == null) {
            event.getHook().sendMessage("Commande non trouvée ! Est-ce un bug ?").queue();
            return;
        }
        command.execute(event);
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        System.out.println("SHUTDOWN");
        Main.bazaarTimer.cancel();
        Main.lowestTimer.cancel();
    }
}
