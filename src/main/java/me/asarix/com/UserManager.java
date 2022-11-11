package me.asarix.com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserManager {
    public static boolean hasAccess(User user) {
        try {
            JsonNode node = new ObjectMapper().readTree(Main.getFile("access.json"));
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                long id = entry.getValue().asLong();
                if (user.getIdLong() == id)
                    return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addAccess(User user) {
        if (hasAccess(user)) return;
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = Main.getFile("access.json");
            ObjectNode oNode;
            JsonNode node = mapper.readTree(file);
            if (node instanceof ObjectNode n)
                oNode = n;
            else
                oNode = new ObjectMapper().createObjectNode();
            oNode.put(user.getName(), user.getIdLong());

            if (!file.createNewFile()) {
                if (!file.exists()) {
                    throw new RuntimeException("Couldn't create file " + file.getName());
                }
            }
            mapper.writeValue(file, node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasAdmin(User user) {
        try {
            JsonNode node = new ObjectMapper().readTree(Main.getFile("admin.json"));
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                long id = entry.getValue().asLong();
                if (user.getIdLong() == id)
                    return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addAdmin(User user) {
        if (hasAdmin(user)) return;
        addAccess(user);
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = Main.getFile("admin.json");
            ObjectNode oNode;
            JsonNode node = mapper.readTree(file);
            if (node instanceof ObjectNode n)
                oNode = n;
            else
                oNode = new ObjectMapper().createObjectNode();
            oNode.put(user.getName(), user.getIdLong());

            if (!file.createNewFile()) {
                if (!file.exists()) {
                    throw new RuntimeException("Couldn't create file " + file.getName());
                }
            }
            mapper.writeValue(file, node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateDiscordNames() throws IOException {
        uDNFile("access.json");
        uDNFile("admin.json");
    }

    private static void uDNFile(String fileName) throws IOException {
        File file = Main.getFile(fileName);
        JsonNode node = new ObjectMapper().readTree(file);
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        Map<String, Long> ids = new HashMap<>();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            long id = entry.getValue().asLong();
            String userName = Main.jda.retrieveUserById(id).complete().getName();
            ids.put(userName, id);
        }
        ObjectMapper mapper = new ObjectMapper();

        if (!file.createNewFile()) {
            if (!file.exists()) {
                throw new RuntimeException("Couldn't create file " + file.getName());
            }
        }
        mapper.writeValue(file, ids);
    }

    public static List<User> getAccess() {
        try {
            List<User> access = new ArrayList<>();
            JsonNode node = new ObjectMapper().readTree(Main.getFile("access.json"));
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                long id = entry.getValue().asLong();
                access.add(Main.jda.retrieveUserById(id).complete());
            }
            return access;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<User> getAdmins() {
        try {
            List<User> access = new ArrayList<>();
            JsonNode node = new ObjectMapper().readTree(Main.getFile("admin.json"));
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                long id = entry.getValue().asLong();
                access.add(Main.jda.retrieveUserById(id).complete());
            }
            return access;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static boolean hasPerm(User user, PermLevel permLevel) {
        if (permLevel == PermLevel.NONE)
            return true;
        if (permLevel == PermLevel.ACCESS)
            return hasAccess(user);
        return hasAdmin(user);
    }
}
