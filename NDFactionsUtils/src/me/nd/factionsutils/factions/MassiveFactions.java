package me.nd.factionsutils.factions;

import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class MassiveFactions {

	static SConfig m1 = Main.get().getConfig("Mensagens");
	 static FileConfiguration m = Main.get().getConfig(); 
	 
	public static boolean resetKDR(Player p) {
		NDPlayer mp = DataManager.players.get(p.getName());
		if (mp.getKills() > 0 || mp.getMortes() > 0) {
			mp.setKills(0);
			mp.setMortes(0);
			MessageUtils.send(p, m1.getStringList("Kdr.ResetadoComSucesso").stream().map(message -> message.replace("%poder%", String.valueOf(mp.getPoder())).replace("%maxpoder%", String.valueOf(mp.getPodermax())).replace("%maximoPoder%", String.valueOf(m.getInt("Poder_Maximo.LimiteMaximoDePoder")))).collect(Collectors.toList()));
			return true;
		} else {
	        MessageUtils.send(p, m1.getStringList("Kdr.JaEstaResetado").stream().map(message -> message.replace("%poder%", String.valueOf(mp.getPoder())).replace("%maxpoder%", String.valueOf(mp.getPodermax())).replace("%maximoPoder%", String.valueOf(m.getInt("Poder_Maximo.LimiteMaximoDePoder")))).collect(Collectors.toList()));
			return false;
		}
	}

	public static boolean upPower(Player p) {
		NDPlayer mp = DataManager.players.get(p.getName());
		int poderatual = mp.getPoder();
		if (poderatual >= mp.getPodermax()) {
	        MessageUtils.send(p, m1.getStringList("PoderInsta.LimiteAtingido").stream().map(message -> message.replace("%poder%", String.valueOf(mp.getPoder())).replace("%maxpoder%", String.valueOf(mp.getPodermax())).replace("%maximoPoder%", String.valueOf(m.getInt("Poder_Maximo.LimiteMaximoDePoder")))).collect(Collectors.toList()));
			return false;
		} else {
			mp.setPoder((int) (poderatual + 1.0));
            MessageUtils.send(p, m1.getStringList("PoderInsta.UsadoComSucesso").stream().map(message -> message.replace("%poder%", String.valueOf(mp.getPoder())).replace("%maxpoder%", String.valueOf(mp.getPodermax())).replace("%maximoPoder%", String.valueOf(m.getInt("Poder_Maximo.LimiteMaximoDePoder")))).collect(Collectors.toList()));
			return true;
		}
	}

	public static boolean upMaxPower(Player p) {
		NDPlayer mp = DataManager.players.get(p.getName());
		int maximo = mp.getPodermax();
		if (maximo >= m.getInt("Poder_Maximo.LimiteMaximoDePoder")) {
	        MessageUtils.send(p, m1.getStringList("PowerMax.LimiteAtingido").stream().map(message -> message.replace("%poder%", String.valueOf(mp.getPoder())).replace("%maxpoder%", String.valueOf(mp.getPodermax())).replace("%maximoPoder%", String.valueOf(m.getInt("Poder_Maximo.LimiteMaximoDePoder")))).collect(Collectors.toList()));
			return false;
		} else {
			mp.setPodermax((int) (mp.getPodermax() + 1.0));
	        MessageUtils.send(p, m1.getStringList("PowerMax.UsadoComSucesso").stream().map(message -> message.replace("%poder%", String.valueOf(mp.getPoder())).replace("%maxpoder%", String.valueOf(mp.getPodermax())).replace("%maximoPoder%", String.valueOf(m.getInt("Poder_Maximo.LimiteMaximoDePoder")))).collect(Collectors.toList()));
			return true;
		}
	}

}
