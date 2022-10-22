package me.asarix.com;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LowestFetcher extends TimerTask {
    public static ConcurrentMap<ItemStack, LowestBinItem> items = new ConcurrentHashMap<>();
    private final ConcurrentMap<ItemStack, LowestBinItem> onGoing = new ConcurrentHashMap<>();
    private CompletableFuture<ConcurrentMap<ItemStack, LowestBinItem>> completable = new CompletableFuture<>();

    public LowestFetcher(List<ItemStack> itemList) {
        for (ItemStack item : itemList)
            items.put(item, new LowestBinItem(item));
    }

    public static double getPrice(String itemName) {
        for (ItemStack itemStack : items.keySet()) {
            if (itemStack.hasName(itemName))
                return items.get(itemStack).getPrice();
        }
        return -1;
    }

    public static double getPrice(ItemStack item) {
        for (ItemStack itemStack : items.keySet()) {
            if (itemStack.hasName(item.locName))
                return items.get(itemStack).getPrice();
        }
        return -1;
    }

    public void scanPage(int page) {
        //System.out.println("Scanning page " + page);
        Main.API.getSkyBlockAuctions(page).whenComplete((page0, throwable) -> {
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
                if (name == null) continue;
                //System.out.println(name);
                for (ItemStack item : onGoing.keySet()) {
                    if (item.hasName(name)) {
                        double price = object.get("starting_bid").getAsDouble() / amount;
                        LowestBinItem lowestBin = onGoing.get(item);
                        lowestBin.request(price);
                    }
                }
            }
            if (page0.hasNextPage()) {
                scanPage(page0.getPage() + 1);
            } else {
                completable.complete(onGoing);
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
        completable = new CompletableFuture<>();
        onGoing.clear();
        for (ItemStack item : items.keySet())
            onGoing.put(item, new LowestBinItem(item));
        try {
            scanPage(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        completable.whenComplete(((map, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }
            items = new ConcurrentHashMap<>(map);
        }));
    }
}
