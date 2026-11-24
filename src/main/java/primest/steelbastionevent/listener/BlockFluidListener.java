package primest.steelbastionevent.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import primest.steelbastionevent.SteelBastionEvent;
import primest.steelbastionevent.event.BastionManager;
import primest.steelbastionevent.util.ColorUtil;

public class BlockFluidListener
implements Listener {
    private final SteelBastionEvent plugin;

    public BlockFluidListener(SteelBastionEvent plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        BastionManager manager = this.plugin.getBastionManager();
        if (!manager.isActive()) {
            return;
        }
        Location center = manager.getCenter();
        if (center == null) {
            return;
        }
        Material bucket = e.getBucket();
        if (bucket != Material.WATER_BUCKET && bucket != Material.LAVA_BUCKET) {
            return;
        }
        Location placeLoc = e.getBlockClicked().getLocation();
        double radius = this.plugin.getConfig().getDouble("protect.block-place-radius", 50.0);
        if (placeLoc.distance(center) <= radius) {
            e.setCancelled(true);
            String msg = this.plugin.getConfig().getString("messages.fluid-place-deny");
            if (msg != null && !msg.isEmpty()) {
                e.getPlayer().sendMessage(ColorUtil.color(msg));
            }
        }
    }
}

