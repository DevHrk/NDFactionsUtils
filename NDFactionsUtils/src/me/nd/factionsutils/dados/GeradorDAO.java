package me.nd.factionsutils.dados;

import org.bukkit.Bukkit;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.gen.Gerador;
import me.nd.factionsutils.gen.GeradorAPI;

import java.util.ArrayList;
import java.util.List;

public class GeradorDAO {
    private static List<Gerador> geradors = new ArrayList<Gerador>();
    private static GeradorAPI geradorAPI = Main.get().getGeradorAPI();

    public static void loadaAllGeradores() {
        geradors.addAll(geradorAPI.findAll());
        Bukkit.getConsoleSender().sendMessage("§6[NDFactionUtils] §eTodos os geradores foram carregados com exito.");
    }

    public static Gerador getGerador(String loc) {
        return geradors.stream().filter(gerador -> gerador.getLocation().equals(loc)).findFirst().orElse(null);
    }

    public static List<Gerador> getallGeradores() {
        return geradors;
    }
}

