package club.somc.statsSpigotPlugin;

import club.somc.protos.stats.Stats;
import club.somc.protos.stats.UpdateStats;
import io.nats.client.Connection;
import io.nats.client.Nats;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class StatsSpigotPlugin extends JavaPlugin implements Listener {

    Connection nc;
    String serverName;

    @Override
    public void onEnable() {
        super.onEnable();
        this.saveDefaultConfig();

        serverName = getConfig().getString("serverName");

        if (serverName == null || serverName.trim().isEmpty()) {
            getLogger().severe("Server name is null or empty!");
            return;
        }

        try {
            this.nc = Nats.connect(getConfig().getString("natsUrl"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Stats stats = Stats.newBuilder()
                .setMinecraftUuid(player.getUniqueId().toString())
                .setServer(serverName)
                .setPlaytime(player.getStatistic(Statistic.PLAY_ONE_MINUTE))
                .setDeaths(player.getStatistic(Statistic.DEATHS))
                .build();

        UpdateStats msg = UpdateStats.newBuilder()
                .setStats(stats)
                .build();

        nc.publish("stats.update", msg.toByteArray());
    }
}
