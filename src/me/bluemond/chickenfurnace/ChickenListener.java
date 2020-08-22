package me.bluemond.chickenfurnace;

import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

public class ChickenListener implements Listener {

    ChickenFurnace plugin;

    public ChickenListener(ChickenFurnace plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onChickenDied(EntityDeathEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Chicken){
            Chicken chicken = (Chicken) entity;
            plugin.getChickenFurnaceManager().removeDeadChicken(chicken);
        }
    }

    @EventHandler
    public void onPlayerFeedChicken(PlayerInteractEntityEvent event){
        Entity entity = event.getRightClicked();

        if(entity instanceof Chicken){
            Player player = event.getPlayer();
            Chicken chicken = (Chicken) entity;
            EquipmentSlot slot = event.getHand();
            ItemStack item = player.getInventory().getItem(slot);

            FurnaceRecipe furnaceRecipe = plugin.getChickenFurnaceManager().getFurnaceRecipe(item);

            if(furnaceRecipe != null){
                plugin.getChickenFurnaceManager().addActiveChicken(
                        new ActiveChicken(chicken, item, furnaceRecipe.getResult()) );

                player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        plugin.getChickenFurnaceManager().loadChunkInactiveChickens(event.getChunk());
    }


}
