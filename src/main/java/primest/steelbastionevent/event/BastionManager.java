package primest.steelbastionevent.event;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import primest.steelbastionevent.SteelBastionEvent;
import primest.steelbastionevent.bossbar.BossBarManager;
import primest.steelbastionevent.hologram.HologramManager;
import primest.steelbastionevent.schem.SchematicManager;
import primest.steelbastionevent.util.ColorUtil;
import primest.steelbastionevent.util.RandomUtil;
import primest.steelbastionevent.util.SoundUtil;

public class BastionManager {
    private final SteelBastionEvent plugin;
    private Location center;
    private Location chestLocation;
    private boolean active;
    private boolean opening;
    private boolean opened;
    private int openTimer;
    private int maxTimer;
    private int finishTimer = 120;
    private boolean fiftyTriggered = false;
    private BukkitRunnable openTimerTask;
    private BukkitRunnable waitOpenTask;
    private BukkitRunnable finishTask;
    private BukkitRunnable autoStartTask;

    public BastionManager(SteelBastionEvent plugin) {
        this.plugin = plugin;
    }

    public void startAutoEvent() {
        if (!this.plugin.getConfig().getBoolean("auto-event.enabled")) {
            return;
        }
        long interval = this.plugin.getConfig().getLong("auto-event.interval-seconds") * 20L;
        if (this.autoStartTask != null) {
            this.autoStartTask.cancel();
        }
        this.autoStartTask = new BukkitRunnable(){

            public void run() {
                BastionManager.this.startEvent();
            }
        };
        this.autoStartTask.runTaskTimer((Plugin)this.plugin, interval, interval);
    }

    public void startEvent() {
        if (this.active) {
            return;
        }
        this.active = true;
        this.opening = false;
        this.opened = false;
        this.fiftyTriggered = false;
        this.finishTimer = 120;
        World world = Bukkit.getWorld((String)this.plugin.getConfig().getString("world"));
        this.center = RandomUtil.randomValidLocation(world, this.plugin.getConfig().getInt("coords.min"), this.plugin.getConfig().getInt("coords.max"));
        SchematicManager.paste(this.center);
        this.chestLocation = this.findChest(this.center);
        this.openTimer = this.maxTimer = this.plugin.getConfig().getInt("chest.open-delay");
        BossBarManager.createClosed();
        if (this.chestLocation != null) {
            HologramManager.spawn(this.chestLocation.clone().add(0.5, 1.8, 0.5), this.openTimer);
        }
        this.broadcast("messages.start", this.center);
        SoundUtil.playAll("sounds.start");
        this.waitOpenTask = new BukkitRunnable(){

            public void run() {
                if (!BastionManager.this.opening && !BastionManager.this.opened) {
                    BastionManager.this.stopEvent("messages.auto-stop");
                }
            }
        };
        this.waitOpenTask.runTaskLater((Plugin)this.plugin, 3600L);
    }

