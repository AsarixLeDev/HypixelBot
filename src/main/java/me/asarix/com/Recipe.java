package me.asarix.com;

import me.asarix.com.prices.BazaarFetcher;
import me.asarix.com.prices.BazaarPrices;
import me.asarix.com.prices.LowestFetcher;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Recipe {
    private final ItemStack target;
    private final CompletableFuture<Double> priceCompletable = new CompletableFuture<>();
    private final HashMap<ItemStack, Double> prices = new HashMap<>();
    private List<ItemStack> ingredients;
    private Double price = null;
    private Boolean success = null;
    private boolean isCraftable = true;

    public Recipe(ItemStack target, List<ItemStack> ingredients) {
        this.target = target;
        this.ingredients = List.copyOf(ingredients);
        if (ingredients.size() == 1 && ingredients.get(0).equals(target)) {
            isCraftable = false;
        } else {
            List<ItemStack> items = new LinkedList<>();
            loop1:
            for (ItemStack itemStack : ingredients) {
                for (ItemStack item : items) {
                    if (item.equals(itemStack)) {
                        item.setAmount(item.amount + itemStack.amount);
                        continue loop1;
                    }
                }
                items.add(itemStack);
            }
            this.ingredients = items;
        }
        priceCompletable.whenComplete(((aDouble, throwable) -> price = aDouble));
    }

    public Recipe(ItemStack item) {
        this(item, List.of(item));
    }

    public CompletableFuture<Double> getPriceCompletable() {
        return priceCompletable;
    }

    public int getPrice() {
        if (price == null)
            throw new RuntimeException("Price hasn't been calculated !");
        return (int) Math.round(price);
    }

    public boolean isSuccessful() {
        if (success == null)
            throw new RuntimeException("Price hasn't been calculated !");
        return success;
    }

    public Recipe calculate() {
        try {
            double[] total = {0};
            List<ItemStack> bazaar = new ArrayList<>();
            List<ItemStack> auction = new ArrayList<>();
            List<ItemStack> pnj = new ArrayList<>();
            for (ItemStack ingredient : ingredients) {
                if (ingredient.fromBazaar)
                    bazaar.add(ingredient);
                else if (ingredient.fromPnj)
                    pnj.add(ingredient);
                else
                    auction.add(ingredient);
            }
            boolean[] done = {
                    bazaar.isEmpty(),
                    auction.isEmpty(),
                    pnj.isEmpty()
            };
            if (!auction.isEmpty()) {
                for (ItemStack item : auction) {
                    double price = LowestFetcher.getPrice(item) * item.amount;
                    if (price < 0) {
                        success = false;
                        prices.put(item, -1.0);
                        continue;
                    }
                    prices.put(item, price);
                    total[0] += price;
                }
                done[1] = true;
                if (done[0] && done[2]) {
                    if (success == null)
                        success = true;
                    priceCompletable.complete(total[0]);
                }
            }
            if (!bazaar.isEmpty()) {
                for (ItemStack item : bazaar) {
                    BazaarPrices prices1 = BazaarFetcher.getPrice(item);
                    if (prices1 == null) {
                        success = false;
                        prices.put(item, -1.0);
                        continue;
                    }
                    double price = prices1.getInstaBuy() * item.amount;
                    prices.put(item, price);
                    total[0] += price;
                }
                done[0] = true;
                if (done[2]) {
                    if (success == null)
                        success = true;
                    priceCompletable.complete(total[0]);
                }
            }
            if (!pnj.isEmpty()) {
                for (ItemStack itemStack : pnj) {
                    if (!Main.pnjItems.containsKey(itemStack.locName)) {
                        success = false;
                        priceCompletable.complete(-1.0);
                        prices.put(itemStack, -1.0);
                        continue;
                    }
                    double price = Main.pnjItems.get(itemStack.locName);
                    prices.put(itemStack, price);
                    total[0] += price;
                }
                done[2] = true;
                if (success == null)
                    success = true;
                priceCompletable.complete(total[0]);
            }
            return this;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return target.equals(recipe.target) && ingredients.equals(recipe.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, ingredients);
    }

    public List<ItemStack> getIngredients() {
        return ingredients;
    }

    public ItemStack getTarget() {
        return target;
    }

    public boolean isCraftable() {
        return isCraftable;
    }

    public void print() {
        System.out.println("--------------------------");
        for (ItemStack item : ingredients) {
            System.out.println(item.normalName + " x" + item.amount);
        }
        System.out.println("--------------------------");
    }

    public List<ItemStack> copyOfIngredients() {
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack itemStack : ingredients)
            list.add(itemStack.copy());
        return list;
    }

    public HashMap<ItemStack, Double> getPrices() {
        return prices;
    }
}
