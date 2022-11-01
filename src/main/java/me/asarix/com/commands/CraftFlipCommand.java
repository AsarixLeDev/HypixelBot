package me.asarix.com.commands;

import me.asarix.com.*;
import me.asarix.com.prices.LowestFetcher;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CraftFlipCommand extends Command {
    private final int MAX_MSG_LENGTH = 1900;

    @Override
    public String run(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping optionMapping = event.getOption("item_name");
        if (optionMapping == null) {
            return "Veuillez spécifier un item !";
        }
        String option = optionMapping.getAsString();
        ItemStack itemStack;
        try {
            itemStack = new ItemStack(option);
        } catch (RuntimeException e) {
            return option + " : cet item n'existe pas !";
        }
        final String[] msg = {"**__" + itemStack.getNormalName() + "__**\n\n"};
        if (itemStack.isFromBazaar()) {
            new BazaarItem(itemStack).getDefaultPrice().whenComplete(
                    ((bazaarPrices, throwable) -> {
                        if (throwable != null) {
                            event.getHook().sendMessage(msg[0] + throwable.getMessage()).queue();
                            return;
                        }
                        msg[0] += "**Prix** (vente) : " + bazaarPrices.toString() + "\n\n";
                        completeItemInfo(event, msg, itemStack, bazaarPrices.getInstaSell());
                    })
            );
        } else {
            double price = LowestFetcher.getPrice(itemStack);
            if (price < 0) {
                msg[0] += "Prix non trouvé !\n\n";
            } else {
                msg[0] += "Prix (vente) : " + FormatUtil.format(price) + "\n\n";
            }
            completeItemInfo(event, msg, itemStack, price);
        }
        return null;
    }

    private void completeItemInfo(SlashCommandInteractionEvent event, final String[] msg, ItemStack item, double price) {
        CompletableFuture.supplyAsync(() -> {
            try {
                List<Recipe> recipes = Main.getRecipes(item);
                if (recipes.size() == 1 && !recipes.get(0).isCraftable()) {
                    msg[0] += "Cet item n'a pas de craft !";
                    event.getHook().sendMessage(msg[0]).queue();
                    return "pute1";
                }

                for (Recipe recipe : recipes) {
                    try {
                        recipe.calculate().getPriceCompletable().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                recipes = recipes.stream()
                        .sorted(Comparator.comparingInt(Recipe::getPrice))
                        .filter(Recipe::isSuccessful)
                        .filter(Recipe::isCraftable)
                        .toList();
                int count = 1;
                for (Recipe recipe : recipes) {
                    msg[0] += "```Possibilité " + count + "```\n";
                    HashMap<ItemStack, Double> prices = recipe.getPrices();
                    for (ItemStack ing : prices.keySet()) {
                        msg[0] += ing.getNormalName() + " x" + ing.getAmount();
                        if (prices.get(ing) < 0)
                            msg[0] += " (non trouvé)";
                        else
                            msg[0] += " (" + FormatUtil.format(prices.get(ing)) + ")";
                        msg[0] += "\n";
                    }
                    msg[0] += "\n";
                    if (recipe.isSuccessful()) {
                        msg[0] += "Prix : " + FormatUtil.format(recipe.getPrice()) + "\n";
                        msg[0] += "Profit : " + FormatUtil.format(price - recipe.getPrice()) + "\n";
                    } else {
                        msg[0] += "Un ou plusieurs items n'ont pas de prix attribué !\n";
                    }
                    msg[0] += "\n";
                    count++;
                }
                List<String> ms = split(msg[0]);
                event.getHook().sendMessage(ms.get(0)).queue();
                if (ms.size() > 1) {
                    for (int i = 1; i < ms.size(); i++)
                        event.getChannel().sendMessage(ms.get(i)).queue();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "pute";
        });
    }

    private List<String> split(String str) {
        if (str.length() < MAX_MSG_LENGTH)
            return List.of(str);
        List<String> get = new LinkedList<>();
        while (str.length() > MAX_MSG_LENGTH) {
            String temp = str.substring(0, MAX_MSG_LENGTH);
            int ind = temp.lastIndexOf("\n") + 1;
            get.add(str.substring(0, ind));
            str = str.substring(ind);
            if (str.isBlank() || str.equals("\n")) break;
        }
        if (!str.isBlank() && !str.isEmpty() && !str.equals("\n"))
            get.add(str);
        return get;
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.STRING, "item_name",
                "nom de l'item en anglais", true);
        return Commands.slash("craftflip", "get craft price for item").addOptions(data);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ACCESS;
    }
}
