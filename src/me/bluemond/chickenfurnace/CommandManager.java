package me.bluemond.chickenfurnace;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class CommandManager implements CommandExecutor {

    private final ChickenFurnace plugin;

    public CommandManager(ChickenFurnace plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {



        return true;
    }

}
