package me.nd.factionsutils.command;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.plugin.SConfig;

public class SetSpawn extends Commands {

	public SetSpawn() {
        super("olho");
    }
	static FileConfiguration m = Main.get().getConfig(); 
	public void perform(CommandSender s, String lb, String[] args) {
		    Player p = (Player)s;
		    SConfig m1 = Main.get().getConfig("Mensagens");
		    SConfig s1 = Main.get().getConfig("database","SetSpawn");
	         	if (p.hasPermission(m.getString("Permissões.SetSpawn"))) {
		        if (args.length == 0) {
		          p.sendMessage(m1.getString("Comando-Errado.SetSpawn").replace("&", "§"));
		        } else if (args.length == 1) {
		        	s1.set(String.valueOf("Spawn") + "..world", p.getWorld().getName());
		        	s1.set(String.valueOf("Spawn") + "..x", Double.valueOf(p.getLocation().getX()));
		        	s1.set(String.valueOf("Spawn") + "..y", Double.valueOf(p.getLocation().getY()));
		        	s1.set(String.valueOf("Spawn") + "..z", Double.valueOf(p.getLocation().getZ()));
		        	s1.set(String.valueOf("Spawn") + "..yaw", Float.valueOf(p.getLocation().getYaw()));
		        	s1.set(String.valueOf("Spawn") + "..pitch", Float.valueOf(p.getLocation().getPitch()));
		            s1.save();
		           p.sendMessage("§asetado com sucesso");
		        } 
		      } else {
		        p.sendMessage("§eprecisa ser §4gerente§e ou superior, para executar este comando!");
		      }  
		    return;
		  }
		}
