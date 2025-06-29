package net.shuyanmc.mpem.mpemfkyoucheat;

import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MPEMFKYouCheat.MODID)
public class MPEMFKYouCheat {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "mpemfkyoucheat";

    public static boolean cheat = false;
    public static String reasons = "";

    public MPEMFKYouCheat() {
    }
}
