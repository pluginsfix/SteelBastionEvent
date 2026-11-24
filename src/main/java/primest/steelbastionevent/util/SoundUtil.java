package primest.steelbastionevent.util;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import primest.steelbastionevent.SteelBastionEvent;

public class SoundUtil {
    public static void playAll(String path) {
        Sound sound = Sound.valueOf((String)SteelBastionEvent.get().getConfig().getString(path));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
        }
    }
}

