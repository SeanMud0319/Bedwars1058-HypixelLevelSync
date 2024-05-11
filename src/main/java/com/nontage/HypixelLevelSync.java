package com.nontage;

import com.andrei1058.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import top.speedcubing.lib.api.hypixel.HypixelLib;
import top.speedcubing.lib.api.hypixel.player.HypixelPlayer;
import top.speedcubing.lib.api.hypixel.stats.BedwarsStats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HypixelLevelSync extends JavaPlugin implements Listener {
    static BedWars bw;
    static HypixelLevelSync plugin;
    static Map<UUID, String[]> levelCatch = new HashMap<>();

    public void onEnable() {
        getLogger().info("Enabling plugin...");
        plugin = this;
        if (Bukkit.getPluginManager().getPlugin("BedWars1058") == null) {
            getLogger().severe("BedWars1058 was not found. Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        var registration = Bukkit.getServicesManager().getRegistration(BedWars.class);
        if (null == registration) {
            getLogger().severe("Cannot hook into BedWars1058.");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
        saveDefaultConfig();
        HypixelLib.key = getConfig().getString("key");
        System.out.println(HypixelLib.key);
        Bukkit.getPluginManager().registerEvents(this, this);

        bw = registration.getProvider();

        getLogger().info("Hooked into BedWars1058!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        long currentTime = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> updatePlayerStats(p, currentTime));
    }

    private void updatePlayerStats(Player p, long currentTime) {
        if (levelCatch.containsKey(p.getUniqueId())) {
            if (currentTime - Long.parseLong(levelCatch.get(p.getUniqueId())[1]) > 1000 * 60 * 60) {
                levelCatch.remove(p.getUniqueId());
            } else {
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    String[] data = levelCatch.get(p.getUniqueId());
                    bw.getLevelsUtil().setLevel(p, Integer.parseInt(data[0]));
                }, 40L);

                return;
            }
        }
        HypixelPlayer hypixelPlayer = HypixelPlayer.get(p.getUniqueId());
        BedwarsStats stats = hypixelPlayer.getBedwars();
        int level = stats.getLevel();
        bw.getLevelsUtil().setLevel(p, level);
        levelCatch.put(p.getUniqueId(), new String[]{String.valueOf(level), String.valueOf(currentTime)});
    }
}