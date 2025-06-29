package net.shuyanmc.mpem.mpemfkyoucheat.mod;

public record ModInfo(
        String version,
        ModLoader loader,
        String modid,
        String description,
        String displayName
) {
}
