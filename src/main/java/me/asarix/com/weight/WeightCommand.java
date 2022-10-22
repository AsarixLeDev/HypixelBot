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

    Map<String, Double> factors = new HashMap<>();

    public WeightCommand() {
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
    }

    @Override
    public String run(@NotNull SlashCommandInteractionEvent event) throws InterruptedException, ExecutionException {
        OptionMapping optionMapping = event.getOption("player");
        if (optionMapping == null) {
            return "Veuillez spécifier un joueur !";
        }
        String playerName = optionMapping.getAsString();
        optionMapping = event.getOption("profile");
        String profileName = optionMapping == null ? null : optionMapping.getAsString();
        PlayerReply.Player player = Main.API.getPlayerByName(playerName).get().getPlayer();
        SkyBlockProfilesReply reply = Main.API.getSkyBlockProfiles(player.getUuid()).get();
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
        StringBuilder builder = new StringBuilder("**__" + player.getName() + "__**\n\n" +
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
        event.getHook().sendMessage(builder.toString()).queue();
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.STRING, "player", "Nom ou UUID du joueur", true);
        OptionData data1 = new OptionData(OptionType.STRING, "profile", "Nom du profile", false);
        return Commands.slash("weight", "Weight infos").addOptions(data, data1);
    }

    private double skillWeight(String skillName, int xp) {
        if (!factors.containsKey(skillName))
             return 0;
        Skill skill = new Skill(xp, skillName);
        double level = skill.getLevel();
        double factor = factors.get(skillName);
        double levelWeight = Math.pow(level * 10,0.5 + factor + level / 100) / 1250;
        if (List.of("foraging", "fishing", "alchemy", "taming").contains(skillName))
            levelWeight = Math.round(levelWeight);
        double excess = skill.getExcess();
        double excessFactor = factors.get("excess_" + skillName);
        double excessWeight = Math.pow(excess/excessFactor, 0.968);
        return levelWeight + excessWeight;
    }

    private double slayerWeight(String slayerName, int xp) {
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
}
