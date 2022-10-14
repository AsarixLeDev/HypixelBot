package me.asarix.com;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Recipe {
    private final ItemStack target;
    private List<ItemStack> ingredients;
    private final CompletableFuture<Double> priceCompletable = new CompletableFuture<>();
    private Double price = null;
    private Boolean success = null;
    private boolean isCraftable = true;
    private final HashMap<ItemStack, Double> prices = new HashMap<>();

    public Recipe(ItemStack target, List<ItemStack> ingredients) {
        this.target = target;
        this.ingredients = List.copyOf(ingredients);
        if (ingredients.size() == 1 && ingredients.get(0).equals(target)) {
            isCraftable = false;
        }
        else {
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
        System.out.println("Calculating...");
        double[] total = {0};
        List<ItemStack> bazaar = new ArrayList<>();
        List<ItemStack> auction = new ArrayList<>();
        for (ItemStack ingredient : ingredients) {
            if (ingredient.fromBazaar)
                bazaar.add(ingredient);
            else
                auction.add(ingredient);
        }
        System.out.println("Filled lists");
        if (!auction.isEmpty()) {
            boolean completeNext = bazaar.isEmpty();
            for (ItemStack item : auction) {
                double price = LowestFetcher.getPrice(item);
                prices.put(item, price);
                if (price < 0) {
                    success = false;
                    continue;
                }
                total[0] += price;
            }
            if (completeNext) {
                if (success == null)
                    success = true;
                priceCompletable.complete(total[0]);
            }
        }
        if (!bazaar.isEmpty()) {
            BazaarItem bazaarItem = new BazaarItem(bazaar);
            bazaarItem.getPrices().whenComplete(((map, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                    success = false;
                    priceCompletable.complete(-1.0);
                    return;
                }
                boolean completeNext = total[0] != 0 || auction.isEmpty();
                for (ItemStack itemStack : map.keySet()) {
                    double price = map.get(itemStack).instaBuy*itemStack.amount;
                    prices.put(itemStack, price);
                    total[0] += price;
                }
                if (completeNext) {
                    if (success == null)
                        success = true;
                    priceCompletable.complete(total[0]);
                }
            }));
        }
        return this;
    }

    public Recipe(ItemStack item) {
        this(item, List.of(item));
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