    public void onChestRightClick(Player player) {
        if (!this.active || this.chestLocation == null) {
            return;
        }
        if (this.opened) {
            return;
        }
        if (this.opening) {
            String soundName;
            String msg = this.plugin.getConfig().getString("messages.chest-wait");
            if (msg != null && !msg.isEmpty()) {
                player.sendMessage(ColorUtil.color(msg));
            }
            if ((soundName = this.plugin.getConfig().getString("sounds.chest-wait.sound")) != null) {
                try {
                    Sound sound = Sound.valueOf((String)soundName.toUpperCase());
                    float volume = (float)this.plugin.getConfig().getDouble("sounds.chest-wait.volume");
                    float pitch = (float)this.plugin.getConfig().getDouble("sounds.chest-wait.pitch");
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
                catch (IllegalArgumentException sound) {
                    // empty catch block
                }
            }
            Vector knockback = player.getLocation().toVector().subtract(this.chestLocation.toVector()).normalize().multiply(1.1).setY(0.35);
            player.setVelocity(knockback);
            return;
        }
        this.opening = true;
        if (this.waitOpenTask != null) {
            this.waitOpenTask.cancel();
            this.waitOpenTask = null;
        }
        Bukkit.broadcastMessage((String)ColorUtil.color(this.plugin.getConfig().getString("messages.chest-opened").replace("%player%", player.getName())));
        SoundUtil.playAll("sounds.open");
        this.startOpeningWave();
        this.startOpenTimer();
    }

    private void startOpenTimer() {
        this.openTimerTask = new BukkitRunnable(){

            public void run() {
                if (!BastionManager.this.active || BastionManager.this.opened) {
                    this.cancel();
                    return;
                }
                BastionManager.this.openTimer--;
                HologramManager.update(BastionManager.this.openTimer);
                BossBarManager.updateOpening(BastionManager.this.openTimer, BastionManager.this.maxTimer);
                if (BastionManager.this.openTimer <= 50 && !BastionManager.this.fiftyTriggered) {
                    BastionManager.this.fiftyTriggered = true;
                    BastionManager.this.fiftySecondWave();
                }
                if (BastionManager.this.openTimer <= 0) {
                    BastionManager.this.openChest();
                    this.cancel();
                }
            }
        };
        this.openTimerTask.runTaskTimer((Plugin)this.plugin, 20L, 20L);
    }

    private void startOpeningWave() {
        int radius = 20;
        for (Player p : this.center.getWorld().getPlayers()) {
            if (!(p.getLocation().distance(this.center) <= (double)radius)) continue;
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 600, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1));
        }
        SoundUtil.playAll("sounds.wave");
        for (int r = 1; r <= radius; ++r) {
            int rr = r;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                for (double a = 0.0; a < Math.PI * 2; a += 0.19634954084936207) {
                    this.center.getWorld().spawnParticle(Particle.FLAME, this.center.clone().add(Math.cos(a) * (double)rr, 1.0, Math.sin(a) * (double)rr), 2, 0.0, 0.0, 0.0, 0.0);
                }
            }, (long)r * 2L);
        }
    }

    private void fiftySecondWave() {
        this.broadcast("messages.fifty-seconds", null);
        SoundUtil.playAll("sounds.fifty");
        int radius = 20;
        for (Player p : this.center.getWorld().getPlayers()) {
            if (!(p.getLocation().distance(this.center) <= (double)radius)) continue;
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1200, 0));
        }
    }

    private void openChest() {
        if (this.opened) {
            return;
        }
        this.opened = true;
        this.broadcast("messages.opened", null);
        final Location center = this.chestLocation.clone().add(0.5, 0.5, 0.5);
        final World world = center.getWorld();
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION_HUGE, center, 1);
        double radius = 15.0;
        double damageHP = 10.0;
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            for (Player p : world.getPlayers()) {
                if (p.getLocation().distance(center) > radius) continue;
                Vector dir = p.getLocation().toVector().subtract(center.toVector());
                if (dir.lengthSquared() < 0.01) {
                    dir = new Vector(Math.random() - 0.5, 0.0, Math.random() - 0.5);
                }
                dir.normalize().multiply(2.2).setY(0.9);
                p.setVelocity(dir);
                p.setHealth(Math.max(1.0, p.getHealth() - damageHP));
            }
        }, 1L);
        HologramManager.opened();
        BossBarManager.opened(this.finishTimer);
        final Block chestBlock = this.chestLocation.getBlock();
        if (chestBlock.getState() instanceof Chest) {
            Chest chest = (Chest)chestBlock.getState();
            chest.open();
        }
        final BukkitRunnable chestParticles = new BukkitRunnable(){

            public void run() {
                world.spawnParticle(Particle.VILLAGER_HAPPY, center, 6, 0.4, 0.3, 0.4, 0.01);
                world.spawnParticle(Particle.SPELL_INSTANT, center, 4, 0.3, 0.3, 0.3, 0.02);
            }
        };
        chestParticles.runTaskTimer((Plugin)this.plugin, 0L, 10L);
        Location dropLoc = this.chestLocation.clone().add(0.5, 1.2, 0.5);
        Random random = new Random();
        int delayBetweenDrops = this.plugin.getConfig().getInt("loot.drop-delay-ticks", 15);
        List<ItemStack> loot = this.plugin.getLoot();
        for (int i = 0; i < loot.size(); ++i) {
            ItemStack item = loot.get(i);
            if (item == null) continue;
            int delay = i * delayBetweenDrops;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                Item dropped = world.dropItem(dropLoc, item.clone());
                dropped.setPickupDelay(40);
                dropped.setGlowing(true);
                dropped.setVelocity(new Vector((random.nextDouble() - 0.5) * 1.2, 0.6, (random.nextDouble() - 0.5) * 1.2));
                world.spawnParticle(Particle.CRIT_MAGIC, dropLoc, 12, 0.3, 0.3, 0.3, 0.05);
                world.playSound(dropLoc, Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.6f);
            }, (long)delay);
        }
        this.finishTask = new BukkitRunnable(){

            public void run() {
                if (!BastionManager.this.active || BastionManager.this.finishTimer <= 0) {
                    this.cancel();
                    chestParticles.cancel();
                    if (chestBlock.getState() instanceof Chest) {
                        ((Chest)chestBlock.getState()).close();
                    }
                    BastionManager.this.stopEvent("messages.finish");
                    return;
                }
                BastionManager.this.finishTimer--;
                BossBarManager.tickOpened();
            }
        };
        this.finishTask.runTaskTimer((Plugin)this.plugin, 20L, 20L);
    }

    public void stopEvent(String messagePath) {
        if (!this.active) {
            return;
        }
        this.active = false;
        this.opening = false;
        this.opened = false;
        if (this.openTimerTask != null) {
            this.openTimerTask.cancel();
        }
        if (this.waitOpenTask != null) {
            this.waitOpenTask.cancel();
        }
        if (this.finishTask != null) {
            this.finishTask.cancel();
        }
        SchematicManager.undo();
        HologramManager.remove();
        BossBarManager.remove();
        this.broadcast(messagePath, null);
        SoundUtil.playAll("sounds.stop");
        this.center = null;
        this.chestLocation = null;
    }

    public void forceStop() {
        this.stopEvent("messages.stop");
    }

    private Location findChest(Location center) {
        int radius = 12;
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    Block b = center.clone().add((double)x, (double)y, (double)z).getBlock();
                    if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST && b.getType() != Material.BARREL) continue;
                    return b.getLocation();
                }
            }
        }
        this.plugin.getLogger().severe("\u274c \u0421\u0443\u043d\u0434\u0443\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0432 \u0441\u0445\u0435\u043c\u0435!");
        return null;
    }

    private void broadcast(String path, Location loc) {
        List<String> list = this.plugin.getConfig().getStringList(path);
        for (String s : list) {
            Bukkit.broadcastMessage((String)ColorUtil.color(s.replace("%x%", loc == null ? "" : String.valueOf(loc.getBlockX())).replace("%y%", loc == null ? "" : String.valueOf(loc.getBlockY())).replace("%z%", loc == null ? "" : String.valueOf(loc.getBlockZ()))));
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public Location getCenter() {
        return this.center;
    }

    public Location getChestLocation() {
        return this.chestLocation;
    }
}

