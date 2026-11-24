package primest.steelbastionevent.loot;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import primest.steelbastionevent.SteelBastionEvent;

public class LootGUI
implements Listener {
    private static final String TITLE = "Bastion Loot";
    private static Inventory inv;

    public static void open(Player p) {
        SteelBastionEvent plugin = SteelBastionEvent.get();
        inv = Bukkit.createInventory(null, (int)54, (String)TITLE);
        for (ItemStack item : plugin.getLoot()) {
            if (item == null) continue;
            inv.addItem(new ItemStack[]{item.clone()});
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) {
            return;
        }
        SteelBastionEvent plugin = SteelBastionEvent.get();
        plugin.getLoot().clear();
        for (ItemStack item : e.getInventory().getContents()) {
            if (item == null) continue;
            plugin.getLoot().add(item.clone());
        }
        plugin.saveLoot();
    }
}

