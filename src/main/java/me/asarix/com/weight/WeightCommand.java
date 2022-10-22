package me.asarix.com.weight;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.asarix.com.FormatUtil;
import me.asarix.com.Main;
import me.asarix.com.commands.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfilesReply;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class WeightCommand extends Command {

    @Override
    public String run(@NotNull SlashCommandInteractionEvent event) throws InterruptedException, ExecutionException {
        OptionMapping optionMapping = event.getOption("player");
        if (optionMapping == null) {
            return "Veuillez spécifier un joueur !";
        }
        String playerName = optionMapping.getAsString();
        optionMapping = event.getOption("profile");
        String profileName = optionMapping == null ? null : optionMapping.getAsString();
        UUID uuid = Main.getUUID(playerName);
        if (uuid == null) {
            return playerName + " : Ce joueur n'a pas pu être trouvé !";
        }
        PlayerReply.Player player = Main.API.getPlayerByUuid(uuid).get().getPlayer();
        SkyBlockProfilesReply reply = Main.API.getSkyBlockProfiles(uuid).get();
        JsonArray array = reply.getProfiles();
        JsonObject latestProfile = null;
        long latest_save = 0;
        for (int i = 0; i < array.size(); i++) {
            JsonObject profile = array.get(i).getAsJsonObject();
            if (profileName != null) {
                String name = profile.get("cute_name").getAsString();
                if (name.equalsIgnoreCase(profileName)) {
                    analyseWeight(player, profile, event);
                    return null;
                }
            }
            if (!profile.has("last_save")) continue;
            long lastSave = profile.get("last_save").getAsLong();
            if (lastSave > latest_save) {
                latestProfile = profile;
                latest_save = lastSave;
            }
        }
        if (profileName != null)
            return "Ce profile n'existe pas !";
        if (latestProfile == null)
            return "Ce joueur n'a pas de profile SkyBlock !";
        analyseWeight(player, latestProfile, event);
        return null;
    }

    private void analyseWeight(PlayerReply.Player player, JsonObject profile, @NotNull SlashCommandInteractionEvent event) {
        JsonObject members = profile.get("members").getAsJsonObject();
        String fUuid = player.getUuid().toString().replace("-", "");
        JsonObject pProfile = members.get(fUuid).getAsJsonObject();
        Map<String, Double> skillWeights = new HashMap<>();
        Map<String, Double> slayerWeights = new HashMap<>();
        Map<String, Double> dungeonWeights = new HashMap<>();
        Map<String, Double> weights = new HashMap<>();
        for (String key : pProfile.keySet()) {
            JsonElement element = pProfile.get(key);
            if (key.startsWith("experience_skill_")) {
                String skillName = key.replace("experience_skill_", "");
                if (List.of("carpentry", "runecrafting", "social2").contains(skillName)) continue;
                double weight = skillWeight(skillName, element.getAsInt());
                weights.put(skillName, weight);
                skillWeights.put(skillName, weight);
            } else if (key.equals("slayer_bosses")) {
                JsonObject value = element.getAsJsonObject();
                for (String slayerName : value.keySet()) {
                    JsonElement slayerData = value.get(slayerName);
                    if (slayerData == null) continue;
                    JsonElement xpData = slayerData.getAsJsonObject().get("xp");
                    if (xpData == null) continue;
                    int xp = xpData.getAsInt();
                    double weight = Slayer.valueOf(slayerName.toUpperCase()).calculate(xp);
                    weights.put(slayerName, weight);
                    slayerWeights.put(slayerName, weight);
                }
            } else if (key.equals("dungeons")) {
                JsonObject value = element.getAsJsonObject();
                for (String dungKey : value.keySet()) {
                    if (dungKey.equals("player_classes")) {
                        JsonObject classes = value.getAsJsonObject(dungKey);
                        for (Map.Entry<String, JsonElement> entry : classes.entrySet()) {
                            String name = entry.getKey();
                            JsonElement expObj = entry.getValue().getAsJsonObject().get("experience");
                            if (expObj == null)
                                 continue;
                            double xp = expObj.getAsDouble();
                            double weight = DungeonType.valueOf(name.toUpperCase()).calculate(xp);
                            weights.put(name, weight);
                            dungeonWeights.put(name, weight);
                        }
                    } else if (dungKey.equals("dungeon_types")) {
                        JsonObject types = value.getAsJsonObject("dungeon_types");
                        JsonObject cata = types.getAsJsonObject("catacombs");
                        double xp = cata.get("experience").getAsDouble();
                        double weight = DungeonType.CATACOMBS.calculate(xp);
                        weights.put("catacombs", weight);
                        dungeonWeights.put("catacombs", weight);
                    }
                }
            }
        }
        Map<String, Double> cleanSkillWeights = new HashMap<>();
        Map<String, Double> cleanSlayerWeights = new HashMap<>();
        Map<String, Double> cleanDungeonWeights = new HashMap<>();
        Map<String, Double> cleanWeights = new HashMap<>();
        for (String key : skillWeights.keySet()) {
            Double aDouble = skillWeights.get(key);
            if (aDouble.isNaN() || aDouble == 0) continue;
            cleanSkillWeights.put(key, aDouble);
            cleanWeights.put(key, aDouble);
        }
        for (String key : slayerWeights.keySet()) {
            Double aDouble = slayerWeights.get(key);
            if (aDouble.isNaN() || aDouble == 0) continue;
            cleanSlayerWeights.put(key, aDouble);
            cleanWeights.put(key, aDouble);
        }
        for (String key : dungeonWeights.keySet()) {
            Double aDouble = dungeonWeights.get(key);
            if (aDouble.isNaN() || aDouble == 0) continue;
            cleanDungeonWeights.put(key, aDouble);
            cleanWeights.put(key, aDouble);
        }
        List<String> keys = new ArrayList<>(cleanWeights.keySet());
        keys = keys.stream().sorted(Comparator.comparingDouble(cleanWeights::get).reversed()).toList();
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        for (String key : keys)
            map.put(key, cleanWeights.get(key));

        double total = 0;
        for (Double value : map.values())
            total += value;


        System.out.println(total);

        StringBuilder builder = new StringBuilder("**__" + player.getName() + "__**\n\n" +
                "Weight totale : " + FormatUtil.round(total, 2) + "\n\n");
        for (String key : map.keySet())
            builder.append("**").append(key).append("** : ")
                    .append(FormatUtil.round(cleanWeights.get(key), 2))
                    .append("\n");

        double skillTotal = 0;
        for (double value : cleanSkillWeights.values())
            skillTotal += value;
        builder.append("\n**Total skills** : ")
                .append(FormatUtil.round(skillTotal, 2))
                .append("\n");
        double slayerTotal = 0;
        for (double value : cleanSlayerWeights.values())
            slayerTotal += value;
        builder.append("**Total slayers** : ")
                .append(FormatUtil.round(slayerTotal, 2))
                .append("\n");
        double djTotal = 0;
        for (double value : cleanDungeonWeights.values())
            djTotal += value;
        builder.append("**Total Dungeons** : ")
                .append(FormatUtil.round(djTotal, 2))
                .append("\n");
        event.getHook().sendMessage(builder.toString()).queue();
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.STRING, "player", "Nom ou UUID du joueur", true);
        OptionData data1 = new OptionData(OptionType.STRING, "profile", "Nom du profile", false);
        return Commands.slash("weight", "Weight infos").addOptions(data, data1);
    }

    private double skillWeight(String skillName, int xp) {
        CalculatedSkill skill = Skill.valueOf(skillName.toUpperCase()).calculate(xp);
        return skill.totalWeight;
    }
}
