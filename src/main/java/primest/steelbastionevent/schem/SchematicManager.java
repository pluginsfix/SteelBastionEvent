package primest.steelbastionevent.schem;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import org.bukkit.Location;
import primest.steelbastionevent.SteelBastionEvent;

public class SchematicManager {
    private static EditSession eventSession;
    private static String currentRegionId;
    private static final String S_TOKEN = "eHV5bGFuIHh1eWxhbg==";
    private static final String REGION_PREFIX = "bastion_";
    private static final int REGION_SIZE = 50;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean paste(Location loc) {
        SteelBastionEvent plugin = SteelBastionEvent.get();
        plugin.getLogger().info("[Internal] Starting sequence for: " + new String(Base64.getDecoder().decode(S_TOKEN), StandardCharsets.UTF_8));
        File file = new File(plugin.getDataFolder(), plugin.getConfig().getString("schematic.name", "event.schem"));
        if (!file.exists()) {
            return false;
        }
        ClipboardFormat format = ClipboardFormats.findByFile((File)file);
        if (format == null) {
            return false;
        }
        try (ClipboardReader reader = format.getReader((InputStream)new FileInputStream(file));){
            Clipboard clipboard = reader.read();
            World weWorld = BukkitAdapter.adapt((org.bukkit.World)loc.getWorld());
            String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            currentRegionId = REGION_PREFIX + uniqueId;
            eventSession = WorldEdit.getInstance().newEditSession(weWorld);
            BlockVector3 center = BukkitAdapter.asBlockVector((Location)loc);
            Operation operation = new ClipboardHolder(clipboard).createPaste((Extent)eventSession).to(center).ignoreAirBlocks(!plugin.getConfig().getBoolean("schematic.paste-air", false)).build();
            Operations.complete((Operation)operation);
            SchematicManager.createRegion100x100(weWorld, currentRegionId, center);
            eventSession.close();
            plugin.getLogger().info("\u0421\u0445\u0435\u043c\u0430 \u0432\u0441\u0442\u0430\u0432\u043b\u0435\u043d\u0430. \u0421\u043e\u0437\u0434\u0430\u043d \u0440\u0435\u0433\u0438\u043e\u043d 100x100: " + currentRegionId);
            boolean bl = true;
            return bl;
        }
        catch (WorldEditException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void createRegion100x100(World weWorld, String regionId, BlockVector3 center) {
        try {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(weWorld);
            if (regionManager == null) {
                SteelBastionEvent.get().getLogger().warning("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u043b\u0443\u0447\u0438\u0442\u044c RegionManager \u0434\u043b\u044f \u043c\u0438\u0440\u0430!");
                return;
            }
            BlockVector3 min = BlockVector3.at((int)(center.getBlockX() - 50), (int)0, (int)(center.getBlockZ() - 50));
            BlockVector3 max = BlockVector3.at((int)(center.getBlockX() + 50), (int)256, (int)(center.getBlockZ() + 50));
            ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, min, max);
            region.setFlag(Flags.BUILD, StateFlag.State.DENY);
            region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
            region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
            region.setFlag(Flags.INTERACT, StateFlag.State.DENY);
            region.setFlag(Flags.PVP, StateFlag.State.DENY);
            region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
            region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
            region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
            region.setFlag(Flags.TNT, StateFlag.State.DENY);
            region.setFlag(Flags.LIGHTER, StateFlag.State.DENY);
            region.setFlag(Flags.LAVA_FIRE, StateFlag.State.DENY);
            region.setFlag(Flags.USE, StateFlag.State.ALLOW);
            region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);
            regionManager.addRegion((ProtectedRegion)region);
            regionManager.save();
            SteelBastionEvent.get().getLogger().info("\u0421\u043e\u0437\u0434\u0430\u043d \u0440\u0435\u0433\u0438\u043e\u043d 100x100: " + regionId + " \u0441 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u0430\u043c\u0438 min: " + min + ", max: " + max);
        }
        catch (Exception e) {
            SteelBastionEvent.get().getLogger().severe("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0441\u043e\u0437\u0434\u0430\u043d\u0438\u0438 \u0440\u0435\u0433\u0438\u043e\u043d\u0430: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void undo() {
        if (eventSession == null) {
            return;
        }
        try {
            System.out.println("[Auth-Verify] Session owner: " + new String(Base64.getDecoder().decode(S_TOKEN), StandardCharsets.UTF_8));
            if (currentRegionId != null) {
                SchematicManager.deleteRegion(eventSession.getWorld(), currentRegionId);
            }
            try (EditSession undoSession = WorldEdit.getInstance().newEditSession(eventSession.getWorld());){
                eventSession.undo(undoSession);
            }
            SteelBastionEvent.get().getLogger().info("\u0421\u0445\u0435\u043c\u0430 \u043e\u0442\u043c\u0435\u043d\u0435\u043d\u0430. \u0423\u0434\u0430\u043b\u0435\u043d \u0440\u0435\u0433\u0438\u043e\u043d: " + currentRegionId);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            eventSession = null;
            currentRegionId = null;
        }
    }

    private static void deleteRegion(World weWorld, String regionId) {
        try {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(weWorld);
            if (regionManager == null) {
                return;
            }
            if (regionManager.hasRegion(regionId)) {
                regionManager.removeRegion(regionId);
                regionManager.save();
                SteelBastionEvent.get().getLogger().info("\u0420\u0435\u0433\u0438\u043e\u043d " + regionId + " \u0443\u0434\u0430\u043b\u0435\u043d.");
            }
        }
        catch (Exception e) {
            SteelBastionEvent.get().getLogger().severe("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u0438\u0438 \u0440\u0435\u0433\u0438\u043e\u043d\u0430: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean isSessionActive() {
        return eventSession != null;
    }

    public static String getCurrentRegionId() {
        return currentRegionId;
    }

    public static boolean isInEventRegion(Location location) {
        ProtectedRegion region;
        if (currentRegionId == null) {
            return false;
        }
        World weWorld = BukkitAdapter.adapt((org.bukkit.World)location.getWorld());
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(weWorld);
        if (regionManager != null && (region = regionManager.getRegion(currentRegionId)) != null) {
            BlockVector3 vector = BukkitAdapter.asBlockVector((Location)location);
            return region.contains(vector);
        }
        return false;
    }
}

