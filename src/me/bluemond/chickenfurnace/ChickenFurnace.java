package me.bluemond.chickenfurnace;

import org.bukkit.plugin.java.JavaPlugin;



public class ChickenFurnace extends JavaPlugin {

    private CommandManager commandManager;
    private ChickenFurnaceManager chickenFurnaceManager;

    @Override
    public void onEnable() {
        // on server enabling the plugin

        //commandManager = new CommandManager(this);
        //getCommand("pocketfurnace").setExecutor(commandManager);

        // ADD PARTICLE EFFECTS AND SOUND TO CHICKEN WHEN THEY FINISH SMELTING 1 INPUT

        chickenFurnaceManager = new ChickenFurnaceManager(this);
        chickenFurnaceManager.loadAllWorldsInactiveChickens(getServer().getWorlds());

        getServer().getPluginManager().registerEvents(new ChickenListener(this), this);

        getLogger().info("ChickenFurnace v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        // on server disabling the plugin

        chickenFurnaceManager.stopFurnaceDriver();

        getLogger().info("ChickenFurnace v" + getDescription().getVersion() + " has been disabled!");
    }

    public ChickenFurnaceManager getChickenFurnaceManager() {
        return chickenFurnaceManager;
    }
}
