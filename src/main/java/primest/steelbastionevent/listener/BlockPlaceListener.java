package primest.steelbastionevent.listener;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import primest.steelbastionevent.SteelBastionEvent;
import primest.steelbastionevent.event.BastionManager;
import primest.steelbastionevent.util.ColorUtil;

public class BlockPlaceListener
implements Listener {
    private final SteelBastionEvent plugin;

    public BlockPlaceListener(SteelBastionEvent plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        BastionManager manager = this.plugin.getBastionManager();
        if (!manager.isActive()) {
            return;
        }
        Location center = manager.getCenter();
        if (center == null) {
            return;
        }
        double radius = this.plugin.getConfig().getDouble("protect.block-place-radius", 50.0);
        if (e.getBlock().getLocation().distance(center) <= radius) {
            e.setCancelled(true);
            String msg = this.plugin.getConfig().getString("messages.block-place-deny");
            if (msg != null && !msg.isEmpty()) {
                e.getPlayer().sendMessage(ColorUtil.color(msg));
            }
        }
    }
}

