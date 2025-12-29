package com.gamingmesh.jobs.listeners;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.NexoBlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.hooks.JobsHook;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;

import net.Zrips.CMILib.Enchants.CMIEnchantEnum;
import net.Zrips.CMILib.Enchants.CMIEnchantment;
import net.Zrips.CMILib.Items.CMIItemStack;

public class JobsNexoBreakPaymentListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNexoBlockBreak(NexoNoteBlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!Jobs.getGCManager().canPerformActionInWorld(player.getWorld()))
            return;

        // Checks whether the broken block has been tracked by BlockTracker
        if (JobsHook.BlockTracker.isEnabled() && Jobs.getGCManager().useBlockProtectionBlockTracker && JobsHook.getBlockTrackerManager().isTracked(event.getBlock())) {
            return;
        }

        // Remove block ownership
        Jobs.getInstance().removeBlockOwnerShip(event.getBlock());

        // check if in creative
        if(!JobsPaymentListener.payIfCreative(player))
            return;

        if (!Jobs.getPermissionHandler().hasWorldPermission(player, player.getWorld().getName()))
            return;

        // check if player is riding
        if (Jobs.getGCManager().disablePaymentIfRiding && player.isInsideVehicle())
            return;

        if (!JobsPaymentListener.payForItemDurabilityLoss(player))
            return;

        // Protection for block break with silktouch
        if (Jobs.getGCManager().useSilkTouchProtection) {
            ItemStack item = CMIItemStack.getItemInMainHand(player);

            if (item.getType() != Material.AIR && Jobs.getExploitManager().isInProtection(event.getBlock())) {
                for (Enchantment one : item.getEnchantments().keySet()) {
                    CMIEnchantment enchant = CMIEnchantment.get(one);
                    if (enchant != null && enchant.equalEnum(CMIEnchantEnum.SILK_TOUCH)) {
                        return;
                    }
                }
            }
        }

        NexoBlockActionInfo nbInfo = new NexoBlockActionInfo(event.getMechanic().getItemID(), ActionType.NEXOBREAK);
        Jobs.action(Jobs.getPlayerManager().getJobsPlayer(player), nbInfo);
    }
}
