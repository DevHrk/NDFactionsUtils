package me.nd.factionsutils.livros.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.nd.factions.Main;
import me.nd.factionsutils.livros.listener.*;

public class Listeners {
	public static void setupListener() {
	    try {
	        PluginManager pm = Bukkit.getPluginManager();
	        Main.get().getServer().getScheduler().scheduleSyncRepeatingTask(Main.get(), new Runnable() {
	            @Override
	            public void run() {
	                // Código que você quer executar a cada tick
	                for (Player p : Bukkit.getOnlinePlayers()) {
	                    if (ImpetoDeBatalhaListener.ultimoAtaque.containsKey(p)) {
	                        long tempoDesdeUltimoAtaque = System.currentTimeMillis() - ImpetoDeBatalhaListener.ultimoAtaque.get(p);
	                        if (tempoDesdeUltimoAtaque > ImpetoDeBatalhaListener.m.getInt("ImpetoDeBatalha.TempoDeInatividade") * 1000) {
	                        	ImpetoDeBatalhaListener.ataques.put(p, 0);
	                        }
	                    }
	                }
	            }
	        }, 0L, 1L); // 0L é o delay inicial, 1L é o intervalo entre as execuções
	        Main.get().getServer().getScheduler().scheduleSyncRepeatingTask(Main.get(), new Runnable() {
	            @Override
	            public void run() {
	            	ProtecaoDivinaListener.ataquesSimultaneos.clear();
	            }
	        }, 0L, 1L);
	        Listener[] listeners = {
	            new DerreterListener(),
	            new SobrecargaListener(),
	            new RagnarokListener(),
	            new ShieldListener(),
	            new AspectoListener(),
	            new RajadaListener(),
	            new FuriaListener(),
	            new AutoRepararListener(),
	            new ExecucaoListener(),
	            new SuperAreaListener(),
	            new ImpetoDeBatalhaListener(),
	            new ProtecaoDivinaListener(),
	            new DurezaListener(),
	            new LivroAleatorioListener(),
	        };

	        for (Listener listener : listeners) {
	            registerEvent(pm, listener);
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}

	private static void registerEvent(PluginManager pm, Listener listener) {
	    try {
	        pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, listener, Main.get());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
