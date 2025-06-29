package net.shuyanmc.mpem.mpemfkyoucheat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public static File file = new File(MPEMFKYouCheat.MODID + ".json");
    public static ConfigObject config = new ConfigObject();

    private static List<String> list(String... items) {
        return new ArrayList<>(List.of(items));
    }

    public static boolean check() {
        if (file.exists()) {
            try {
                JsonParser.parseString(Files.readString(file.toPath()));
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public static void read() {
        if (!check()) throw new RuntimeException();
        try {
            config = new Gson().fromJson(new FileReader(file, StandardCharsets.UTF_8), ConfigObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write() {
        try {
            java.nio.file.Files.writeString(file.toPath(), new Gson().toJson(new ConfigObject()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        if (check()) {
            read();
        } else {
            write();
        }
    }

    public static class ConfigObject {
        public List<String> classes = list(
                "cn.ksmcbrigade",
                "net.shuyanmc.mpem"
        );
        public List<String> ids = list(
                "mpem",
                MPEMFKYouCheat.MODID
        );
        public List<String> keywords = list(
                "wurst",
                "meteor",
                "liquidbounce",
                "impact",
                "hack",
                "cheat",
                "freecam",
                "xray"
        );
    }
}
