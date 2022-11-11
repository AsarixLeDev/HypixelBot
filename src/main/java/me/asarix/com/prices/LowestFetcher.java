package me.asarix.com.prices;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.asarix.com.ItemStack;
import me.asarix.com.Main;
import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LowestFetcher extends TimerTask {
    private static final ConcurrentMap<String, Double> data = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Double> latest = new ConcurrentHashMap<>();
    private static boolean onGoing = false;

    private static final List<String> invalidNames = new ArrayList<>();

    public static double getUnsafePrice(String itemName) {
        for (String name : data.keySet()) {
            if (name.equalsIgnoreCase(itemName))
                return data.get(name);
        }
        return -1;
    }

    public static double getUnsafePrice(ItemStack item) {
        for (String name : data.keySet()) {
            if (item.hasName(name))
                return data.get(name) * item.getAmount();
        }
        return -1;
    }

    public static double getPrice(String itemName) {
        if (onGoing) return -1;
        for (String name : latest.keySet()) {
            if (name.equalsIgnoreCase(itemName))
                return latest.get(name);
        }
        return -1;
    }

    public static double getPrice(ItemStack item) {
        if (onGoing) return -1;
        for (String name : latest.keySet()) {
            if (item.hasName(name))
                return latest.get(name) * item.getAmount();
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
                    double price = object.get("starting_bid").getAsDouble() / amount;
                    if (name == null) continue;
                    Double lowestBin = latest.get(name);
                    if (lowestBin != null) {
                        if (lowestBin > price)
                            latest.put(name, price);
                    } else {
                        try {
                            new ItemStack(name);
                            latest.put(name, price);
                        } catch (Exception e) {
                            if (!invalidNames.contains(name))
                                invalidNames.add(name);
                        }
                    }
                }
                if (page0.hasNextPage()) {
                    scanPage(page0.getPage() + 1);
                }
            } catch (Exception e) {
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
        for (String key : latest.keySet())
            data.put(key, latest.get(key));
        onGoing = true;
        latest.clear();
        try {
            scanPage(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String name : invalidNames)
            System.out.println("Invalid name : " + name);
        onGoing = false;
    }
}
