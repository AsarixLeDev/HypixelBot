package me.asarix.com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import me.asarix.com.commands.CommandHandler;
import me.asarix.com.prices.BazaarFetcher;
import me.asarix.com.prices.LowestFetcher;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
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
    public static final Map<String, Integer> bitPrices = new HashMap<>();
    public static final HashMap<String, Double> pnjItems = new HashMap<>();
    private static final Map<String, String> normToLoc = new HashMap<>();
    private static final Map<String, String> locToNorm = new HashMap<>();
    public static String botToken;
    public static String apiKey;
    public static JDA jda;
    public static HypixelAPI API;
    public static Timer lowestTimer;
    public static Timer bazaarTimer;
    private static File file;

    public static void main(String[] args) {
//        System.out.println(getVersion());
        File toCreate = new File(System.getProperty("user.dir") + "/resources");
        toCreate.mkdirs();
        JsonNode node;
        try {
            node = new ObjectMapper().readTree(getFile("variables.json"));
        } catch (IOException e) {
            System.err.println("Couldn't read variables file");
            createVariableFile();
            shutDown();
            return;
        }
        try {
            botToken = node.get("BOT_TOKEN").asText();
            if (botToken.isBlank()) throw new NullPointerException();
        } catch (NullPointerException e) {
            System.err.println("Please specify the bot token in the variable file !");
            createVariableFile();
            shutDown();
            return;
        }
        try {
            apiKey = node.get("API_KEY").asText();
            if (apiKey.isBlank()) throw new NullPointerException();
        } catch (NullPointerException e) {
            System.err.println("Please specify the api key in the variable file !");
            createVariableFile();
            shutDown();
            return;
        }
        String key = System.getProperty("apiKey", apiKey);
        API = new HypixelAPI(new ApacheHttpClient(UUID.fromString(key)));

        jda = JDABuilder.createDefault(botToken)
                .setActivity(Activity.playing("Hypixel :D"))
                .addEventListeners(new CommandHandler())
                .build();
        User asarix = jda.retrieveUserById("441284809856122891").complete();
        UserManager.addAdmin(asarix);
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

    private static void createVariableFile() {
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.set("API_KEY", new TextNode(""));
        node.set("BOT_TOKEN", new TextNode(""));
        try {
            new ObjectMapper().writeValue(getFile("variables.json"), node);
        } catch (IOException ex) {
            System.err.println("Failed to write to variable file");
            shutDown();
            throw new RuntimeException(ex);

        }
    }

    private static int getVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    public static void shutDown() {
        if (jda != null)
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
        File file = new File(System.getProperty("user.dir") + "/resources/" + name);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to create file : " + name);
            shutDown();
            throw new RuntimeException(e);
        }
        return file;
    }

    public static UUID getUUID(String playerName) {
        try {
            ObjectNode oNode;
            JsonNode node = new ObjectMapper().readTree(file);
            if (node instanceof ObjectNode n)
                oNode = n;
            else
                oNode = new ObjectMapper().createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = oNode.fields();
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
                oNode.set(player.getName(), new TextNode(player.getUuid().toString()));
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
