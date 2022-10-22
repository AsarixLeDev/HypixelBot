package me.asarix.com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.asarix.com.commands.CommandHandler;
import me.asarix.com.weight.Skill;
import me.asarix.com.weight.SlayerLevel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    public static final String variablesPath = "C:\\Users\\Mathieu\\Desktop\\variables.json";
    public static String botToken;
    public static String apiKey;
    public static final Map<String, Integer> bitPrices = new HashMap<>();
    private static final Map<String, String> normToLoc = new HashMap<>();
    private static final Map<String, String> locToNorm = new HashMap<>();
    private static final List<ItemStack> nonBazaarItems = new LinkedList<>();
    public static List<String> reforges = new ArrayList<>();
    public static List<String> bazaarNames = new ArrayList<>();
    public static JDA jda;
    private static File file;

    private static final HashMap<String, Double> factors = new HashMap<>();
    public static final HashMap<String, Double> pnjItems = new HashMap<>();
    public static HypixelAPI API;

    public static void main(String[] args) {
        try {
            JsonNode node = new ObjectMapper().readTree(new File(variablesPath));
            botToken = node.get("BOT_TOKEN").asText();
            apiKey = node.get("API_KEY").asText();
            String key = System.getProperty("apiKey", apiKey);
            API = new HypixelAPI(new ApacheHttpClient(UUID.fromString(key)));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(10);
        }
        jda = JDABuilder.createDefault(botToken)
                .setActivity(Activity.playing("Hypixel :D"))
                .addEventListeners(new CommandHandler())
                .build();
        try {
            readBzFile();
            readInmFile();
            readBitFile();
            readPnjFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        fetchLoop();
        try {
            test("b7bb4e3732504848b68cd89615e27177", "8177cfe83a1f4ac886b28e19dff1c156");
            //test("13e807683a46435fa91aaa9cd0bd12ed", "1771eae87e5c4808982b1838aa03b401");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void test(String sbProfile, String playerUUID) throws InterruptedException, ExecutionException {
        factors.put("mining", 1.18207448);
        factors.put("foraging", 1.232826);
        factors.put("enchanting", 0.96976583);
        factors.put("farming", 1.217848139);
        factors.put("combat", 1.15797687265);
        factors.put("fishing", 1.406418);
        factors.put("alchemy", 1.0);
        factors.put("taming", 1.14744);

        factors.put("excess_mining", 259634.0);
        factors.put("excess_foraging", 259634.0);
        factors.put("excess_enchanting", 882758.0);
        factors.put("excess_farming", 220689.0);
        factors.put("excess_combat", 275862.0);
        factors.put("excess_fishing", 88274.0);
        factors.put("excess_alchemy", 1103448.0);
        factors.put("excess_taming", 441379.0);

        factors.put("zombie", 2208.0);
        factors.put("spider", 2118.0);
        factors.put("wolf", 1962.0);
        factors.put("enderman", 1430.0);

        factors.put("excess_zombie", 3643.2);
        factors.put("excess_spider", 3346.44);
        factors.put("excess_wolf", 2972.43);
        factors.put("excess_enderman", 2169.31);
        JsonObject profile = Main.API.getSkyBlockProfile(sbProfile).get().getProfile();
        JsonObject members = profile.get("members").getAsJsonObject();
        JsonObject pProfile = members.get(playerUUID).getAsJsonObject();
        Map<String, Double> skillWeights = new HashMap<>();
        Map<String, Double> slayerWeights = new HashMap<>();
        Map<String, Double> weights = new HashMap<>();
        for (String key : pProfile.keySet()) {
            JsonElement element = pProfile.get(key);
            if (key.startsWith("experience_skill_")) {
                String skillName = key.replace("experience_skill_", "");
                if (List.of("carpentry", "runecrafting", "social2").contains(skillName)) continue;
                System.out.println(skillName);
                double weight = skillWeight(skillName, element.getAsInt());
                weights.put(skillName, weight);
                skillWeights.put(skillName, weight);
            }
            if (key.equals("slayer_bosses")) {
                JsonObject value = element.getAsJsonObject();
                for (String slayerName : value.keySet()) {
                    JsonElement slayerData = value.get(slayerName);
                    if (slayerData == null) continue;
                    JsonElement xpData = slayerData.getAsJsonObject().get("xp");
                    if (xpData == null) continue;
                    int xp = xpData.getAsInt();
                    double weight = slayerWeight(slayerName, xp);
                    weights.put(slayerName, weight);
                    slayerWeights.put(slayerName, weight);
                }
            }
        }
        List<String> keys = new ArrayList<>(weights.keySet());
        keys = keys.stream().sorted(Comparator.comparingDouble(weights::get).reversed()).toList();
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        for (String key : keys)
            map.put(key, weights.get(key));

        double total = 0;
        for (double value : map.values())
            total += value;
        StringBuilder builder = new StringBuilder("**__Asarix__**\n\n" +
                "Weight totale : " + FormatUtil.round(total, 2) + "\n\n");
        for (String key : map.keySet())
            builder.append("**").append(key).append("** : ")
                    .append(FormatUtil.round(weights.get(key), 2))
                    .append("\n");

        double skillTotal = 0;
        for (double value : skillWeights.values())
            skillTotal += value;
        builder.append("**Total skills** : ")
                .append(FormatUtil.round(skillTotal, 2))
                .append("\n");
        double slayerTotal = 0;
        for (double value : slayerWeights.values())
            slayerTotal += value;
        builder.append("**Total slayers** : ")
                .append(FormatUtil.round(slayerTotal, 2));
        System.out.println(builder);
    }

    private static double skillWeight(String skillName, int xp) {
        if (!factors.containsKey(skillName))
            return 0;
        Skill skill = new Skill(xp, skillName);
        double level = skill.getLevel();
        double factor = factors.get(skillName);
        double levelWeight = Math.pow(level * 10,0.5 + factor + level / 100) / 1250;
        double excess = 0;
        if (skill.isMaxed()) {
            levelWeight = Math.round(levelWeight);
            excess = xp - skill.getMaxXp();
        }
        System.out.println("---------------- excess " + skillName + " -----------------");
        System.out.println(excess);
        System.out.println("---------------- excess " + skillName + " -----------------");
        double excessFactor = factors.get("excess_" + skillName);
        double excessWeight = Math.pow(excess/excessFactor, 0.968);
        return levelWeight + excessWeight;
    }

    private static double slayerWeight(String slayerName, int xp) {
        if (!factors.containsKey(slayerName))
            return 0;
        SlayerLevel skill = new SlayerLevel(slayerName, xp);
        double factor = factors.get(slayerName);
        double levelWeight = xp / factor;
        double excess = skill.getExcess();
        double excessFactor = factors.get("excess_" + slayerName);
        double excessWeight = Math.pow(excess/excessFactor, 0.968);
        return levelWeight + excessWeight;
    }

    private static void fetchLoop() {
        Timer timer = new Timer();
        TimerTask lowestFetcher = new LowestFetcher(nonBazaarItems);
        timer.schedule(lowestFetcher, 0, 60 * 1000);
    }

    public static List<Recipe> getRecipes(ItemStack item) {
        System.out.println("Getting recipes of item " + item.normalName);
        List<Recipe> list = new LinkedList<>();
        Recipe currentRecipe = getRecipe(item);
        do {
            if (list.size() > 0) {
                if (currentRecipe.equals(list.get(list.size() - 1)))
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

    private static void readBzFile() throws IOException {
        JsonNode node = new ObjectMapper().readTree(getFile("bazaarItems.json"));
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            bazaarNames.add(entry.getKey());
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

    private static void readPnjFile() throws IOException {
        JsonNode node = new ObjectMapper().readTree(getFile("pnj_prices.json"));
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            double price = entry.getValue().asDouble();
            pnjItems.put(entry.getKey(), price);
            System.out.println("PNJ : " + entry.getKey() + " " + price);
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
}
