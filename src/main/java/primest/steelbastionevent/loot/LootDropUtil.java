package primest.steelbastionevent.loot;

import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import primest.steelbastionevent.SteelBastionEvent;

public class LootDropUtil {
    private static final Random random = new Random();

    public static void drop(Location loc, List<ItemStack> items) {
        World world = loc.getWorld();
        if (world == null) {
            return;
        }
        int delay = 0;
        for (ItemStack item : items) {
            if (item == null) continue;
            SteelBastionEvent.get().getServer().getScheduler().runTaskLater((Plugin)SteelBastionEvent.get(), () -> {
                Item drop = world.dropItem(loc, item);
                drop.setPickupDelay(40);
                drop.setGlowing(true);
                Vector velocity = new Vector(LootDropUtil.randomDouble(-0.4, 0.4), LootDropUtil.randomDouble(0.4, 0.8), LootDropUtil.randomDouble(-0.4, 0.4));
                drop.setVelocity(velocity);
            }, (long)delay);
            delay += 8;
        }
    }

    private static double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}

