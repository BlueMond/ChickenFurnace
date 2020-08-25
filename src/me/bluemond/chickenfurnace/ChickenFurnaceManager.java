package me.bluemond.chickenfurnace;

import org.bukkit.*;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;

public class ChickenFurnaceManager {

    List<UUID> unloadedActiveChickens;
    Map<UUID, ActiveChicken> activeChickens;
    List<double[]> offsets;
    ChickenFurnace plugin;
    long driverWaitTicks;
    long inputCookTicks;
    boolean furnaceDriving;

    public ChickenFurnaceManager(ChickenFurnace plugin){
        this.plugin = plugin;
        loadChickensFromSave();
        offsets = new ArrayList<double[]>();
        driverWaitTicks = 20;
        inputCookTicks = 200;
        offsets.add(new double[]{.25,.25,.25});
        offsets.add(new double[]{-.25,-.25,-.25});
        offsets.add(new double[]{-.25,.25,-.25});
        offsets.add(new double[]{.25,-.25,.25});

        startFurnaceDriver();
    }

    private void loadChickensFromSave(){
        activeChickens = new HashMap<>();
        unloadedActiveChickens = plugin.getDataHandler().getAllUUIDs();
    }

    public void startFurnaceDriver(){
        furnaceDriving = true;
        furnaceDriver();
    }

    public void stopFurnaceDriver(){
        furnaceDriving = false;
    }


