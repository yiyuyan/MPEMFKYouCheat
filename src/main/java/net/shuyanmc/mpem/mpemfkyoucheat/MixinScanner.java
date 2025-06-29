package net.shuyanmc.mpem.mpemfkyoucheat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MixinScanner {
    public static List<String> getMixinInfo(JarFile jar) throws IOException {
        List<String> mixinClasses = new ArrayList<>();
        Enumeration<JarEntry> entryEnumeration = jar.entries();
        while (entryEnumeration.hasMoreElements()) {
            JarEntry entry = entryEnumeration.nextElement();
            if (entry.getName().endsWith("mixin.json")) {
                JsonObject mixinConfig = JsonParser.parseString(IOUtils.toString(jar.getInputStream(entry))).getAsJsonObject();
                String packageMixin = mixinConfig.get("package").getAsString();
                JsonArray mixin = mixinConfig.getAsJsonArray("mixins");
                JsonArray client = mixinConfig.getAsJsonArray("client");
                mixinClasses.addAll(mixin.asList().stream().map(JsonElement::getAsString).toList());
                mixinClasses.addAll(client.asList().stream().map(JsonElement::getAsString).toList());
                mixinClasses.add(packageMixin);
            }
        }
        return mixinClasses;
    }
}
