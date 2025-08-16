package me.nd.factionsutils.command;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import me.nd.factionsutils.listeners.itens.Potion;

public abstract class Commands extends Command {
	
	  public Commands(String name, String... aliases) {
		    super(name);
		    setAliases(Arrays.asList(aliases));
		    
		    try {
		      SimpleCommandMap simpleCommandMap = (SimpleCommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap").invoke(Bukkit.getServer());
		      simpleCommandMap.register(getName(), "nditens", this);
		    } catch (ReflectiveOperationException ex) {
		    	
		    }
		  }
	  
	  public static void setupCommands() {
		  new CriadorItem();
		  new GeradorCommand();
		  new Itemgive();
		  new SetSpawn();
		  new Cofre();
		  new Kit();
		  new ArmorsBonus();
		  new Beacon();
		  new Broadcast();
		  new Borda();
		  new Zeus();
		  new Rtp();
		  new Encantar();
		  new Reparar();
		  new Agrupar();
		  new Leilao();
		  new Potion();
	  }
	  
	  public abstract void perform(CommandSender sender, String label, String[] args);
	  
	  @Override
	  public boolean execute(CommandSender sender, String commandLabel, String[] args) {
	    perform(sender, commandLabel, args);
	    return true;
	  }
	
}
