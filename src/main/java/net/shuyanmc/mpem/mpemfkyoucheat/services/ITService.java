package net.shuyanmc.mpem.mpemfkyoucheat.services;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import net.shuyanmc.mpem.mpemfkyoucheat.ClassScanner;
import net.shuyanmc.mpem.mpemfkyoucheat.MPEMFKYouCheat;
import net.shuyanmc.mpem.mpemfkyoucheat.MixinScanner;
import net.shuyanmc.mpem.mpemfkyoucheat.ModConfig;
import net.shuyanmc.mpem.mpemfkyoucheat.mod.ModInfo;
import net.shuyanmc.mpem.mpemfkyoucheat.mod.ModInfoGetter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

public class ITService implements ITransformationService {

    private boolean loaded = false;

    @Override
    public @NotNull String name() {
        return "MPEMFKYouCheat";
    }

    @Override
    public void initialize(IEnvironment iEnvironment) {
        this.onLoad(iEnvironment, Set.of());
    }

    @Override
    public void onLoad(IEnvironment iEnvironment, Set<String> set) {
        if (loaded) return;
        loaded = true;
        ModConfig.init();
        File mods = new File("mods");
        File[] files = mods.listFiles();
        Set<String>
                shitIds = new HashSet<>(),
                shitDesc = new HashSet<>(),
                shitNames = new HashSet<>(),
                shitMixins = new HashSet<>(),
                shitClasses = new HashSet<>();

        if (files == null) {
            throw new RuntimeException("The dir files is null.");
        }

        for (File file : files) {
            try {
                if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(file)) {
                        var modClasses = ClassScanner.getClassInfo(jarFile);
                        List<ModInfo> modInfos = ModInfoGetter.getModInfo(jarFile);
                        List<String> modMixins = MixinScanner.getMixinInfo(jarFile);
                        //检测类
                        {
                            for (String class_ : modClasses.first()) {
                                boolean all = true;
                                for (String aClass : ModConfig.config.classes) {
                                    if (class_.startsWith(aClass)) {
                                        all = false;
                                        break;
                                    }
                                }
                                if (all) continue;
                                for (String aClass : ModConfig.config.classes) {
                                    if (!class_.startsWith(aClass)) {
                                        shitClasses.add(class_);
                                    }
                                }
                            }
                            for (var stringPair : modClasses.second()) {
                                for (var string : stringPair.second()) {
                                    if (has(string, ModConfig.config.keywords)) {
                                        shitClasses.add(stringPair.first());
                                    }
                                }
                            }
                        }
                        //检测模组信息
                        {
                            for (ModInfo info : modInfos) {
                                if (!ModConfig.config.ids.contains(info.modid())) {
                                    shitIds.add(info.modid());
                                }
                                if (has(info.description(), ModConfig.config.keywords)) {
                                    shitDesc.add(info.modid());
                                }
                                if (has(info.displayName(), ModConfig.config.keywords)) {
                                    shitNames.add(info.modid());
                                }
                            }
                        }

                        //检测模组Mixin
                        {
                            for (String mixin : modMixins) {
                                if (ModConfig.config.keywords.contains(mixin)) {
                                    shitMixins.add(mixin);
                                }
                            }

                            for (String mixin : modMixins) {
                                if (ModConfig.config.classes.contains(mixin)) {
                                    shitClasses.add(mixin);
                                }
                            }
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StringBuilder builder = new StringBuilder();
        if (!shitIds.isEmpty()) builder.append("Suspicious mod ids: ");
        for (String shitId : shitIds) {
            builder.append(shitId).append(",");
        }
        if (!shitNames.isEmpty()) builder.append("\n").append("Suspicious mod names: ");
        for (String shitId : shitNames) {
            builder.append(shitId).append(",");
        }
        if (!shitDesc.isEmpty()) builder.append("\n").append("Suspicious mod desc: ");
        for (String shitId : shitDesc) {
            builder.append(shitId).append(",");
        }
        if (!shitMixins.isEmpty()) builder.append("\n").append("Suspicious mod mixins: ");
        for (String shitId : shitMixins) {
            builder.append(shitId).append(",");
        }
        if (!shitClasses.isEmpty()) builder.append("\n").append("Suspicious mod classes: ");
        for (String shitId : shitClasses) {
            builder.append(shitId).append(",");
        }
        MPEMFKYouCheat.reasons = builder.toString();
        MPEMFKYouCheat.cheat = !builder.isEmpty();
        MPEMFKYouCheat.LOGGER.info(builder.toString());
    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return List.of();
    }

    public boolean has(String words, List<String> keywords) {
        for (String keyword : keywords) {
            return words.toLowerCase().contains(keyword.toLowerCase());
        }
        return false;
    }
}
