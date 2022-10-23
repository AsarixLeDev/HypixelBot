package me.asarix.com.prices;

import me.asarix.com.ItemStack;
import me.asarix.com.Main;
import me.asarix.com.prices.BazaarPrices;
import net.hypixel.api.reply.skyblock.SkyBlockBazaarReply;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BazaarFetcher extends TimerTask {

    public static ConcurrentMap<String, BazaarPrices> prices = new ConcurrentHashMap<>();

    public static boolean isFromBazaar(String locName) {
        for (String key : prices.keySet()) {
            if (key.equalsIgnoreCase(locName))
                return true;
        }
        return false;
    }

    public static BazaarPrices getPrice(String itemName) {
        for (String name : prices.keySet()) {
            if (name.equalsIgnoreCase(itemName))
                return prices.get(name);
        }
        return null;
    }

    public static BazaarPrices getPrice(ItemStack item) {
        for (String name : prices.keySet()) {
            if (item.hasName(name))
                return prices.get(name);
        }
        return null;
    }

    public void scan() {
        Main.API.getSkyBlockBazaar().whenComplete((page0, throwable) -> {
            try {
                if (throwable != null) {
                    System.err.println("Il y a eu une erreur en analysant les auctions !");
                    return;
                }
                Map<String, SkyBlockBazaarReply.Product> map = page0.getProducts();
                for (String key : map.keySet()) {
                    if (key.toLowerCase().contains("diamond"))
                        System.out.println(key);
                    SkyBlockBazaarReply.Product product = map.get(key);
                    SkyBlockBazaarReply.Product.Status status = product.getQuickStatus();
                    double instaBuy = status.getBuyPrice();
                    double instaSell = status.getSellPrice();
                    List<SkyBlockBazaarReply.Product.Summary> buySummary = product.getBuySummary();
                    double orderBuy = buySummary.isEmpty() ? -1 : buySummary.get(0).getPricePerUnit();
                    List<SkyBlockBazaarReply.Product.Summary> sellSummary = product.getSellSummary();
                    double orderSell = sellSummary.isEmpty() ? -1 : sellSummary.get(0).getPricePerUnit();
                    prices.put(key, new BazaarPrices(instaBuy, instaSell, orderBuy, orderSell));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void run() {
        for (BazaarPrices prices1 : prices.values())
            prices1.disableSafe();
        try {
            scan();
            //System.out.println(prices.get("ENCHANTED_DIAMOND").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