    // performs the operations for all active chickens every wait tick
    private void furnaceDriver(){
        if(furnaceDriving){
            Iterator<ActiveChicken> chickenIterator = activeChickens.values().iterator();

            while(chickenIterator.hasNext()){
                ActiveChicken activeChicken = chickenIterator.next();
                if(activeChicken.getChicken().getWorld().getChunkAt(activeChicken.getLocation()).isLoaded()) {
                    World world = activeChicken.getWorld();
                    long currentWorldTick = world.getFullTime();
                    long lastDropTick = activeChicken.getLastDropTick();

                    if (currentWorldTick - lastDropTick >= inputCookTicks) {
                        activeChicken.setLastDropTick(currentWorldTick);
                        activeChicken.getWorld().dropItemNaturally(activeChicken.getLocation(),
                                activeChicken.getResultantItem());
                        if (activeChicken.decrementInput()) {
                            removeActiveChicken(activeChicken.getChicken());
                            plugin.getDataHandler().removeActiveChicken(activeChicken.getChicken());
                        }else{
                            plugin.getDataHandler().setActiveChicken(activeChicken);
                        }
                        activeChicken.updateName();
                        playParticleEffect(activeChicken.getChicken());

                    }
                }else{
                    unloadActiveChicken(activeChicken);
                }
            }

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> furnaceDriver(), driverWaitTicks);
        }
    }

    // unloads an activeChicken, because its chunk is no longer loaded
    private void unloadActiveChicken(ActiveChicken activeChicken) {
        UUID uuid = activeChicken.getChicken().getUniqueId();
        activeChickens.remove(uuid);
        unloadedActiveChickens.add(uuid);
    }

    // particle effect for when an output is processed for an active chicken
    private void playParticleEffect(Chicken chicken){
        Location loc = chicken.getLocation();
        World world = chicken.getWorld();

        world.playEffect(loc, Effect.SMOKE, 1);

        for(double[] offset : offsets){
            Location location = new Location(loc.getWorld(),
                    loc.getX()+offset[0], loc.getY()+offset[1], loc.getZ()+offset[2]);
            world.spawnParticle(Particle.FLAME, location, 1, 0D, 0D, 0D, 0D);
        }
    }

    // removes a dead chicken from the furnace manager if it corresponds with an ActiveChicken instance
    // and drops its input
    public void removeDeadChicken(Chicken chicken){
        ActiveChicken activeChicken = activeChickens.get(chicken.getUniqueId());

        if(activeChicken != null){
            activeChicken.getWorld()
                    .dropItemNaturally(activeChicken.getChicken().getLocation(), activeChicken.getProcessingItem());
        }

        removeActiveChicken(chicken);
    }

    // removes an ActiveChicken instance from the furnace manager
    public void removeActiveChicken(Chicken chicken){
        unloadedActiveChickens.remove(chicken.getUniqueId());
        activeChickens.remove(chicken.getUniqueId());
        plugin.getDataHandler().removeActiveChicken(chicken);
    }

    // adds an ActiveChicken instance to the furnace manager
    public void addActiveChicken(ActiveChicken activeChicken){
        activeChickens.put(activeChicken.getChicken().getUniqueId(), activeChicken);

        plugin.getDataHandler().setActiveChicken(activeChicken);
    }

    // returns whether or not the chicken is an unloaded active chicken
    public boolean isUnloadedActiveChicken(Chicken chicken){
        if(unloadedActiveChickens.contains(chicken.getUniqueId())){
            return true;
        }
        return false;
    }

    // returns whether or not the chicken is already a loaded active chicken
    public boolean alreadyActiveChicken(Chicken chicken){
        if(activeChickens.get(chicken.getUniqueId()) != null){
            return true;
        }
        return false;
    }

    // loads saved activechicken if the chicken in question was saved and not already loaded
    private void loadInactiveChicken(Chicken chicken){
        if(isUnloadedActiveChicken(chicken) && !alreadyActiveChicken(chicken)){
            addActiveChicken(plugin.getDataHandler().getActiveChicken(chicken));
            unloadedActiveChickens.remove(chicken.getUniqueId());
        }
    }

    // tries to load an active chicken for any chicken found in the chunk
    public void loadChunkInactiveChickens(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        for(Entity entity : entities){
            if(entity instanceof Chicken){
                Chicken chicken = (Chicken) entity;

                loadInactiveChicken(chicken);
            }
        }
    }

    // tries to load an active chicken for any chickens loaded in the world
    public void loadAllWorldsInactiveChickens(){
        List<World> worlds = plugin.getServer().getWorlds();
        for(World world : worlds){
            Collection<Chicken> chickens = world.getEntitiesByClass(Chicken.class);
            for(Chicken chicken : chickens){
                loadInactiveChicken(chicken);
            }
        }
    }

    // gets the furnace recipe for a given input (returns null if none)
    public FurnaceRecipe getFurnaceRecipe(ItemStack item){

        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();

        while(recipeIterator.hasNext()){
            Recipe recipe = recipeIterator.next();
            if(recipe instanceof FurnaceRecipe){
                FurnaceRecipe furnaceRecipe = (FurnaceRecipe) recipe;
                if(item.getType().equals(furnaceRecipe.getInput().getType())){
                    return furnaceRecipe;
                }
            }
        }

        return null;
    }

    public void saveActiveChickens() {
        for(ActiveChicken activeChicken : activeChickens.values()){
            plugin.getDataHandler().setActiveChicken(activeChicken);
        }
    }

    // OLD FUNCTION USING CUSTOM NAME PARSING FOR LOADING AFTER SERVER RESTART
    /*

    private void loadInactiveChicken(Chicken chicken){
        String customName = chicken.getCustomName();
        if(customName != null && customName.contains("Smelting:") && !alreadyActiveChicken(chicken)){

            int numStart = customName.indexOf(':')+2;
            int numEnd = numStart+2;
            int matStart = numEnd;
            System.out.println("index from " + numStart + " to " + numEnd + " is "
                    + "-" + customName.substring(numStart,numEnd) + "-");
            int amount = Integer.parseInt(customName.substring(numStart, numEnd).trim());
            String materialString = customName.substring(matStart).trim().replace(' ', '_');
            Material inputMaterial = Material.matchMaterial(materialString);

            ItemStack processingItem = new ItemStack(inputMaterial, amount);
            ItemStack resultantItem = getFurnaceRecipe(processingItem).getResult();
            addActiveChicken(new ActiveChicken(chicken, processingItem, resultantItem));

        }
    }

     */


}
