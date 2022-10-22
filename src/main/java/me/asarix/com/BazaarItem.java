package me.asarix.com;

import net.hypixel.api.reply.skyblock.SkyBlockBazaarReply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BazaarItem {
    private final ItemStack mainItem;
    private final List<ItemStack> itemStacks = new ArrayList<>();
    private final Map<ItemStack, BazaarPrices> prices = new HashMap<>();
    private final CompletableFuture<Map<ItemStack, BazaarPrices>> lowest = new CompletableFuture<>();

    public BazaarItem(String itemName) {
        this(List.of(new ItemStack(itemName)));
    }

    public BazaarItem(ItemStack item) {
        this(List.of(item));
    }

    public BazaarItem(List<ItemStack> items) {
        this.mainItem = items.get(0);
        itemStacks.addAll(items);
        scanPage();
    }

    public static BazaarItem of(List<String> itemNames) {
        List<ItemStack> list = new ArrayList<>();
        for (String itemName : itemNames) {
            list.add(new ItemStack(itemName));
        }
        return new BazaarItem(list);
    }
//TODO BazaarFetcher
    public void scanPage() {
        Main.API.getSkyBlockBazaar().whenComplete((page0, throwable) -> {
            try {
                if (throwable != null) {
                    throwable.printStackTrace();
                    System.exit(0);
                    lowest.complete(null);
                    return;
                }
                if (itemStacks.size() == 1) {
                    System.out.println(1);
                    String productName = mainItem.locName;
                    System.out.println(productName);
                    SkyBlockBazaarReply.Product product = page0.getProduct(productName);
                    if (product == null) {
                        System.out.println("non non non");
                        lowest.completeExceptionally(new Throwable("Product doesn't exist !"));
                        return;
                    }
                    SkyBlockBazaarReply.Product.Status status = product.getQuickStatus();
                    double instaBuy = status.getBuyPrice();
                    double instaSell = status.getSellPrice();
                    List<SkyBlockBazaarReply.Product.Summary> buySummary = product.getBuySummary();
                    List<SkyBlockBazaarReply.Product.Summary> sellSummary = product.getSellSummary();
                    double orderBuy = buySummary.get(0).getPricePerUnit();
                    double orderSell = sellSummary.get(0).getPricePerUnit();
                    BazaarPrices prices1 = new BazaarPrices(instaBuy, instaSell, orderBuy, orderSell);
                    System.out.println(prices1);
                    prices.put(mainItem, prices1);
                } else {
                    Map<String, SkyBlockBazaarReply.Product> products = page0.getProducts();
                    for (ItemStack item : itemStacks) {
                        String productName = item.locName;
                        SkyBlockBazaarReply.Product product = products.get(productName);
                        if (product == null) {
                            lowest.completeExceptionally(new Throwable("Product doesn't exist !"));
                            return;
                        }
                        SkyBlockBazaarReply.Product.Status status = product.getQuickStatus();
                        double instaBuy = status.getBuyPrice();
                        double instaSell = status.getSellPrice();
                        List<SkyBlockBazaarReply.Product.Summary> buySummary = product.getBuySummary();
                        List<SkyBlockBazaarReply.Product.Summary> sellSummary = product.getSellSummary();
                        double orderBuy = buySummary.get(0).getPricePerUnit();
                        double orderSell = sellSummary.get(0).getPricePerUnit();
                        BazaarPrices prices1 = new BazaarPrices(instaBuy, instaSell, orderBuy, orderSell);
                        prices.put(item, prices1);
                    }
                }
                System.out.println("Compeleted bazaar price");
                lowest.complete(prices);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Map<ItemStack, BazaarPrices>> getPrices() {
        return lowest;
    }

    public CompletableFuture<BazaarPrices> getPrice(String itemName) {
        CompletableFuture<BazaarPrices> get = new CompletableFuture<>();
        lowest.whenComplete(((stringBazaarPricesMap, throwable) -> {
            for (ItemStack item : stringBazaarPricesMap.keySet()) {
                if (item.hasName(itemName))
                    get.complete(stringBazaarPricesMap.get(item));
            }
            get.complete(null);
        }));
        return get;
    }

    public CompletableFuture<BazaarPrices> getDefaultPrice() {
        CompletableFuture<BazaarPrices> get = new CompletableFuture<>();
        lowest.whenComplete(((stringBazaarPricesMap, throwable) -> {
            for (ItemStack item : stringBazaarPricesMap.keySet()) {
                if (item.hasName(mainItem.normalName))
                    get.complete(stringBazaarPricesMap.get(item));
            }
            get.complete(null);
        }));
        return get;
    }
}
