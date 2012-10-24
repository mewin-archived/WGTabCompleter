package com.mewin.WGTC.commands;

import com.mewin.WGCustomFlags.flags.CustomSetFlag;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class RegionCommand extends Command {
    private Command subCmd;
    private final Map<String, String> subCommands = new HashMap<>();
    private WorldGuardPlugin wgPlugin;
    
    public RegionCommand(Command cmd, WorldGuardPlugin wgPlugin)
    {
        super("region");
        this.subCmd = cmd;
        this.wgPlugin = wgPlugin;
        
        initSubCommands();
    }
    
    private void initSubCommands()
    {
        subCommands.put("addmember", "");
        subCommands.put("addowner", "");
        subCommands.put("claim", "claim");
        subCommands.put("define", "define");
        subCommands.put("flag", "");
        subCommands.put("info", "info");
        subCommands.put("list", "list");
        subCommands.put("redefine", "redefine");
        subCommands.put("remove", "");
        subCommands.put("removememeber", "");
        subCommands.put("select", "");
        subCommands.put("setparent", "");
        subCommands.put("setpriority", "");
        subCommands.put("teleport", "");
    }

    @Override
    public boolean execute(CommandSender cs, String string, String[] strings) {
        return this.subCmd.execute(cs, string, strings);
    }
    
    @Override
    public List<String> tabComplete(CommandSender cs, String alias, String[] params)
    {
        List<String> comps = new ArrayList<>();
        String cmdStart = "";
        
        if (!(cs instanceof Player))
        {
            return comps;
        }
        
        if (params.length < 2)
        {
            cmdStart = params[0];
            
            for(Entry<String, String> entry : subCommands.entrySet())
            {
                String cmd = entry.getKey();
                String perm = entry.getValue();
                
                if (perm.equals("") || cs.hasPermission("worldguard.region." + perm))
                {
                    comps.add(cmd);
                }
            }
        }
        else if (params.length < 3)
        {
            cmdStart = params[1];
            switch(params[0].toLowerCase())
            {
                case "redefine":
                case "select":
                case "info":
                case "i":
                case "addowner":
                case "addmember":
                case "removeowner":
                case "remowner":
                case "removemember":
                case "remmember":
                case "removemem":
                case "remmem":
                case "flag":
                case "f":
                case "setpriority":
                case "setparent":
                case "remove":
                    comps.addAll(completeRegionsForPlayer((Player) cs));
            }
        }
        else if (params.length < 4)
        {
            cmdStart = params[2];
            switch(params[0].toLowerCase())
            {
                case "addowner":
                case "addmember":
                case "removeowner":
                case "remowner:":
                case "removemember":
                case "remmember":
                case "removemem":
                    return super.tabComplete(cs, alias, params);
                case "setparent":
                    comps.addAll(completeRegionsForPlayer((Player) cs));
                    break;
                case "flag":
                case "f":
                    comps.addAll(completeFlagsForPlayer((Player) cs));
            }
        }
        else if (params.length < 5 && "flag".startsWith(params[0].toLowerCase()))
        {
            cmdStart = params[3];
            
            comps.addAll(completeFlagValue(params[2], cmdStart));
        }
        
        cmdStart = cmdStart.toLowerCase();
        Iterator<String> itr = comps.iterator();
        while (itr.hasNext())
        {
            if(!itr.next().toLowerCase().startsWith(cmdStart))
            {
                itr.remove();
            }
        }
        
        return comps;
    }
    
    private List<String> completeRegionsForPlayer(Player player)
    {
        List<String> comps = new ArrayList<>();
        RegionManager rm = wgPlugin.getRegionManager(player.getWorld());
        
        if (rm == null)
        {
            return comps; 
        }
        
        Map<String, ProtectedRegion> regions = rm.getRegions();
        
        if (player.isOp() || player.hasPermission("worldguard.region.list"))
        {
            for (Entry<String, ProtectedRegion> entry : regions.entrySet())
            {
                comps.add(entry.getKey());
            }
        }
        else
        {
           for (Entry<String, ProtectedRegion> entry : regions.entrySet())
            {
                ProtectedRegion region = entry.getValue();
                
                if (region.isOwner(player.getName()) || region.isMember(player.getName()))
                {
                    comps.add(entry.getKey());
                }
            } 
        }
        
        return comps;
    }
    
    private List<String> completeFlagsForPlayer(Player player)
    {
        List<String> comps = new ArrayList<>();
        
        Flag[] flags = DefaultFlag.flagsList;
        
        if (player.hasPermission("worldguard.region.flag.flags.*"))
        {
            for (Flag flag : flags)
            {
                comps.add(flag.getName());
            }
        }
        else
        {
            for (Flag flag : flags)
            {
                if (player.hasPermission("worldguard.region.flag." + flag.getName() + ".*"))
                {
                    comps.add(flag.getName());
                }
            }
        }
        
        return comps;
    }
    
    private List<String> completeFlagValue(String flagName, String cmdStart)
    {
        Flag flag = null;
        List<String> comps = new ArrayList<>();
        
        for (Flag f : DefaultFlag.flagsList)
        {
            if (f.getName().equalsIgnoreCase(flagName))
            {
                flag = f;
            }
        }
        
        if (flag == null)
        {
            return comps;
        }
        
        return completeFlagValue(flag, cmdStart);
    }
    
    private List<String> completeFlagValue(Flag flag, String cmdStart)
    {
        List<String> comps = new ArrayList<>();
        
        if (flag instanceof StateFlag)
        {
            comps.add("ALLOW");
            comps.add("DENY");
        }
        else if (flag instanceof EnumFlag)
        {
            Class enumClass = (Class) getPrivateValue(flag, "enumClass");
            
            Enum[] values = getEnumValues(enumClass);
            
            for (Enum value : values)
            {
                comps.add(value.name());
            }
        }
        else if (flag instanceof SetFlag || flag instanceof CustomSetFlag)
        {
            String newStart = cmdStart;
            String before = "";
            List<String> newComps;
            Flag subFlag = (Flag) getPrivateValue(flag, "subFlag");
            
            if (newStart.indexOf(",") >= 0)
            {
                before = newStart.substring(0, newStart.lastIndexOf(",")) + ",";
                newStart = newStart.substring(newStart.lastIndexOf(",") + 1);
            }
            
            newComps = completeFlagValue(subFlag, newStart);
            
            for (String newComp : newComps)
            {
                comps.add(before + newComp);
            }
        }
        
        return comps;
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
    
    private Enum[] getEnumValues(Class<? extends Enum> cls)
    {
        try {
            Method method = cls.getDeclaredMethod("values", new Class[0]);
            return (Enum[]) method.invoke(cls, new Object[0]);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            return null;
        }
    }
}
