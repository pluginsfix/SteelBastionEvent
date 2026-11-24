package primest.steelbastionevent.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import primest.steelbastionevent.SteelBastionEvent;
import primest.steelbastionevent.event.BastionManager;
import primest.steelbastionevent.util.ColorUtil;

public class BlockProtectListener
implements Listener {
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        BastionManager manager = SteelBastionEvent.get().getBastionManager();
        if (!manager.isActive()) {
            return;
        }
        if (manager.getCenter() == null) {
            return;
        }
        int radius = SteelBastionEvent.get().getConfig().getInt("block-protect.radius");
        if (e.getBlock().getLocation().distance(manager.getCenter()) <= (double)radius) {
            e.setCancelled(true);
            for (String msg : SteelBastionEvent.get().getConfig().getStringList("block-protect.message")) {
                e.getPlayer().sendMessage(ColorUtil.color(msg));
            }
        }
    }
}

