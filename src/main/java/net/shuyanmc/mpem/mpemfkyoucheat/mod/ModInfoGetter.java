package net.shuyanmc.mpem.mpemfkyoucheat.mod;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModInfoGetter {
    public static List<ModInfo> getModInfo(ZipFile file) throws IOException {
        List<ModInfo> modInfo = new ArrayList<>();
        byte[] infoFileBytes;
        ZipEntry modInfoEntry = file.getEntry("META-INF/mods.toml");
        ModLoader loader = ModLoader.Forge;
        if (modInfoEntry == null) {
            modInfoEntry = file.getEntry("META-INF/neoforge.mods.toml");
            if (modInfoEntry == null) {
                modInfoEntry = file.getEntry("fabric.mod.json");
                loader = ModLoader.Fabric;
            } else {
                loader = ModLoader.NeoForge;
            }
        }
        infoFileBytes = file.getInputStream(modInfoEntry).readAllBytes();
        String infoData = new String(infoFileBytes, StandardCharsets.UTF_8);
        File tempFile = new File("tmp_" + RandomStringUtils.randomNumeric(8) + ".toml");
        Files.writeString(tempFile.toPath(), infoData);
        switch (loader) {
            case Forge -> modInfo.addAll(parseForgeLikeModInfo(tempFile, false));
            case NeoForge -> modInfo.addAll(parseForgeLikeModInfo(tempFile, true));
            case Fabric -> modInfo.addAll(parseFabricModInfo(infoData));
        }
        tempFile.delete();
        return modInfo;
    }

    public static List<ModInfo> parseFabricModInfo(String info) {
        List<ModInfo> infos = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject modInfo = gson.fromJson(new StringReader(info), JsonObject.class);
        if (modInfo != null && modInfo.has("id")) {
            String modId = modInfo.get("id").getAsString();
            String description = modInfo.has("description") ? modInfo.get("description").getAsString() : "";
            String displayName = modInfo.has("name") ? modInfo.get("name").getAsString() : "";
            String version = modInfo.has("version") ? modInfo.get("version").getAsString() : "0.0";
            infos.add(new ModInfo(
                    version,
                    ModLoader.Fabric,
                    modId,
                    description,
                    displayName
            ));
        }
        return infos;
    }

    public static List<ModInfo> parseForgeLikeModInfo(File info, boolean isNeo) {
        List<ModInfo> infos = new ArrayList<>();
        FileConfig fileConfig = FileConfig.builder(info).build();
        List<? extends Config> modsList = fileConfig.get("mods");
        if (modsList != null && !modsList.isEmpty()) {
            Config mod = modsList.get(0);
            String modId = mod.get("modId");
            String description = mod.get("description");
            String displayName = mod.get("displayName");
            String version = mod.get("version");
            infos.add(new ModInfo(
                    version,
                    isNeo ? ModLoader.NeoForge : ModLoader.Forge,
                    modId,
                    description,
                    displayName
            ));
        }
        return infos;
    }
}
