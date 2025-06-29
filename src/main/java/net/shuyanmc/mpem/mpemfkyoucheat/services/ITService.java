package net.shuyanmc.mpem.mpemfkyoucheat.services;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import net.shuyanmc.mpem.mpemfkyoucheat.MPEMFKYouCheat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ITService implements ITransformationService {

    private boolean loaded = false;

    @Override
    public @NotNull String name() {
        return "MPEMFKYouCheat";
    }

    @Override
    public void initialize(IEnvironment iEnvironment) {
        this.onLoad(iEnvironment,Set.of());
    }

    @Override
    public void onLoad(IEnvironment iEnvironment, Set<String> set) {
        if(loaded) return;
        loaded = true;
        try {
            net.shuyanmc.mpem.mpemfkyoucheat.Config.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File mods = new File("mods");
        File[] files = mods.listFiles();
        ArrayList<String> ids = new ArrayList<>(),classes = new ArrayList<>(),blackKeyWords = new ArrayList<>(),
                shitIds = new ArrayList<>(),
                shitDesc = new ArrayList<>(),shitNames = new ArrayList<>(),
                shitMixins = new ArrayList<>(),shitClasses = new ArrayList<>();

        for (JsonElement string : net.shuyanmc.mpem.mpemfkyoucheat.Config.white.get("ids").getAsJsonArray()) {
            ids.add(string.getAsString());
        }
        for (JsonElement string : net.shuyanmc.mpem.mpemfkyoucheat.Config.white.get("classes").getAsJsonArray()) {
            classes.add(string.getAsString());
        }
        for (JsonElement string : net.shuyanmc.mpem.mpemfkyoucheat.Config.white.get("black_keywords").getAsJsonArray()) {
            blackKeyWords.add(string.getAsString());
        }

        if(files==null){
            throw new RuntimeException("The dir files is null.");
        }

        for (File file : files) {
            try {
                String metadata = null;
                JsonObject mixins = new JsonObject();
                if(file.getName().endsWith(".zip") || file.getName().endsWith(".jar")){
                    try (JarFile jarFile = new JarFile(file)){
                        Enumeration<JarEntry> entryEnumeration = jarFile.entries();
                        while (entryEnumeration.hasMoreElements()){
                            JarEntry entry = entryEnumeration.nextElement();
                            if(entry.getName().endsWith("mods.toml")){
                                metadata = IOUtils.toString(jarFile.getInputStream(entry));
                            }
                            else if(entry.getName().endsWith("mixins.json")){
                                mixins = JsonParser.parseString(IOUtils.toString(jarFile.getInputStream(entry))).getAsJsonObject();
                            }
                            else if(entry.getName().endsWith(".class")){
                                String clazzS = entry.getName().replace("/","").replace(".class","");
                                byte[] bytes = IOUtils.toByteArray(jarFile.getInputStream(entry));
                                ClassReader reader = new ClassReader(bytes);
                                for (String aClass : classes) {
                                    if(!clazzS.startsWith(aClass)){
                                        shitClasses.add(clazzS);
                                    }
                                }
                                ArrayList<String> strings = new ArrayList<>();
                                reader.accept(new ClassVisitor(Opcodes.ASM9) {
                                    @Override
                                    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                                     String signature, String[] exceptions) {
                                        return new MethodVisitor(Opcodes.ASM9) {
                                            @Override
                                            public void visitLdcInsn(Object value) {
                                                if (value instanceof String) {
                                                    strings.add((String) value);
                                                }
                                            }

                                            @Override
                                            public void visitParameter(String name, int access) {
                                                if(name!=null && !name.isEmpty())strings.add(name);
                                                super.visitParameter(name, access);
                                            }
                                        };
                                    }

                                    @Override
                                    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                                        strings.add(name);
                                        return super.visitField(access, name, descriptor, signature, value);
                                    }
                                }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                                for (String string : strings) {
                                    if(has(string,blackKeyWords)){
                                        shitClasses.add(clazzS);
                                    }
                                }
                            }
                        }
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
                if(metadata!=null){
                    File tmp_toml = new File("tmp_"+ RandomStringUtils.randomNumeric(6)+".toml");
                    FileUtils.writeStringToFile(tmp_toml,metadata);
                    FileConfig config = FileConfig.of(tmp_toml);
                    config.load();
                    try {
                        List<? extends Config> modsList = config.get("mods");

                        if (modsList != null && !modsList.isEmpty()) {
                            Config firstMod = modsList.get(0);

                            String modId = firstMod.get("modId");
                            String description = firstMod.get("description");

                            if(!ids.contains(modId)){
                                shitIds.add(modId);
                            }
                            if(has(description,blackKeyWords)){
                                shitDesc.add(modId);
                            }

                            String displayName = firstMod.get("displayName");

                            if(has(displayName,blackKeyWords)){
                                shitNames.add(modId);
                            }
                        }
                    } finally {
                        config.close();
                    }
                }
                if(!mixins.keySet().isEmpty()){
                    String packageMixin = mixins.get("package").getAsString();
                    JsonArray mixin = mixins.getAsJsonArray("mixins");
                    JsonArray client = mixins.getAsJsonArray("client");

                    for (JsonElement element : mixin) {
                        String k = element.getAsString();
                        if(blackKeyWords.contains(k)){
                            shitMixins.add(k);
                        }
                    }
                    for (JsonElement element : client) {
                        String k = element.getAsString();
                        if(blackKeyWords.contains(k)){
                            shitMixins.add(k);
                        }
                    }

                    for (String aClass : classes) {
                        if(!packageMixin.startsWith(aClass)){
                            shitClasses.add(packageMixin);
                        }
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StringBuilder builder = new StringBuilder();

        if(!shitIds.isEmpty()) builder.append("Suspicious mod ids: ");
        for (String shitId : shitIds) {
            builder.append(shitId).append(",");
        }
        if(!shitNames.isEmpty()) builder.append("\n").append("Suspicious mod names: ");
        for (String shitId : shitNames) {
            builder.append(shitId).append(",");
        }
        if(!shitDesc.isEmpty()) builder.append("\n").append("Suspicious mod desc: ");
        for (String shitId : shitDesc) {
            builder.append(shitId).append(",");
        }
        if(!shitMixins.isEmpty()) builder.append("\n").append("Suspicious mod mixins: ");
        for (String shitId : shitMixins) {
            builder.append(shitId).append(",");
        }
        if(!shitClasses.isEmpty()) builder.append("\n").append("Suspicious mod classes: ");
        for (String shitId : shitClasses) {
            builder.append(shitId).append(",");
        }

        MPEMFKYouCheat.reasons = builder.toString();
        MPEMFKYouCheat.cheat = !builder.isEmpty();

        System.out.println(builder);
    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return List.of();
    }

    public boolean has(String words,ArrayList<String> keywords){
        for (String keyword : keywords) {
            return words.toLowerCase().contains(keyword.toLowerCase());
        }
        return false;
    }
}
