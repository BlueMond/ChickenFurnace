package me.bluemond.chickenfurnace;

import org.bukkit.*;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;

public class ChickenFurnaceManager {

    List<ActiveChicken> activeChickens;
    List<double[]> offsets;
    ChickenFurnace plugin;
    long driverWaitTicks;
    long inputCookTicks;
    boolean furnaceDriving;

    public ChickenFurnaceManager(ChickenFurnace plugin){
        activeChickens = new ArrayList<>();
        offsets = new ArrayList<double[]>();
        this.plugin = plugin;
        driverWaitTicks = 20;
        inputCookTicks = 200;
        offsets.add(new double[]{.4,.4,.4});
        offsets.add(new double[]{-.5,-.5,-.5});
        offsets.add(new double[]{-.5,.5,-.5});
        offsets.add(new double[]{.5,-.5,.5});

        startFurnaceDriver();
    }

    public void startFurnaceDriver(){
        furnaceDriving = true;
        furnaceDriver();
    }

    public void stopFurnaceDriver(){
        furnaceDriving = false;
    }

    private void furnaceDriver(){
        if(furnaceDriving){
            Iterator<ActiveChicken> chickenIterator = activeChickens.iterator();

            while(chickenIterator.hasNext()){
                ActiveChicken activeChicken = chickenIterator.next();
                World world = activeChicken.getWorld();
                long currentWorldTick = world.getFullTime();
                long lastDropTick = activeChicken.getLastDropTick();
                if(currentWorldTick - lastDropTick >= inputCookTicks){
                    activeChicken.setLastDropTick(currentWorldTick);
                    activeChicken.getWorld().dropItemNaturally(activeChicken.getLocation(),
                            activeChicken.getResultantItem());
                    if (activeChicken.decrementInput()){
                        chickenIterator.remove();
                    }
                    activeChicken.updateName();
                    playParticleEffect(activeChicken.getChicken());
                }
            }

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> furnaceDriver(), driverWaitTicks);
        }
    }

    private void playParticleEffect(Chicken chicken){
        Location loc = chicken.getLocation();
        World world = chicken.getWorld();

        world.playEffect(loc, Effect.SMOKE, 1);

        for(double[] offset : offsets){
            Location location = new Location(loc.getWorld(),
                    loc.getX()+offset[0], loc.getY()+offset[1], loc.getZ()+offset[2]);
            world.spawnParticle(Particle.FLAME, location, 1, 0D, 0D, 0D, 0D);
        }
            //world.spawnParticle(Particle.FLAME, loc, 1, offset[0], offset[1], offset[2]);
    }

    public void removeDeadChicken(Chicken chicken){
        Iterator<ActiveChicken> chickenIterator = activeChickens.iterator();

        while(chickenIterator.hasNext()){
            ActiveChicken activeChicken = chickenIterator.next();
            if(chicken.equals(activeChicken.getChicken())){
                chickenIterator.remove();
                activeChicken.getWorld()
                        .dropItemNaturally(activeChicken.getChicken().getLocation(), activeChicken.getProcessingItem());
                break;
            }
        }
    }

    public void addActiveChicken(ActiveChicken activeChicken){
        activeChickens.add(activeChicken);
    }

    public boolean alreadyActiveChicken(Chicken chicken){
        for(ActiveChicken activeChicken : activeChickens){
            if(chicken.equals(activeChicken.getChicken())){
                return true;
            }
        }

        return false;
    }

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

    public void loadChunkInactiveChickens(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        for(Entity entity : entities){
            if(entity instanceof Chicken){
                Chicken chicken = (Chicken) entity;

                loadInactiveChicken(chicken);
            }
        }
    }

    public void loadAllWorldsInactiveChickens(List<World> worlds){
        for(World world : worlds){
            Collection<Chicken> chickens = world.getEntitiesByClass(Chicken.class);
            for(Chicken chicken : chickens){
                loadInactiveChicken(chicken);
            }
        }
    }

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
}
