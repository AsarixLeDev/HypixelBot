package me.asarix.com.prices;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.asarix.com.ItemStack;
import me.asarix.com.Main;
import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LowestFetcher extends TimerTask {
    private static final ConcurrentMap<String, LowestBinItem> onGoing = new ConcurrentHashMap<>();

    public static double getPrice(String itemName) {
        for (String name : onGoing.keySet()) {
            if (name.equalsIgnoreCase(itemName))
                return onGoing.get(name).getUnitPrice();
        }
        return -1;
    }

    public static double getPrice(ItemStack item) {
        for (String name : onGoing.keySet()) {
            if (item.hasName(name))
                return onGoing.get(name).getUnitPrice() * item.getAmount();
        }
        return -1;
    }

    public void scanPage(int page) {
        Main.API.getSkyBlockAuctions(page).whenComplete((page0, throwable) -> {
            try {
                if (throwable != null) {
                    System.err.println("Il y a eu une erreur en analysant les auctions !");
                    return;
                }
                JsonArray auctions = page0.getAuctions();
                for (int i = 0; i < auctions.size(); i++) {
                    JsonObject object = auctions.get(i).getAsJsonObject();
                    if (!object.get("bin").getAsBoolean()) continue;
                    String rawName = object.get("item_name").getAsString();
                    String itemBytes = object.get("item_bytes").getAsString();
                    String name = idFromBytes(itemBytes);
                    int amount = amountFromBytes(itemBytes);
                    double price = object.get("starting_bid").getAsDouble();
                    if (name == null) continue;
                    LowestBinItem lowestBin = onGoing.get(name);
                    if (lowestBin != null)
                        lowestBin.request(price);
                    else {
                        try {
                            LowestBinItem toAdd = new LowestBinItem(name, price / amount);
                            onGoing.put(name, toAdd);
                        }
                        catch (Exception ignored) {}
                    }
                }
                if (page0.hasNextPage()) {
                    scanPage(page0.getPage() + 1);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String idFromBytes(String bytes) {
        NBTList nbtList;
        try {
            nbtList = NBTReader.readBase64(bytes).getList("i");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        NBTCompound compound = nbtList.getCompound(0);
        NBTCompound compound1 = compound.getCompound("tag");
        NBTCompound compound2 = compound1.getCompound("ExtraAttributes");
        return compound2.getString("id");
    }

    private int amountFromBytes(String bytes) {
        NBTList nbtList;
        try {
            nbtList = NBTReader.readBase64(bytes).getList("i");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        NBTCompound compound = nbtList.getCompound(0);
        boolean ok = compound.containsKey("Count");
        if (!ok) System.out.println("Pas ok");
        return compound.getInt("Count", 1);
    }

    @Override
    public void run() {
        for (LowestBinItem item : onGoing.values())
            item.disableSafe();
        try {
            scanPage(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
