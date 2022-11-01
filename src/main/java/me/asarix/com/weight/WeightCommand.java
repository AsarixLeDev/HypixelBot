package me.asarix.com.weight;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.asarix.com.FormatUtil;
import me.asarix.com.Main;
import me.asarix.com.PermLevel;
import me.asarix.com.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
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
        Map<String, Weight> skillWeights = new HashMap<>();
        Map<String, Weight> slayerWeights = new HashMap<>();
        Map<String, Weight> dungeonWeights = new HashMap<>();
        for (String key : pProfile.keySet()) {
            JsonElement element = pProfile.get(key);
            if (key.startsWith("experience_skill_")) {
                String skillName = key.replace("experience_skill_", "");
                if (List.of("carpentry", "runecrafting", "social2").contains(skillName)) continue;
                Weight weight = skillWeight(skillName, element.getAsInt());
                skillWeights.put(skillName, weight);
            } else if (key.equals("slayer_bosses")) {
                JsonObject value = element.getAsJsonObject();
                for (String slayerName : value.keySet()) {
                    if (slayerName.equalsIgnoreCase("blaze"))
                        continue;
                    JsonElement slayerData = value.get(slayerName);
                    if (slayerData == null) continue;
                    JsonElement xpData = slayerData.getAsJsonObject().get("xp");
                    if (xpData == null) continue;
                    int xp = xpData.getAsInt();
                    Weight weight = Slayer.valueOf(slayerName.toUpperCase()).calculate(xp);
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
                            Weight weight = DungeonType.valueOf(name.toUpperCase()).calculate(xp);
                            dungeonWeights.put(name, weight);
                        }
                    } else if (dungKey.equals("dungeon_types")) {
                        JsonObject types = value.getAsJsonObject("dungeon_types");
                        JsonObject cata = types.getAsJsonObject("catacombs");
                        double xp = cata.get("experience").getAsDouble();
                        Weight weight = DungeonType.CATACOMBS.calculate(xp);
                        dungeonWeights.put("catacombs", weight);
                    }
                }
            }
        }
        List<String> sortedSkill = new LinkedList<>();
        List<String> sortedSlayer = new LinkedList<>();
        List<String> sortedDungeon = new LinkedList<>();
        double total = 0;
        double totalBase = 0;
        for (String key : skillWeights.keySet()) {
            Weight weight = skillWeights.get(key);
            double aDouble = weight.total();
            if (aDouble == 0) continue;
            sortedSkill.add(key);
            total += aDouble;
            totalBase += weight.base();
        }
        for (String key : slayerWeights.keySet()) {
            Weight weight = slayerWeights.get(key);
            double aDouble = weight.total();
            if (aDouble == 0) continue;
            sortedSlayer.add(key);
            total += aDouble;
            totalBase += weight.base();
        }
        for (String key : dungeonWeights.keySet()) {
            Weight weight = dungeonWeights.get(key);
            double aDouble = weight.total();
            if (aDouble == 0) continue;
            sortedDungeon.add(key);
            total += aDouble;
            totalBase += weight.base();
        }


        sortedSkill = sortedSkill.stream().sorted(Comparator.comparingDouble(
                key -> skillWeights.get(key).total()).reversed()).toList();
        sortedSlayer = sortedSlayer.stream().sorted(Comparator.comparingDouble(
                key -> slayerWeights.get(key).total()).reversed()).toList();
        sortedDungeon = sortedDungeon.stream().sorted(Comparator.comparingDouble(
                key -> dungeonWeights.get(key).total()).reversed()).toList();


        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail("https://crafatar.com/avatars/" + player.getUuid().toString());
        builder.setTitle(player.getName(), "https://sky.shiiyu.moe/stats/" + player.getName());
        builder.addField("Weight totale", String.valueOf(FormatUtil.round(total, 2)), true);
        builder.addField("Weight sans excédent", String.valueOf(FormatUtil.round(totalBase, 2)), true);

        StringBuilder skillsValue = new StringBuilder();
        double skillTotal = 0;
        double skillBase = 0;
        for (String key : sortedSkill) {
            Weight val = skillWeights.get(key);
            skillTotal += val.total();
            skillBase += val.base();
            skillsValue.append(FormatUtil.firstCap(key))
                    .append(" : **")
                    .append(FormatUtil.round(val.total(), 2))
                    .append("**")
                    .append(" (")
                    .append(FormatUtil.round(val.base(), 2))
                    .append(")").append("\n");
        }
        String title = "Skills : " + FormatUtil.round(skillTotal, 2);
        title += " (" + FormatUtil.round(skillBase, 2) + ")";
        builder.addField(title, skillsValue.append("\n").toString(), false);

        StringBuilder slayerValue = new StringBuilder();
        double slayerTotal = 0;
        double slayerBase = 0;
        for (String key : sortedSlayer) {
            Weight val = slayerWeights.get(key);
            slayerTotal += val.total();
            slayerBase += val.base();
            slayerValue.append(FormatUtil.firstCap(key))
                    .append(" : **")
                    .append(FormatUtil.round(val.total(), 2))
                    .append("**")
                    .append(" (")
                    .append(FormatUtil.round(val.base(), 2))
                    .append(")").append("\n");
        }
        title = "Slayers : " + FormatUtil.round(slayerTotal, 2);
        title += " (" + FormatUtil.round(slayerBase, 2) + ")";
        builder.addField(title, slayerValue.append("\n").toString(), false);

        StringBuilder dungeonValue = new StringBuilder();
        double dungeonTotal = 0;
        double dungeonBase = 0;
        for (String key : sortedDungeon) {
            Weight val = dungeonWeights.get(key);
            dungeonTotal += val.total();
            dungeonBase += val.base();
            dungeonValue.append(FormatUtil.firstCap(key))
                    .append(" : **")
                    .append(FormatUtil.round(val.total(), 2))
                    .append("**")
                    .append(" (")
                    .append(FormatUtil.round(val.base(), 2))
                    .append(")").append("\n");
        }
        title = "Donjons : " + FormatUtil.round(dungeonTotal, 2);
        title += " (" + FormatUtil.round(dungeonBase, 2) + ")";
        builder.addField(title, dungeonValue.append("\n").toString(), false);

        builder.setAuthor("Asarix");
        builder.setFooter("By Asarix#1234");
        List<String> urls = List.of("https://www.musicmundial.com/en/wp-content/uploads/2022/01/Mia-Khalifa-died.-Find-out-the-truth-of-what-happened-to-the-actress.jpg",
                "https://www.starmag.com/wp-content/uploads/2018/06/miakhalifa-326c25fd0ed4921505665ba723ce5804-1200x600.jpg");
        Random rand = new Random();
        String randomElement = urls.get(rand.nextInt(urls.size()));
        builder.setImage(randomElement);
        event.getHook().sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.STRING, "player", "Nom ou UUID du joueur", true);
        OptionData data1 = new OptionData(OptionType.STRING, "profile", "Nom du profile", false);
        return Commands.slash("weight", "Weight infos").addOptions(data, data1);
    }

    private Weight skillWeight(String skillName, int xp) {
        CalculatedSkill skill = Skill.valueOf(skillName.toUpperCase()).calculate(xp);
        return skill.totalWeight;
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ACCESS;
    }
}
