package me.asarix.com.commands;

import me.asarix.com.FormatUtil;
import me.asarix.com.ItemStack;
import me.asarix.com.PermLevel;
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
        int amount = 1;
        optionMapping = event.getOption("amount");
        if (optionMapping != null) {
            amount = optionMapping.getAsInt();
        }
        ItemStack item;
        try {
            item = new ItemStack(option);
        } catch (Exception e) {
            return e.getMessage();
        }
        boolean unsafe = false;
        double price = item.getPrice(amount);
        if (price < 0) {
            price = item.getUnsafePrice(amount);
            unsafe = true;
        }
        if (price < 0)
            return "Item non trouvé ! Existe-t-il ?";
        String msg = "Prix de " + item.getNormalName() + " x" + amount + " : " + FormatUtil.format(price);
        if (unsafe)
            msg += "\n (attention : cet item n'est pas en vente actuellement";
        return msg;
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.STRING, "item_name",
                "nom de l'item en anglais", true);
        OptionData data1 = new OptionData(OptionType.INTEGER, "amount",
                "nombre d'items désirés", false);
        return Commands.slash("price", "get price for item").addOptions(data, data1);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ACCESS;
    }
}
