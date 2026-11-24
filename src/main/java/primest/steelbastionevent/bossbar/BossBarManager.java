package primest.steelbastionevent.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import primest.steelbastionevent.SteelBastionEvent;
import primest.steelbastionevent.util.ColorUtil;

public class BossBarManager {
    private static BossBar bar;
    private static int disappearTime;
    private static int disappearMax;

    public static void createClosed() {
        SteelBastionEvent pl = SteelBastionEvent.get();
        BossBarManager.remove();
        bar = Bukkit.createBossBar((String)ColorUtil.color(pl.getConfig().getString("bossbar.title-closed")), (BarColor)BarColor.valueOf((String)pl.getConfig().getString("bossbar.color").toUpperCase()), (BarStyle)BarStyle.valueOf((String)pl.getConfig().getString("bossbar.style").toUpperCase()), (BarFlag[])new BarFlag[0]);
        for (Player p : Bukkit.getOnlinePlayers()) {
            bar.addPlayer(p);
        }
        bar.setProgress(1.0);
    }

    public static void updateOpening(int time, int max) {
        if (bar == null) {
            return;
        }
        if (max <= 0) {
            max = 1;
        }
        SteelBastionEvent pl = SteelBastionEvent.get();
        bar.setTitle(ColorUtil.color(pl.getConfig().getString("bossbar.title-opening").replace("%time%", String.valueOf(time))));
        bar.setProgress(Math.max(0.0, (double)time / (double)max));
    }

    public static void opened(int seconds) {
        disappearTime = seconds;
        disappearMax = seconds;
        BossBarManager.updateOpenedTitle();
        bar.setProgress(1.0);
    }

    public static void tickOpened() {
        if (bar == null) {
            return;
        }
        if (--disappearTime < 0) {
            disappearTime = 0;
        }
        BossBarManager.updateOpenedTitle();
        bar.setProgress(Math.max(0.0, (double)disappearTime / (double)disappearMax));
    }

    private static void updateOpenedTitle() {
        SteelBastionEvent pl = SteelBastionEvent.get();
        bar.setTitle(ColorUtil.color(pl.getConfig().getString("bossbar.title-opened").replace("%time%", String.valueOf(disappearTime))));
    }

    public static void addPlayer(Player p) {
        if (bar != null) {
            bar.addPlayer(p);
        }
    }

    public static void remove() {
        if (bar != null) {
            bar.removeAll();
            bar = null;
        }
    }
}

