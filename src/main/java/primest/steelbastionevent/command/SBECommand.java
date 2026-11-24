package primest.steelbastionevent.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import primest.steelbastionevent.SteelBastionEvent;
import primest.steelbastionevent.loot.LootGUI;
import primest.steelbastionevent.util.ColorUtil;

public class SBECommand
implements CommandExecutor {
    private final SteelBastionEvent plugin;

    public SBECommand(SteelBastionEvent plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.color("&c/sbe start | stop | lootgui"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "start": {
                this.plugin.getBastionManager().startEvent();
                break;
            }
            case "stop": {
                this.plugin.getBastionManager().forceStop();
                break;
            }
            case "lootgui": {
                if (!(sender instanceof Player)) {
                    return true;
                }
                LootGUI.open((Player)sender);
            }
        }
        return true;
    }
}

