package me.asarix.com;

import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import net.hypixel.api.reply.AbstractReply;

import java.util.UUID;
import java.util.function.BiConsumer;

public class ExampleUtil {

    public static final HypixelAPI API;

    static {
        String key = System.getProperty("apiKey", "083e19d0-8cbd-40ba-aeea-bbe16ed7b959"); // arbitrary key, replace with your own to test or use the property
        API = new HypixelAPI(new ApacheHttpClient(UUID.fromString(key)));
    }

    public static <T extends AbstractReply> BiConsumer<T, Throwable> getTestConsumer() {
        return (result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                System.exit(0);
                return;
            }

            System.out.println(result);

            System.exit(0);
        };
    }
}
