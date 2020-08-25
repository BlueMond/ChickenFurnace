package me.bluemond.chickenfurnace;

import me.bluemond.chickenfurnace.datahandler.DataHandler;
import org.bukkit.plugin.java.JavaPlugin;



public class ChickenFurnace extends JavaPlugin {

    private CommandManager commandManager;
    private ChickenFurnaceManager chickenFurnaceManager;
    private DataHandler dataHandler;

    @Override
    public void onEnable() {
        // on server enabling the plugin

        //commandManager = new CommandManager(this);
        //getCommand("chickenfurnace").setExecutor(commandManager);

        // ADD PARTICLE EFFECTS AND SOUND TO CHICKEN WHEN THEY FINISH SMELTING 1 INPUT

        dataHandler = new DataHandler(this);
        chickenFurnaceManager = new ChickenFurnaceManager(this);
        chickenFurnaceManager.loadAllWorldsInactiveChickens();

        getServer().getPluginManager().registerEvents(new ChickenListener(this), this);

        getLogger().info("ChickenFurnace v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        // on server disabling the plugin

        chickenFurnaceManager.stopFurnaceDriver();
        chickenFurnaceManager.saveActiveChickens();

        getLogger().info("ChickenFurnace v" + getDescription().getVersion() + " has been disabled!");
    }

    public ChickenFurnaceManager getChickenFurnaceManager() {
        return chickenFurnaceManager;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }
}
