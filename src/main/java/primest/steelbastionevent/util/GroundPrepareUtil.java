package primest.steelbastionevent.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class GroundPrepareUtil {
    public static int prepare(Location center, int radius) {
        World world = center.getWorld();
        int baseX = center.getBlockX();
        int baseZ = center.getBlockZ();
        int targetY = 0;
        for (int x = -radius; x <= radius; ++x) {
            for (int z = -radius; z <= radius; ++z) {
                int y = world.getHighestBlockYAt(baseX + x, baseZ + z);
                targetY = Math.max(targetY, y);
            }
        }
        Material ground = world.getBlockAt(baseX, targetY - 1, baseZ).getType();
        if (ground == Material.WATER || ground == Material.LAVA) {
            return -1;
        }
        for (int x = -radius; x <= radius; ++x) {
            for (int z = -radius; z <= radius; ++z) {
                int highest;
                int y;
                int wx = baseX + x;
                int wz = baseZ + z;
                for (y = highest = world.getHighestBlockYAt(wx, wz); y > targetY; --y) {
                    world.getBlockAt(wx, y, wz).setType(Material.AIR, false);
                }
                for (y = highest + 1; y < targetY; ++y) {
                    world.getBlockAt(wx, y, wz).setType(Material.DIRT, false);
                }
            }
        }
        return targetY;
    }

    public static int prepare(Location center) {
        return GroundPrepareUtil.prepare(center, 10);
    }
}

