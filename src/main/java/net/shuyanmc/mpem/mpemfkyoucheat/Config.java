package net.shuyanmc.mpem.mpemfkyoucheat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Config {
    public static File file = new File(MPEMFKYouCheat.MODID+".json");

    public static JsonObject white = new JsonObject();

    public static void init() throws IOException {
        if(!file.exists()){
            JsonObject object = new JsonObject();
            JsonArray ids = new JsonArray();
            JsonArray classes = new JsonArray();
            JsonArray keywords = new JsonArray();
            classes.add("cn.ksmcbrigade");
            classes.add("net.shuyanmc.mpem");
            ids.add("mpem");
            ids.add(MPEMFKYouCheat.MODID);
            keywords.add("wurst");
            keywords.add("meteor");
            keywords.add("liquidbounce");
            keywords.add("impact");
            keywords.add("hack");
            keywords.add("cheat");
            keywords.add("freecam");
            keywords.add("xray");
            object.add("ids",ids);
            object.add("classes",classes);
            object.add("black_keywords",keywords);
            FileUtils.writeStringToFile(file,object.toString());
        }
        white = JsonParser.parseString(FileUtils.readFileToString(file)).getAsJsonObject();
    }
}
