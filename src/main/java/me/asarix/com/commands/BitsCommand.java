package me.asarix.com.commands;

import me.asarix.com.BitItem;
import me.asarix.com.FormatUtil;
import me.asarix.com.Main;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BitsCommand extends Command {
    @Override
    public String run(@NotNull SlashCommandInteractionEvent event) {
        List<BitItem> list = new ArrayList<>();
        for (String itemLocName : Main.bitPrices.keySet()) {
            System.out.println(itemLocName);
            list.add(new BitItem(itemLocName, getBitNumb(itemLocName)));
        }
        list = list.stream().sorted((a, b) -> (int) (b.pricePerBit() - a.pricePerBit())).toList();
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            BitItem item = list.get(i);
            msg.append(i + 1).append(". **").append(item.getNormalName()).append("** ")
                    .append(FormatUtil.round(item.pricePerBit()))
                    .append(" par bit (")
                    .append(FormatUtil.round(item.getLowestBin()))
                    .append(" en lowest BIN pour ")
                    .append(item.getBitNumb()).append(" bits)\n");
        }
        return msg.toString();
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
    public CommandData data() {
        return new CommandDataImpl("bits", "Most profitable bits items");
    }
}
