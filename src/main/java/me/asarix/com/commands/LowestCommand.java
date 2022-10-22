package me.asarix.com.commands;

import me.asarix.com.FormatUtil;
import me.asarix.com.ItemStack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

public class LowestCommand extends Command {
    @Override
    public String run(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping optionMapping = event.getOption("item_name");
        if (optionMapping == null) {
            return "Veuillez spécifier un item !";
        }
        String option = optionMapping.getAsString();
        ItemStack item;
        try {
            item = new ItemStack(option);
        } catch (Exception e) {
            return e.getMessage();
        }
        double price = item.getLowestBin();
        String msg;
        if (price < 0)
            msg = "Item non trouvé ! Existe-t-il ?";
        else
            msg = "Prix de " + item.getNormalName() + " : " + FormatUtil.format(price);
        return msg;
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.STRING, "item_name",
                "nom de l'item en anglais", true);
        return Commands.slash("price", "get price for item").addOptions(data);
    }
}
