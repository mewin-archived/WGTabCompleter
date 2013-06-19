/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mewin.WGTC;

import com.mewin.WGTC.commands.RegionCommand;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.lang.reflect.Field;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_5_R2.CraftServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class WGTCPlugin extends JavaPlugin {
    private CraftServer craftServer;
    private WorldGuardPlugin wgPlugin;
    
    @Override
    public void onEnable()
    {
        wgPlugin = this.getWGPlugin();
        if (wgPlugin == null)
        {
            return;
        }
        craftServer = (CraftServer) getServer();
        replaceCommand();
    }
    
    private void replaceCommand()
    {
        Command originalRegionCmd = craftServer.getCommandMap().getCommand("region");
        RegionCommand rgCmd = new RegionCommand(originalRegionCmd, wgPlugin);
        Map<String, Command> knownCommands = (Map<String, Command>) getPrivateValue(craftServer.getCommandMap(), "knownCommands");
        knownCommands.put("region", rgCmd);
        knownCommands.put("rg", rgCmd);
        knownCommands.put("regions", rgCmd);
    }
    
    private WorldGuardPlugin getWGPlugin()
    {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        
        if (plugin == null || !(plugin instanceof WorldGuardPlugin))
        {
            return null;
        }
        
        return (WorldGuardPlugin) plugin;
    }
    
    private Object getPrivateValue(Object obj, String name)
    {
        try
        {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(obj);
        }
        catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex)
        {
            return null;
        }
    }
}
