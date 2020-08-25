package me.bluemond.chickenfurnace.datahandler;

import me.bluemond.chickenfurnace.ActiveChicken;
import me.bluemond.chickenfurnace.ChickenFurnace;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Chicken;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DataHandler {
    private final AbstractConfig dataConfig;
    private FileConfiguration dataFile;

    private final ChickenFurnace plugin;
    private ConfigurationSection activeChickens;

    public DataHandler(ChickenFurnace plugin) {
        this.plugin = plugin;
        dataConfig = new AbstractConfig(plugin, "data.yml");

        // Load data.yml
        try {
            dataConfig.createNewFile();
            dataFile = dataConfig.getConfig();
            dataConfig.saveConfig();
            activeChickens = dataFile.getConfigurationSection("activechickens");
            if(activeChickens == null){
                activeChickens = dataFile.createSection("activechickens");
            }
        } catch (InvalidConfigurationException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not load data file!");
            e.printStackTrace();
        }

    }

    public List<UUID> getAllUUIDs(){
        Set<String> keys = activeChickens.getKeys(false);
        List<UUID> uuids = new ArrayList<>();

        for(String uuidString : keys){
            uuids.add(UUID.fromString(uuidString));
        }

        return uuids;
    }

    public ActiveChicken getActiveChicken(Chicken chicken){
        UUID uuid = chicken.getUniqueId();
        if(activeChickens.getConfigurationSection(uuid.toString()) != null){
            Material inputMaterial = Material.matchMaterial(activeChickens.getString(uuid + ".input.material"));
            int inputAmount = activeChickens.getInt(uuid + ".input.amount");
            ItemStack input = new ItemStack(inputMaterial, inputAmount);
            ItemStack output = plugin.getChickenFurnaceManager().getFurnaceRecipe(input).getResult();
            return new ActiveChicken(chicken, input, output);
        }else{
            return null;
        }
    }

    public void setActiveChicken(ActiveChicken activeChicken){
        UUID uuid = activeChicken.getChicken().getUniqueId();
        activeChickens.set(uuid + ".input.material", activeChicken.getProcessingItem().getType().toString());
        activeChickens.set(uuid + ".input.amount", activeChicken.getProcessingItem().getAmount());
        dataConfig.saveConfig();
    }

    public void removeActiveChicken(Chicken chicken){
        activeChickens.set(chicken.getUniqueId().toString(), null);
        dataConfig.saveConfig();
    }

}
