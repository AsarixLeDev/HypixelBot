package me.asarix.com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.asarix.com.commands.CommandHandler;
import me.asarix.com.prices.BazaarFetcher;
import me.asarix.com.prices.LowestFetcher;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import net.hypixel.api.reply.PlayerReply;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static final String variablesPath = "C:\\Users\\Mathieu\\Desktop\\variables.json";
    public static final Map<String, Integer> bitPrices = new HashMap<>();
    public static final HashMap<String, Double> pnjItems = new HashMap<>();
    private static final Map<String, String> normToLoc = new HashMap<>();
    private static final Map<String, String> locToNorm = new HashMap<>();
    public static String botToken;
    public static String apiKey;
    public static List<String> reforges = new ArrayList<>();
    public static JDA jda;
    public static HypixelAPI API;
    public static Timer lowestTimer;
    public static Timer bazaarTimer;
    private static File file;

    public static void main(String[] args) {
        try {
            JsonNode node = new ObjectMapper().readTree(new File(variablesPath));
            botToken = node.get("BOT_TOKEN").asText();
            apiKey = node.get("API_KEY").asText();
            String key = System.getProperty("apiKey", apiKey);
            API = new HypixelAPI(new ApacheHttpClient(UUID.fromString(key)));
        } catch (Exception e) {
            e.printStackTrace();
            shutDown();
        }
        jda = JDABuilder.createDefault(botToken)
                .setActivity(Activity.playing("Hypixel :D"))
                .addEventListeners(new CommandHandler())
                .build();
        try {
            readInmFile();
            readBitFile();
            readPnjFile();
            UserManager.updateDiscordNames();
        } catch (IOException e) {
            e.printStackTrace();
            shutDown();
        }
        fetchLoop();
    }

    public static void shutDown() {
        jda.shutdownNow();
        System.exit(10);
    }

    private static void fetchLoop() {
        lowestTimer = new Timer();
        TimerTask lowestFetcher = new LowestFetcher();
        lowestTimer.schedule(lowestFetcher, 0, 60 * 1000);

        bazaarTimer = new Timer();
        TimerTask bazaarFetcher = new BazaarFetcher();
        bazaarTimer.schedule(bazaarFetcher, 0, 60 * 1000);
    }

    public static List<Recipe> getRecipes(ItemStack item) {
        List<Recipe> list = new LinkedList<>();
        Recipe currentRecipe = getRecipe(item);
        do {
            if (list.size() > 0) {
                if (currentRecipe.equals(list.get(list.size() - 1)))
                    break;
            }
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
        for (int i = ingredients.size() - 1; i > 0; i--) {
            ItemStack itemStack = ingredients.get(i);
            Recipe recipe1 = getRecipe(itemStack);
            if (!recipe1.isCraftable()) continue;
            ItemStack test = recipe1.getIngredients().get(0);
            if (test.normalName.equals("Block of " + itemStack.normalName))
                continue;
            ingredients.remove(itemStack);
            List<ItemStack> toAdd = new ArrayList<>();
            for (ItemStack item : recipe1.getIngredients())
                toAdd.add(new ItemStack(item.locName, item.amount * itemStack.amount));
            ingredients.addAll(toAdd);
        }
        if (recipe.getIngredients().equals(ingredients))
            return null;
        return new Recipe(recipe.getTarget().copy(), ingredients);
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

    private static void readPnjFile() throws IOException {
        JsonNode node = new ObjectMapper().readTree(getFile("pnj_prices.json"));
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            double price = entry.getValue().asDouble();
            pnjItems.put(entry.getKey(), price);
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

    public static File getFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/main/resources/" + name);
    }

    public static UUID getUUID(String playerName) {
        try {
            JsonNode node = new ObjectMapper().readTree(getFile("playerIdTranslates.json"));
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (!entry.getKey().equalsIgnoreCase(playerName)) continue;
                return UUID.fromString(entry.getValue().asText());
            }
            CompletableFuture<UUID> get = new CompletableFuture<>();
            API.getPlayerByName(playerName).whenComplete(((playerReply, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                    get.complete(null);
                    return;
                }
                PlayerReply.Player player = playerReply.getPlayer();
                if (player == null) {
                    get.complete(null);
                    return;
                }
                get.complete(player.getUuid());
                ((ObjectNode) node).set(player.getName(), JsonNodeFactory.instance.textNode(player.getUuid().toString()));
                ObjectMapper mapper = new ObjectMapper();
                try {
                    mapper.writeValue(getFile("playerIdTranslates.json"), node);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            return get.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
