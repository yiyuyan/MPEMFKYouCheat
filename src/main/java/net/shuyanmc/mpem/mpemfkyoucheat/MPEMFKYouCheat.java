package net.shuyanmc.mpem.mpemfkyoucheat;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MPEMFKYouCheat.MODID)
public class MPEMFKYouCheat {
    public static final String MODID = "mpemfkyoucheat";
    public static final Logger LOGGER = LoggerFactory.getLogger("FkYouCheat");

    public static boolean cheat = false;
    public static String reasons = "";

    public MPEMFKYouCheat() {
    }
}
