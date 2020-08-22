package me.bluemond.chickenfurnace;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Chicken;
import org.bukkit.inventory.ItemStack;

public class ActiveChicken {

    Chicken chicken;
    ItemStack processingItem;
    ItemStack resultantItem;
    long lastDropTick;


    public ActiveChicken(Chicken chicken, ItemStack processingItem, ItemStack resultantItem){
        this.chicken = chicken;
        this.processingItem = processingItem;
        this.resultantItem = resultantItem;
        this.lastDropTick = chicken.getWorld().getFullTime();
        chicken.setRemoveWhenFarAway(false);
        updateName();
    }

    // returns whether or not the input is empty
    public boolean decrementInput(){
        processingItem.setAmount(processingItem.getAmount() - 1);
        if(processingItem.getAmount() <= 0){
            return true;
        }
        return false;
    }

    public ItemStack getProcessingItem() {
        return processingItem;
    }

    public ItemStack getResultantItem(){
        return resultantItem;
    }

    public World getWorld(){
        return chicken.getWorld();
    }

    public long getLastDropTick(){
        return lastDropTick;
    }

    public void setLastDropTick(long lastDropTick){
        this.lastDropTick = lastDropTick;
    }

    public Location getLocation() {
        return chicken.getLocation();
    }

    public void updateName(){
        if(processingItem.getAmount() < 1){
            chicken.setCustomName("");
            chicken.setCustomNameVisible(false);
        }else{
            chicken.setCustomName("Smelting: " + processingItem.getAmount() + " " +
                    processingItem.getType().name().replace('_', ' ').toLowerCase());
        }

    }

    public Chicken getChicken(){
        return chicken;
    }

}
