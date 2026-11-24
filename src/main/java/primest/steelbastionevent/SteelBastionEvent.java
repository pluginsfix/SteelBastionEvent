package primest.steelbastionevent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import primest.steelbastionevent.command.SBECommand;
import primest.steelbastionevent.event.BastionManager;
import primest.steelbastionevent.listener.BlockFluidListener;
import primest.steelbastionevent.listener.BlockPlaceListener;
import primest.steelbastionevent.listener.BlockProtectListener;
import primest.steelbastionevent.listener.ChestInteractListener;
import primest.steelbastionevent.listener.PlayerJoinListener;
import primest.steelbastionevent.loot.LootGUI;

public class SteelBastionEvent
extends JavaPlugin {
    private static SteelBastionEvent instance;
    private BastionManager bastionManager;
    private final List<ItemStack> loot = new ArrayList<ItemStack>();
    private File lootFile;
    private FileConfiguration lootConfig;

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.initLootFile();
        this.loadLoot();
        this.bastionManager = new BastionManager(this);
        this.getCommand("sbe").setExecutor((CommandExecutor)new SBECommand(this));
        Bukkit.getPluginManager().registerEvents((Listener)new BlockPlaceListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new BlockFluidListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new PlayerJoinListener(), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new BlockProtectListener(), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new ChestInteractListener(), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new LootGUI(), (Plugin)this);
        this.bastionManager.startAutoEvent();
    }

    public void onDisable() {
        this.saveLoot();
        if (this.bastionManager != null) {
            this.bastionManager.forceStop();
        }
    }

    private void initLootFile() {
        this.lootFile = new File(this.getDataFolder(), "loot.yml");
        if (!this.lootFile.exists()) {
            try {
                this.lootFile.getParentFile().mkdirs();
                this.lootFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.lootConfig = YamlConfiguration.loadConfiguration((File)this.lootFile);
    }

    public void loadLoot() {
        this.loot.clear();
        List list = this.lootConfig.getList("loot");
        if (list == null) {
            return;
        }
        for (Object obj : list) {
            if (!(obj instanceof ItemStack)) continue;
            this.loot.add((ItemStack)obj);
        }
        this.getLogger().info("\u0417\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u043e \u043b\u0443\u0442\u0430: " + this.loot.size());
    }

    public void saveLoot() {
        this.lootConfig.set("loot", this.loot);
        try {
            this.lootConfig.save(this.lootFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ItemStack> getLoot() {
        return this.loot;
    }

    public static SteelBastionEvent get() {
        return instance;
    }

    public BastionManager getBastionManager() {
        return this.bastionManager;
    }
}

