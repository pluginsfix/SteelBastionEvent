package primest.steelbastionevent.hologram;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import primest.steelbastionevent.SteelBastionEvent;
import primest.steelbastionevent.util.ColorUtil;

public class HologramManager {
    private static Object hologram;
    private static boolean opening;
    private static boolean opened;

    private static boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("DecentHolograms") != null;
    }

    public static void spawn(Location loc, int openTimer) {
        if (!HologramManager.isEnabled()) {
            return;
        }
        HologramManager.remove();
        opening = false;
        opened = false;
        try {
            Class<?> dhApi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            String id = "steelbastion_" + UUID.randomUUID();
            Method create = dhApi.getMethod("createHologram", String.class, Location.class);
            hologram = create.invoke(null, id, loc);
            HologramManager.showClosedIdle();
        }
        catch (Throwable t) {
            Bukkit.getLogger().severe("[SteelBastionEvent] Failed to spawn hologram");
            t.printStackTrace();
        }
    }

    public static void update(int openTimer) {
        if (!HologramManager.isEnabled() || hologram == null || opened) {
            return;
        }
        if (!opening) {
            opening = true;
        }
        HologramManager.showOpening(openTimer);
    }

    private static void showClosedIdle() {
        HologramManager.setLinesFromConfig("hologram.closed-idle", -1);
    }

    private static void showOpening(int time) {
        if (time < 0) {
            HologramManager.showClosedIdle();
            return;
        }
        HologramManager.setLinesFromConfig("hologram.opening", time);
    }

    public static void opened() {
        if (!HologramManager.isEnabled() || hologram == null) {
            return;
        }
        opened = true;
        opening = false;
        HologramManager.setLinesFromConfig("hologram.opened", -1);
    }

    private static void setLinesFromConfig(String path, int time) {
        try {
            Class<?> dhApi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            Method setLines = dhApi.getMethod("setHologramLines", hologram.getClass(), List.class);
            ArrayList<String> result = new ArrayList<String>();
            Iterator iterator = SteelBastionEvent.get().getConfig().getStringList(path).iterator();
            while (iterator.hasNext()) {
                String s;
                String line = s = (String)iterator.next();
                if (time >= 0) {
                    line = line.replace("%time%", String.valueOf(time));
                }
                result.add(ColorUtil.color(line));
            }
            if (result.isEmpty()) {
                return;
            }
            setLines.invoke(null, hologram, result);
        }
        catch (Throwable t) {
            Bukkit.getLogger().severe("[SteelBastionEvent] Failed to update hologram lines: " + path);
            t.printStackTrace();
        }
    }

    public static void remove() {
        if (!HologramManager.isEnabled() || hologram == null) {
            return;
        }
        try {
            Method delete = hologram.getClass().getMethod("delete", new Class[0]);
            delete.invoke(hologram, new Object[0]);
        }
        catch (Throwable t) {
            Bukkit.getLogger().severe("[SteelBastionEvent] Failed to delete hologram");
            t.printStackTrace();
        }
        finally {
            hologram = null;
            opening = false;
            opened = false;
        }
    }

    static {
        opening = false;
        opened = false;
    }
}

