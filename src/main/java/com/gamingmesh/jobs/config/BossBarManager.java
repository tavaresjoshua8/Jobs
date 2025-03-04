package com.gamingmesh.jobs.config;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.BossBarInfo;
import com.gamingmesh.jobs.container.CurrencyType;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import com.gamingmesh.jobs.container.MessageToggleState;
import com.gamingmesh.jobs.stuff.ToggleBarHandling;

import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class BossBarManager {

    private Jobs plugin;

    public BossBarManager(Jobs plugin) {
        this.plugin = plugin;
    }

    public void ShowJobProgression(final JobsPlayer player) {
        if (Version.getCurrent().isLower(Version.v1_9_R1) || player == null)
            return;

        for (JobProgression oneJob : player.getJobProgression()) {
            if (oneJob.getLastExperience() != 0) {
                ShowJobProgression(player, oneJob, oneJob.getLastExperience());
            }
        }
        player.getUpdateBossBarFor().clear();
    }

    public void ShowJobProgression(final JobsPlayer player, final JobProgression jobProg, double expGain) {
        if (Version.getCurrent().isLower(Version.v1_9_R1) || Jobs.getGCManager().BossBarsMessageDefault.equals(MessageToggleState.Off))
            return;

        if (ToggleBarHandling.getBossBarState(player.getUniqueId()).equals(MessageToggleState.Off))
            return;

        showJobProgressionInTask(player, jobProg, expGain);
    }

    private static synchronized void showJobProgressionInTask(final JobsPlayer player, final JobProgression jobProg, double expGain) {
        BossBar bar = null;
        BossBarInfo oldOne = null;
        for (BossBarInfo one : player.getBossBarInfo()) {
            if (one.getJobName().equalsIgnoreCase(jobProg.getJob().getName())) {
                one.cancel();
                bar = one.getBar();
                oldOne = one;
                break;
            }
        }

        String gain = "";
        if (expGain != 0) {
            expGain = (int) (expGain * 100) / 100D;
            gain = expGain > 0 ? "+" + expGain : "" + expGain;
            gain = Jobs.getLanguage().getMessage("command.stats.bossBarGain", "%gain%", gain);
        }

        String message = Jobs.getLanguage().getMessage("command.stats.bossBarOutput",
            "%joblevel%", jobProg.getLevelFormatted(),
            jobProg.getJob(),
            "%jobxp%", CurrencyType.EXP.format(jobProg.getExperience()),
            "%jobmaxxp%", jobProg.getMaxExperience(),
            "%gain%", gain);

        if (bar == null) {
            bar = initBossBar(player, jobProg, message);
        } else
            bar.setTitle(message);

        double percentage = jobProg.getExperience() / jobProg.getMaxExperience();
        percentage = percentage > 1D ? 1D : percentage < 0 ? 0 : percentage;
        bar.setProgress(percentage);

        if (oldOne == null && player.getPlayer() != null) {
            bar.addPlayer(player.getPlayer());
            oldOne = new BossBarInfo(player.getName(), jobProg.getJob().getName(), bar);
            player.getBossBarInfo().add(oldOne);
        }

        bar.setVisible(true);

        if (oldOne != null)
            oldOne.setScheduler(CMIScheduler.runTaskLater(Jobs.getInstance(), () -> {
                for (BossBarInfo one : player.getBossBarInfo()) {
                    if (!one.getPlayerName().equalsIgnoreCase(player.getName()))
                        continue;

                    if (!one.getJobName().equalsIgnoreCase(jobProg.getJob().getName()))
                        continue;

                    BossBar tempBar = one.getBar();
                    tempBar.setVisible(false);
                    break;
                }
            }, Jobs.getGCManager().BossBarTimer * 20L));

        jobProg.setLastExperience(0D);
    }

    private static BossBar initBossBar(final JobsPlayer player, final JobProgression jobProg, String message) {
        BarColor color = getColor(jobProg.getJob());
        if (color == null) {
            switch (player.getBossBarInfo().size()) {
            case 1:
                color = BarColor.GREEN;
                break;
            case 2:
                color = BarColor.RED;
                break;
            case 3:
                color = BarColor.WHITE;
                break;
            case 4:
                color = BarColor.YELLOW;
                break;
            case 5:
                color = BarColor.PINK;
                break;
            case 6:
                color = BarColor.PURPLE;
                break;
            default:
                color = BarColor.BLUE;
                break;
            }
        }
        BarStyle style;
        switch (Jobs.getGCManager().SegmentCount) {
        case 1:
            style = BarStyle.SOLID;
            break;
        case 6:
            style = BarStyle.SEGMENTED_6;
            break;
        case 10:
            style = BarStyle.SEGMENTED_10;
            break;
        case 12:
            style = BarStyle.SEGMENTED_12;
            break;
        case 20:
            style = BarStyle.SEGMENTED_20;
            break;
        default:
            style = BarStyle.SOLID;
            break;
        }
        return Bukkit.createBossBar(message, color, style);
    }

    private static BarColor getColor(Job job) {
        if (job.getBossbar() == null)
            return null;
        for (BarColor color : BarColor.values()) {
            if (job.getBossbar().equalsIgnoreCase(color.name()))
                return color;
        }
        return null;
    }
}
