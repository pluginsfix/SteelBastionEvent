package primest.steelbastionevent.listener;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import primest.steelbastionevent.SteelBastionEvent;
import primest.steelbastionevent.event.BastionManager;

public class ChestInteractListener
implements Listener {
    @EventHandler
    public void onChestClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!(block.getState() instanceof Chest)) {
            return;
        }
        BastionManager manager = SteelBastionEvent.get().getBastionManager();
        if (!manager.isActive()) {
            return;
        }
        if (manager.getChestLocation() == null) {
            return;
        }
        if (!block.getLocation().equals((Object)manager.getChestLocation())) {
            return;
        }
        e.setCancelled(true);
        manager.onChestRightClick(e.getPlayer());
    }
}

