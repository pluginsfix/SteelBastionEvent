package primest.steelbastionevent.util;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class RandomUtil {
    private static final int CHECK_RADIUS = 3;
    private static final int MAX_HEIGHT_DIFF = 2;

    public static Location randomValidLocation(World world, int min, int max) {
        for (int i = 0; i < 100; ++i) {
            int z;
            int y;
            int x = ThreadLocalRandom.current().nextInt(min, max);
            Location base = new Location(world, (double)x, (double)(y = world.getHighestBlockYAt(x, z = ThreadLocalRandom.current().nextInt(min, max))), (double)z);
            Material mat = base.getBlock().getType();
            if (mat == Material.WATER || mat == Material.LAVA || !RandomUtil.isFlat(world, x, z, y)) continue;
            return base;
        }
        int y = world.getHighestBlockYAt(0, 0);
        return new Location(world, 0.0, (double)y, 0.0);
    }

    private static boolean isFlat(World world, int x, int z, int baseY) {
        for (int dx = -3; dx <= 3; ++dx) {
            for (int dz = -3; dz <= 3; ++dz) {
                int y = world.getHighestBlockYAt(x + dx, z + dz);
                if (Math.abs(y - baseY) > 2) {
                    return false;
                }
                Material mat = world.getBlockAt(x + dx, y, z + dz).getType();
                if (mat != Material.WATER && mat != Material.LAVA) continue;
                return false;
            }
        }
        return true;
    }
}

