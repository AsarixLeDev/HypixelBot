package me.asarix.com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    public static final Map<String, Integer> bitPrices = new HashMap<>();
    private static final Map<String, String> normToLoc = new HashMap<>();
    private static final Map<String, String> locToNorm = new HashMap<>();
    public static List<String> reforges = new ArrayList<>();
    public static List<String> bazaarNames = new ArrayList<>();
    private static final List<ItemStack> nonBazaarItems = new LinkedList<>();
    private static File file;

    public static JDA jda;

    public static void main(String[] args) {
        fillLists();
        jda = JDABuilder.createDefault("MTAyNTcyNjUzNzcyNDYwMDQ2MA.GhGr0m.n57p25GLYIcD0_8DByh7lmDdVZ3jRZLWFcc1xQ")
                .setActivity(Activity.playing("Hypixel"))
                .addEventListeners(new BitCommand())
                .build();
        registerCommands();
        try {
            readInmFile();
            readBitFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        //test();
        fetchLoop();
    }

    private static void fetchLoop() {
        Timer timer = new Timer();
        TimerTask lowestFetcher = new LowestFetcher(nonBazaarItems);
        timer.schedule(lowestFetcher, 0, 60*1000);
    }

    private static void test() {
        String bordel = "H4sIAAAAAAAAAFVUzW7bRhAeWXYiMUFjFCn6i2BbNEFS1wkpS7LkQwGBlh3BTgJYrnooCmG1HJELL7kCuYzjY1+gT9Cedegz9KJHyYMUnSVFyxUo7s7fN7Oz39ABaEJNOgBQ24ItGdQe12DH13liag7UDQ+bsI2JiMD+avDg52SWIr/iM4W1OjRfywBPFA8zsv7rwP1AZgvFbyjoXKfYIO1X8PVqeXjMYx7iEVstxZ7X7tCKz/da7gv4hoxjk2ISmqg099yN9Vuy+qk0zI94Im7jn9Laf243L+C7yuV/KTx37dN2yedz8hklBpWSIa5x+F7HhSe06f1K/49//U7v327FP/+wIpXfWS2D1VKVb1/HM80mBPyjRfcV8vd4R6YypOCKTaBlxXwms3hjHiYBpmxM7aFlo/2AIjeEAj8V0gJTaWtko9Go8jmRaWYYtUlekf429FTyxLAzqZQFhB6pRvGCK5mEd6PP0USkNDebpOdyjoSHVCs5gmdVubjaOIwFnSwJCfcO0Dji6SLBLKNk1Pb+ZZTbIymdBpvICSY61rn1AfjBXr3NYvvtdYpLOQyKi2JGs6IjMSakfFlcJD0XKFBSVymiW7grm7CMgSckz1Md341k1xEmLEIVvKSMT1fL7mAm7XGP2FjnSqQ3lk90exej09eXzD8f+WfwjAJPuUwKtrRc9+Pf/7ATTLWwbSIbD8mYGfiM9lUuNtepLaudwZdFrT2LP1f6mvk6M5ZUBx48Jv0bnvCNjuDhU9L6WqtAXydHaxCAXeqXLWxwMWTjX95dHDdg+y2PEb6ous0mmiYsoHadcUOo4MCj4QeT8oEhNsyIOFkddiNtpgttuNFTYUeXoJ0GNGIdyLnEFHYyi9Ww8w2PJu9Gx8Pj0+H0bHA5eDtw4KGdb2ISHdEQWkOuOUQo9TpsKyIGbXfIItYEL8VPcmVkzA1SUpqLUnlPFENRCs2solGJRZmoldOsGIHSxcFbxpc+jiJuTjPLzVJxH8sJKf0fhpb006uC9GvV3I4HhdjxINU2Vfp+zcLS44HZMLWqrOJyVXYxrGujqibGykCXkufUue/bPXGIPd7db/Xn/f029w73+zOB+26/7XZEu9MV7YMGNKkpmBkeL2DXc1953qtWi3WPWgds8AZgC+6VHyr7Pf0PsnHann4FAAA\u003d";
        try {
            NBTList nbtList = NBTReader.readBase64(bordel).getList("i");
            NBTCompound compound = nbtList.getCompound(0);
            NBTCompound compound1 = compound.getCompound("tag");
            NBTCompound compound2 = compound1.getCompound("ExtraAttributes");
            System.out.println(compound2.getString("id"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (true)
             return;
        List<Recipe> recipes = getRecipes("Livid Dagger");
        int count = 1;
        //System.out.println("Recipe size : " + recipes.size());
        for (int i = recipes.size()-1; i >= 0; i--) {
            Recipe recipe = recipes.get(i);
            //System.out.println("/////////////////////recipe " + count + "/////////////////////");
            //recipe.print();

            try {
                //System.out.println("Getting price...");
                recipe.calculate().getPriceCompletable().get();
                if (recipe.isSuccessful()) {
                    //System.out.println("Recipe is successful !");
                }
                else {
                    //System.out.println("Recipe failed");
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            //System.out.println("Done scanning");

            count++;
            //System.out.println("///////////////////recipe " + (count-1) + " - end/////////////////");
        }
        //System.out.println("Escaped loop");
        recipes = recipes.stream().sorted(Comparator.comparingInt(Recipe::getPrice)).toList();
        for (Recipe recipe : recipes) {
            //System.out.println("---------------------");
            if (!recipe.isSuccessful())
                //System.out.println("ATTENTION : un ou plusieurs items n'ont pas de prix attribué !");
            for (ItemStack item : recipe.getIngredients());
                //System.out.println(item.normalName + " x" + item.amount);
            //System.out.println("Prix : " + FormatUtil.format(recipe.getPrice()));
        }
        //System.out.println("Done loading !");
    }

    public static CompletableFuture<PriceType> getPrice(ItemStack item) {
        CompletableFuture<PriceType> get = new CompletableFuture<>();
        if (item.fromBazaar) {
            BazaarItem bazaarItem = new BazaarItem(item);
            bazaarItem.getDefaultPrice().whenComplete((bazaarPrices, throwable) -> {
                if (throwable != null) {
                    get.completeExceptionally(throwable);
                }
                get.complete(PriceType.BAZAAR.of(bazaarPrices.instaSell));
            });
        } else {
            double price = LowestFetcher.getPrice(item.locName);
            if (price < 0) {
                get.completeExceptionally(new Throwable("Item not found ! Does it exist ?"));
            }
            else get.complete(PriceType.AUCTION.of(price));
        }
        return get;
    }

    public static List<Recipe> getRecipes(ItemStack item) {
        System.out.println("Getting recipes of item " + item.normalName);
        List<Recipe> list = new LinkedList<>();
        Recipe currentRecipe = getRecipe(item);
        do {
            if (list.size() > 0) {
                if (currentRecipe.equals(list.get(list.size()-1)))
                    break;
            }
            currentRecipe.print();
            list.add(currentRecipe);
            currentRecipe = getExpandedRecipe(currentRecipe);
        }
        while (currentRecipe != null);
        return list;
    }

    public static List<Recipe> getRecipes(String itemName) {
        return getRecipes(new ItemStack(itemName));
    }

    @Nullable
    private static Recipe getExpandedRecipe(Recipe recipe) {
        List<ItemStack> ingredients = recipe.copyOfIngredients();
        for (int i = ingredients.size()-1; i > 0; i--) {
            ItemStack itemStack = ingredients.get(i);
            Recipe recipe1 = getRecipe(itemStack);
            if (!recipe1.isCraftable()) continue;
            ItemStack test = recipe1.getIngredients().get(0);
            if (test.normalName.equals("Block of " + itemStack.normalName))
                continue;
            ingredients.remove(itemStack);
            List<ItemStack> toAdd = new ArrayList<>();
            for (ItemStack item : recipe1.getIngredients())
                toAdd.add(new ItemStack(item.locName, item.amount*itemStack.amount));
            ingredients.addAll(toAdd);
        }
        if (recipe.getIngredients().equals(ingredients))
            return null;
        return new Recipe(recipe.getTarget().copy(), ingredients);
    }

    public static List<ItemStack> getIngredients(String itemName) {
        System.out.println("Getting ingredients for " + itemName);
        Recipe recipe = getRecipe(itemName);
        List<ItemStack> ingredients = recipe.getIngredients();
        if (ingredients.size() == 1 && ingredients.get(0).hasName(itemName))
            return ingredients;
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            ItemStack ing = ingredients.get(i);
            List<ItemStack> subIng = getIngredients(ing.locName);
            if (!subIng.contains(ing)) {
                ingredients.remove(ing);
                ingredients.addAll(subIng);
            }
        }
        List<ItemStack> fin = new LinkedList<>();
        loop1:
        for (ItemStack toAdd : ingredients) {
            for (ItemStack added : fin) {
                if (added.equals(toAdd)) {
                    added.setAmount(added.amount + toAdd.amount);
                    continue loop1;
                }
            }
            fin.add(toAdd);
        }
        for (ItemStack itemStack : fin) {
            System.out.println(itemStack.normalName + " : " + itemStack.amount);
        }
        return fin;
    }

    public static String normToLoc(String itemName) {
        for (String key : normToLoc.keySet()) {
            if (itemName.equalsIgnoreCase(key))
                return normToLoc.get(key);
            if (itemName.equalsIgnoreCase(normToLoc.get(key)))
                return normToLoc.get(key);
        }
        return null;
    }

    public static String locToNorm(String itemName) {
        for (String key : locToNorm.keySet()) {
            if (itemName.equalsIgnoreCase(key))
                return locToNorm.get(key);
            if (itemName.equalsIgnoreCase(locToNorm.get(key)))
                return locToNorm.get(key);
        }
        return null;
    }

    private static void fillLists() {
        try {
            JsonNode node = new ObjectMapper().readTree(getFile("bazaarItems.json"));
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                bazaarNames.add(entry.getKey());
            }
            JsonArray a = (JsonArray) JsonParser.parseReader(
                    new FileReader(getFile("reforges.json"))
            );
            for (int i = 0; i < a.size(); i++) {
                reforges.add(a.get(i).getAsString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readInmFile() throws IOException {
        URL url = new URL("https://raw.githubusercontent.com/kr45732/skyblock-plus-data/main/InternalNameMappings.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(url);
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode node1 = entry.getValue();
            String name = node1.get("name").textValue();
            normToLoc.put(name, entry.getKey());
            locToNorm.put(entry.getKey(), name);
            if (!bazaarNames.contains(entry.getKey()))
                nonBazaarItems.add(new ItemStack(name));
        }
        file = getFile("InternalNameMappings.json");
        if (!file.createNewFile()) {
            if (!file.exists()) {
                throw new RuntimeException("Couldn't create file " + file.getName());
            }
        }
        mapper.writeValue(file, node);
    }

    private static void readBitFile() throws IOException {
        URL url = new URL("https://raw.githubusercontent.com/kr45732/skyblock-plus-data/main/BitPricesJson.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(url);
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            int price = Integer.parseInt(entry.getValue().asText());
            bitPrices.put(entry.getKey(), price);
        }
    }

    public static Recipe getRecipe(String itemName) {
        return getRecipe(new ItemStack(itemName));
    }

    public static Recipe getRecipe(ItemStack target) {
        List<ItemStack> list = new LinkedList<>();
        try {
            JsonNode node = new ObjectMapper().readTree(file);
            JsonNode found = node.get(target.locName);
            if (found == null)
                return new Recipe(target);
            JsonNode recipe = found.get("recipe");
            if (recipe == null)
                return new Recipe(target);
            Iterator<Map.Entry<String, JsonNode>> fields = recipe.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String rawItemStack = entry.getValue().asText();
                if (rawItemStack.isBlank()) continue;
                String ingName = rawItemStack.substring(0, rawItemStack.indexOf(":"));
                if (ingName.equals("INK_SACK-4"))
                    return new Recipe(target);
                int ingAmount = Integer.parseInt(rawItemStack.replace(ingName + ":", ""));
                list.add(new ItemStack(ingName, ingAmount));
            }
            return new Recipe(target, list);
        } catch (IOException e) {
            e.printStackTrace();
            return new Recipe(target);
        }
    }

    private static void registerCommands() {
        jda.upsertCommand("bits", "bits").queue();
        OptionData data = new OptionData(OptionType.STRING, "item_name", "nom de l'item en anglais", true);
        CommandData cmdData = Commands.slash("lowest", "get lowest bin for item").addOptions(data);
        jda.upsertCommand(cmdData).queue();
        data = new OptionData(OptionType.STRING, "item_name", "nom de l'item en anglais", true);
        cmdData = Commands.slash("price", "get price for item").addOptions(data);
        jda.upsertCommand(cmdData).queue();
        data = new OptionData(OptionType.STRING, "item_name", "nom de l'item en anglais", true);
        cmdData = Commands.slash("craftprice", "get craft price for item").addOptions(data);
        jda.upsertCommand(cmdData).queue();
    }

    public static File getFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/main/resources/" + name);
    }
}
