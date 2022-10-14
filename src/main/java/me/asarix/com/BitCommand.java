package me.asarix.com;

import me.asarix.com.commands.Command;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BitCommand extends ListenerAdapter {
    private final HashMap<String, Command> commandMap = new HashMap<>();

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
    }

    private void registerCommand(String name, Command command) {
        this.commandMap.put(name, command);
    }

    private int getBitNumb(ItemStack item) {
        Map<String, Integer> bitItems = Main.bitPrices;
        for (String key : bitItems.keySet()) {
            if (key.equalsIgnoreCase(item.locName))
                return bitItems.get(key);
        }
        return -1;
    }

    private int getBitNumb(String locName) {
        Map<String, Integer> bitItems = Main.bitPrices;
        for (String key : bitItems.keySet()) {
            if (key.equalsIgnoreCase(locName))
                return bitItems.get(key);
        }
        return -1;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("bits")) {
            event.deferReply().queue();
            List<BitItem> list = new ArrayList<>();
            for (String itemLocName : Main.bitPrices.keySet()) {
                System.out.println(itemLocName);
                list.add(new BitItem(itemLocName, getBitNumb(itemLocName), LowestFetcher.getPrice(itemLocName)));
            }
            list = list.stream().sorted((a, b) -> (int) (b.pricePerBit() - a.pricePerBit())).toList();
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                BitItem item = list.get(i);
                msg.append(i + 1).append(". **").append(item.itemName).append("** ")
                        .append(rounded(item.pricePerBit())).append(" par bit (").append(rounded(item.lowestBin))
                        .append(" en lowest BIN pour ").append(item.bitNumb).append(" bits)\n");
            }
            event.getHook().sendMessage(msg.toString()).queue();
        } else if (event.getName().equals("lowest")) {
            event.deferReply().queue();
            OptionMapping optionMapping = event.getOption("item_name");
            if (optionMapping == null) {
                event.getHook().sendMessage("Veuillez spécifier un item !").queue();
                return;
            }
            String option = optionMapping.getAsString();
            double price = LowestFetcher.getPrice(option);
            String msg;
            if (price < 0)
                msg = "Item non trouvé ! Existe-t-il ?";
            else
                msg = "Prix de " + new ItemStack(option).normalName + " : " + FormatUtil.format(price);
            event.getHook().sendMessage(msg).queue();
        } else if (event.getName().equals("price")) {
            event.deferReply().queue();
            OptionMapping optionMapping = event.getOption("item_name");
            if (optionMapping == null) {
                event.getHook().sendMessage("Veuillez spécifier un item !").queue();
                return;
            }
            String option = optionMapping.getAsString();
            ItemStack itemStack;
            try {
                itemStack = new ItemStack(option);
            } catch (RuntimeException e) {
                event.getHook().sendMessage(option + " : cet item n'existe pas !").queue();
                return;
            }
            CompletableFuture<PriceType> price = Main.getPrice(itemStack);
            price.whenComplete(((priceType, throwable) -> {
                if (throwable != null) {
                    event.getHook().sendMessage(throwable.getMessage()).queue();
                    return;
                }
                if (priceType == PriceType.BAZAAR) {
                    event.getHook().sendMessage("Prix de " + itemStack.normalName + " : " + FormatUtil.format(priceType.value) + " au bazaar en insta-sell").queue();
                } else {
                    event.getHook().sendMessage("Prix de " + itemStack.normalName + " : " + FormatUtil.format(priceType.value) + " aux auctions en BIN").queue();
                }
            }));
        } else if (event.getName().equals("craftprice")) {

        }
    }

    private String rounded(double numb) {
        return FormatUtil.format((int) Math.round(numb));
    }


}
