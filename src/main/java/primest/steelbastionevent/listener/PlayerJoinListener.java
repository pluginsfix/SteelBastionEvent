package primest.steelbastionevent.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import primest.steelbastionevent.SteelBastionEvent;
import primest.steelbastionevent.bossbar.BossBarManager;
import primest.steelbastionevent.event.BastionManager;

public class PlayerJoinListener
implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        BastionManager manager = SteelBastionEvent.get().getBastionManager();
        if (!manager.isActive()) {
            return;
        }
        BossBarManager.addPlayer(e.getPlayer());
    }
}

