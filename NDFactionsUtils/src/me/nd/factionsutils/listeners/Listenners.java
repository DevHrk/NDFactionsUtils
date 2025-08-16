package me.nd.factionsutils.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.command.Cofre;
import me.nd.factionsutils.command.Reparar;
import me.nd.factionsutils.listeners.itens.*;
import me.nd.factionsutils.listeners.player.PlayerPreventEvents;

public class Listenners {

	
	public static void setupListeners() {
	    try {
	        PluginManager pm = Bukkit.getPluginManager();

	        Listener[] listeners = {
	            new GodChestPlateListener(),
	            new PlacaListener(),
	            new RelogioDoTempoListener(),
	            new RemoverAttackListener(),
	            new BoxListenner(),
	            new ArmadilhaListener(),
	            new OlhoDeDeusListener(),
	            new SalvacaoListener(),
	            new AspectoListener(),
	            new SuperSocoListener(),
	            new CabecaHydraListener(),
	            new MuralhaListener(),
	            new GeradorListener(),
	            new GosmaListener(),
	            new CriarListener(),
	            new ReparadorListener(),
	            new RaioListener(),
	            new PurificadorListener(),
	            new PicaretaListener(),
	            new DetectListener(),
	            new PoderInstaListener(),
	            new PoderMaxListene(),
	            new CaldeiraoListener(),
	            new CoordListener(),
	            new TntRadListener(),
	            new ResetListener(),
	            new TotemListener(),
	            new SaveLifeListener(),
	            new RegeneradorListener(),
	            new LauncherListener(),
	            new TntThrowListener(),
	            new SlimeBlockJumpListener(),
	            new MeteorListener(),
	            new GeneratorListener(),
	            new ChunkClearListener(),
	            new TrackerListener(),
	            new IceWarListener(),
	            new MaquinaDoTempoListener(),
	            new AntiTrapListener(),
	            new CataclistListener(),
	            new InversorListener(),
	            new PlayerPreventEvents(),
	            new SchematicListenner(),
	            new CreeperListener(),
	            new Cofre(),
	            new RelogioDeRedstoneListener(),
	            new BlazeListener(),
	            new MagmaListener(),
	            new BeaconListener(),
	            new DropListener(),
	            new ZeusListener(),
	            new CuboPerfeitoListener(),
	            new BloquearPigZombie(),
	            new Reparar(),
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
