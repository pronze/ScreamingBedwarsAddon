package io.github.pronze.sba.wrapper;

import com.google.common.base.Strings;
import io.github.pronze.sba.AddonAPI;
import io.github.pronze.sba.MessageKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bedwars.Main;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerWrapper extends org.screamingsandals.lib.player.PlayerWrapper {
    private int shout;
    private boolean shoutCooldown;
    private final AtomicBoolean isInParty = new AtomicBoolean(false);
    private final AtomicBoolean isInvitedToParty = new AtomicBoolean(false);
    private final AtomicBoolean isPartyChatEnabled = new AtomicBoolean(false);

    public PlayerWrapper(Player player) {
        super(player.getName(), player.getUniqueId());
        shout = AddonAPI.getInstance().getConfigurator().getInt("shout.time-out", 60);
    }

    public int getShoutTimeOut() {
        return shout;
    }

    public void sendMessage(String message) { getInstance().sendMessage(message); }

    public Player getInstance() {
        return Bukkit.getPlayer(getUuid());
    }

    public boolean isInParty() {
        return isInParty.get();
    }

    public void setInParty(boolean isInParty) {
        this.isInParty.set(isInParty);
    }

    public boolean isInvitedToAParty() {
        return isInvitedToParty.get();
    }

    public void setInvitedToAParty(boolean isInvited) {
        isInvitedToParty.set(isInvited);
    }

    public boolean isPartyChatEnabled() {
        return isPartyChatEnabled.get();
    }

    public void setPartyChatEnabled(boolean bool) {
        isPartyChatEnabled.set(bool);
    }

    public boolean canShout() {
        return !shoutCooldown;
    }

    public void shout(Component message) {
        if (!shoutCooldown) {
            shoutCooldown = true;
            sendMessage(message);
            if (getInstance().hasPermission("hypixelify.shout.unlimited")
                    || AddonAPI.getInstance().getConfigurator().getInt("shout.time-out", 60) == 0)
                shoutCooldown = false;

            new BukkitRunnable() {
                @Override
                public void run() {
                    shout--;
                    if (shout == 0) {
                        shoutCooldown = false;
                        shout = AddonAPI
                                .getInstance()
                                .getConfigurator()
                                .getInt("shout.time-out", 60);
                        this.cancel();
                    }
                }
            }.runTaskTimer(AddonAPI.getInstance().getJavaPlugin(), 0L, 20L);
        } else {
            final var shout = String.valueOf(getShoutTimeOut());
            AddonAPI
                    .getInstance()
                    .getLanguageService()
                    .get(MessageKeys.MESSAGE_SHOUT_WAIT)
                    .replace("%seconds%", shout)
                    .send(this);
        }
    }

    public int getXP() {
        var statistic = Main
                .getPlayerStatisticsManager()
                .getStatistic(this.getInstance());

        if (statistic == null) {
            return 1;
        }
        return statistic.getScore();
    }

    public int getLevel() {
        var xp = getXP();
        if (xp < 50) {
            return 1;
        }
        return 1 + (xp / AddonAPI.getInstance().getConfigurator().getInt("player-statistics.xp-to-level-up", 500));
    }

    public String getProgress() {
        var maxLimit  = AddonAPI
                .getInstance()
                .getConfigurator()
                .getInt("player-statistics.xp-to-level-up", 500);

        final var format = AddonAPI
                .getInstance()
                .getConfigurator()
                .getString("main-lobby.progress-format", "§b%progress%§7/§a%total%")
                .replace("%total%", String.valueOf(maxLimit));

        int progress = getXP() - ((getLevel() - 1) * maxLimit);

        return format.replace("%progress%", String.valueOf(progress));
    }

    public int getIntegerProgress() {
        var maxLimit  = AddonAPI
                .getInstance()
                .getConfigurator()
                .getInt("player-statistics.xp-to-level-up", 500);
        return getXP() - ((getLevel() - 1) * maxLimit);
    }

    public String getStringProgress() {
        var maxLimit  = AddonAPI
                .getInstance()
                .getConfigurator()
                .getInt("player-statistics.xp-to-level-up", 500);

        String progress = null;
        try {
            int p = getIntegerProgress();
            if (p < 0)
                progress = "§b0§7/§a" + maxLimit;
            else
                progress = getProgress();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (progress == null) {
            return "§b0§7/§a" + maxLimit;
        }
        return progress;
    }

    public String getCompletedBoxes() {
        var maxLimit  = AddonAPI
                .getInstance()
                .getConfigurator()
                .getInt("player-statistics.xp-to-level-up", 500);

        int progress = (getXP() - ((getLevel() - 1) * maxLimit)) / (maxLimit / 100);
        if (progress < 1)
            progress = 1;

        char i = String.valueOf(Math.abs((long) progress)).charAt(0);
        if (progress < 10) {
            i = '1';
        }
        return "§7[§b" + Strings.repeat("■", Integer.parseInt(String.valueOf(i)))
                + "§7" + Strings.repeat("■", 10 - Integer.parseInt(String.valueOf(i))) + "]";
    }
}