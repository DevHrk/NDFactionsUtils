package me.nd.factionsutils.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.nd.factions.Main;
import me.nd.factionsutils.listeners.KitInventoryListener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Kit extends Commands {
    public final File pastaKits;
    private final File cooldownFile;
    private final YamlConfiguration cooldownConfig;
    public Map<UUID, String> editandoKits = new HashMap<>();

    public Kit() {
        super("kit", "kits", "kitt");
        pastaKits = new File(Main.get().getDataFolder(), "Kits");
        pastaKits.mkdirs();
        cooldownFile = new File(pastaKits.getParentFile(), "cooldowns.yml");
        cooldownConfig = loadCooldownConfig();
        editandoKits = new HashMap<>();
        new KitInventoryListener(this);
    }

    private YamlConfiguration loadCooldownConfig() {
        if (!cooldownFile.exists()) {
            try {
                cooldownFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(cooldownFile);
    }

    private void saveCooldownConfig() {
        try {
            cooldownConfig.save(cooldownFile);
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to save cooldowns.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                sender.sendMessage("§cUso: /kit <kit>");
                return;
            }
            sender.sendMessage("§cApenas jogadores podem abrir o menu de kits.");
            return;
        }

        if (args[0].toLowerCase().matches("recarregar|ekit|ckit|dkit|rkit|tkit|vperm|cckit")) {
            if (!sender.hasPermission("nd.kit.admin")) {
                sender.sendMessage("§cVocê não tem permissão para usar este comando.");
                return;
            }
        }

        switch (args[0].toLowerCase()) {
            case "recarregar":
                recarregarConfiguracoes(sender);
                break;
            case "ekit":
                if (args.length == 2) {
                    editarKit(sender, args[1], null, false);
                } else if (args.length == 3) {
                    try {
                        Long.parseLong(args[2]);
                        editarKit(sender, args[1], args[2], true);
                    } catch (NumberFormatException e) {
                        editarKit(sender, args[1], args[2], false);
                    }
                } else {
                    sender.sendMessage("§cUso: /kit editarkit <kit> [atraso|permissao]");
                }
                break;
            case "ckit":
                if (args.length == 2) {
                    criarKit(sender, args[1], "0", null);
                } else if (args.length == 3) {
                    criarKit(sender, args[1], args[2], null);
                } else if (args.length == 4) {
                    criarKit(sender, args[1], args[2], args[3]);
                } else {
                    sender.sendMessage("§cUso: /kit criarkit <kit> [atraso] [permissao]");
                }
                break;
            case "dkit":
                if (args.length > 1) deletarKit(sender, args[1]);
                else sender.sendMessage("§cUso: /kit deletarkit <kit>");
                break;
            case "vkit":
                if (args.length > 1) visualizarKit(sender, args[1]);
                else sender.sendMessage("§cUso: /kit verkit <kit>");
                break;
            case "rkit":
                if (args.length > 2) resetarAtrasoKit(sender, args[1], args[2]);
                else sender.sendMessage("§cUso: /kit resetkit <kit> <jogador>");
                break;
            case "tkit":
                if (args.length > 2) verificarAtrasoKit(sender, args[1], args[2]);
                else sender.sendMessage("§cUso: /kit tempokit <kit> <jogador>");
                break;
            case "vperm":
                if (args.length > 1) verPermissaoKit(sender, args[1]);
                else sender.sendMessage("§cUso: /kit verperm <kit>");
                break;
            case "cckit":
                if (args.length > 2) {
                    if (!(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
                        sender.sendMessage("§cEste comando só pode ser executado pelo console.");
                        return;
                    }
                    darKitConsole(sender, args[1], args[2]);
                } else {
                    sender.sendMessage("§cUso: /kit cckit <kit> <jogador>");
                }
                break;
            default:
                if (args.length == 1 && sender instanceof Player) {
                    darKit(sender, args[0], ((Player) sender).getName());
                } else if (args.length == 2) {
                    darKit(sender, args[0], args[1]);
                } else {
                    sender.sendMessage("§cUso: /kit <kit> [jogador]");
                }
                break;
        }
    }

    private void darKitConsole(CommandSender sender, String nomeKit, String nomeJogador) {
        Player alvo = Bukkit.getPlayer(nomeJogador);
        if (alvo == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return;
        }

        File arquivoKit = new File(pastaKits, nomeKit + ".yml");
        if (!arquivoKit.exists()) {
            sender.sendMessage("§cKit não encontrado.");
            return;
        }

        YamlConfiguration configKit = YamlConfiguration.loadConfiguration(arquivoKit);

        // Contar slots necessários para itens e armaduras
        List<ItemStack> itens = (List<ItemStack>) configKit.getList("itens");
        List<ItemStack> armadura = (List<ItemStack>) configKit.getList("armadura");

        // Handle inventory items
        if (itens != null) {
            for (ItemStack item : itens) {
                if (item != null) {
                    HashMap<Integer, ItemStack> sobrou = alvo.getInventory().addItem(item.clone());
                    if (!sobrou.isEmpty()) {
                        for (ItemStack itemSobrou : sobrou.values()) {
                            alvo.getWorld().dropItem(alvo.getLocation(), itemSobrou);
                        }
                    }
                }
            }
        }

        // Handle armor
        if (armadura != null) {
            ItemStack[] armaduraAtual = alvo.getInventory().getArmorContents();
            boolean temArmaduraEquipada = false;
            for (ItemStack armaduraItem : armaduraAtual) {
                if (armaduraItem != null && armaduraItem.getType() != org.bukkit.Material.AIR) {
                    temArmaduraEquipada = true;
                    break;
                }
            }

            if (!temArmaduraEquipada) {
                // No armor equipped, equip kit armor directly
                ItemStack[] novaArmadura = new ItemStack[4];
                for (int i = 0; i < Math.min(armadura.size(), 4); i++) {
                    novaArmadura[i] = armadura.get(i) != null ? armadura.get(i).clone() : null;
                }
                alvo.getInventory().setArmorContents(novaArmadura);
            } else {
                // Armor equipped, add kit armor to inventory
                for (ItemStack item : armadura) {
                    if (item != null) {
                        HashMap<Integer, ItemStack> sobrou = alvo.getInventory().addItem(item.clone());
                        if (!sobrou.isEmpty()) {
                            for (ItemStack itemSobrou : sobrou.values()) {
                                alvo.getWorld().dropItem(alvo.getLocation(), itemSobrou);
                            }
                        }
                    }
                }
            }
        }

        // Executar comandos
        List<String> comandos = configKit.getStringList("comandos");
        for (String cmd : comandos) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%jogador%", alvo.getName()));
        }

        sender.sendMessage("§eKit §f" + nomeKit + "§e dado para §f" + nomeJogador);
    }

    private void darKit(CommandSender sender, String nomeKit, String nomeJogador) {
        Player alvo = Bukkit.getPlayer(nomeJogador);
        if (alvo == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return;
        }

        File arquivoKit = new File(pastaKits, nomeKit + ".yml");
        if (!arquivoKit.exists()) {
            sender.sendMessage("§cKit não encontrado.");
            return;
        }

        YamlConfiguration configKit = YamlConfiguration.loadConfiguration(arquivoKit);
        String permissao = configKit.getString("permissao");
        if (permissao != null && !alvo.hasPermission(permissao)) {
            sender.sendMessage("§cVocê não tem permissão para pegar o kit §f" + nomeKit + "§c.");
            return;
        }

        long atraso = configKit.getLong("atraso", 0);
        String chave = alvo.getUniqueId() + "." + nomeKit;
        long cooldownEnd = cooldownConfig.getLong("cooldowns." + chave, 0);

        if (cooldownEnd > System.currentTimeMillis()) {
            long tempoRestante = cooldownEnd - System.currentTimeMillis();
            String tempoFormatado = formatarTempo(tempoRestante);
            sender.sendMessage("§cVocê deve esperar §f" + tempoFormatado + "§c para pegar o kit §f" + nomeKit + "§c novamente.");
            return;
        }

        // Contar slots necessários para itens e armaduras
        List<ItemStack> itens = (List<ItemStack>) configKit.getList("itens");
        List<ItemStack> armadura = (List<ItemStack>) configKit.getList("armadura");
        int slotsNecessarios = 0;
        if (itens != null) {
            for (ItemStack item : itens) {
                if (item != null) slotsNecessarios++;
            }
        }
        if (armadura != null) {
            for (ItemStack item : armadura) {
                if (item != null) slotsNecessarios++;
            }
        }

        // Verificar slots vazios no inventário
        int slotsVazios = 0;
        for (ItemStack item : alvo.getInventory().getContents()) {
            if (item == null || item.getType() == org.bukkit.Material.AIR) {
                slotsVazios++;
            }
        }
        for (ItemStack armor : alvo.getInventory().getArmorContents()) {
            if (armor == null || armor.getType() == org.bukkit.Material.AIR) {
                slotsVazios++;
            }
        }

        if (slotsVazios < slotsNecessarios) {
            sender.sendMessage("§cO inventário de §cestá lotado.");
            return;
        }

        // Handle inventory items
        if (itens != null) {
            for (ItemStack item : itens) {
                if (item != null) {
                    HashMap<Integer, ItemStack> sobrou = alvo.getInventory().addItem(item.clone());
                    if (!sobrou.isEmpty()) {
                        for (ItemStack itemSobrou : sobrou.values()) {
                            alvo.getWorld().dropItem(alvo.getLocation(), itemSobrou);
                        }
                    }
                }
            }
        }

        // Handle armor
        if (armadura != null) {
            ItemStack[] armaduraAtual = alvo.getInventory().getArmorContents();
            boolean temArmaduraEquipada = false;
            for (ItemStack armaduraItem : armaduraAtual) {
                if (armaduraItem != null && armaduraItem.getType() != org.bukkit.Material.AIR) {
                    temArmaduraEquipada = true;
                    break;
                }
            }

            if (!temArmaduraEquipada) {
                // No armor equipped, equip kit armor directly
                ItemStack[] novaArmadura = new ItemStack[4];
                for (int i = 0; i < Math.min(armadura.size(), 4); i++) {
                    novaArmadura[i] = armadura.get(i) != null ? armadura.get(i).clone() : null;
                }
                alvo.getInventory().setArmorContents(novaArmadura);
            } else {
                // Armor equipped, add kit armor to inventory
                for (ItemStack item : armadura) {
                    if (item != null) {
                        HashMap<Integer, ItemStack> sobrou = alvo.getInventory().addItem(item.clone());
                        if (!sobrou.isEmpty()) {
                            for (ItemStack itemSobrou : sobrou.values()) {
                                alvo.getWorld().dropItem(alvo.getLocation(), itemSobrou);
                            }
                        }
                    }
                }
            }
        }

        // Executar comandos
        List<String> comandos = configKit.getStringList("comandos");
        for (String cmd : comandos) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%jogador%", alvo.getName()));
        }

        // Aplicar cooldown se necessário
        if (atraso > 0) {
            long newCooldownEnd = System.currentTimeMillis() + atraso * 1000;
            cooldownConfig.set("cooldowns." + chave, newCooldownEnd);
            saveCooldownConfig();
        }

        sender.sendMessage("§eKit §f" + nomeKit + "§e dado para §f" + nomeJogador);
    }

    private String formatarTempo(long milissegundos) {
        if (milissegundos <= 0) {
            return "0s";
        }

        long segundosTotais = milissegundos / 1000;
        long dias = segundosTotais / 86400;
        long horas = (segundosTotais % 86400) / 3600;
        long minutos = (segundosTotais % 3600) / 60;
        long segundos = segundosTotais % 60;

        StringBuilder tempo = new StringBuilder();

        if (dias > 0) {
            tempo.append(dias).append("d");
            if (horas > 0 || minutos > 0 || segundos > 0) tempo.append(", ");
        }
        if (horas > 0) {
            tempo.append(horas).append("h");
            if (minutos > 0 || segundos > 0) tempo.append(", ");
        }
        if (minutos > 0) {
            tempo.append(minutos).append("m");
            if (segundos > 0) tempo.append(" e ");
        }
        if (segundos > 0 || (dias == 0 && horas == 0 && minutos == 0)) {
            tempo.append(segundos).append("s");
        }

        return tempo.toString();
    }

    public String getKitCooldown(Player player, String nomeKit) {
        File arquivoKit = new File(pastaKits, nomeKit + ".yml");
        if (!arquivoKit.exists()) {
            return "Kit não encontrado";
        }

        String chave = player.getUniqueId() + "." + nomeKit;
        long cooldownEnd = cooldownConfig.getLong("cooldowns." + chave, 0);

        if (cooldownEnd > System.currentTimeMillis()) {
            long tempoRestante = cooldownEnd - System.currentTimeMillis();
            return formatarTempo(tempoRestante);
        }
        return "Disponível";
    }

    public String getKitPermissionStatus(Player player, String nomeKit) {
        File arquivoKit = new File(pastaKits, nomeKit + ".yml");
        if (!arquivoKit.exists()) {
            return "Kit não encontrado";
        }

        YamlConfiguration configKit = YamlConfiguration.loadConfiguration(arquivoKit);
        String permissao = configKit.getString("permissao");
        if (permissao == null || permissao.isEmpty() || player.hasPermission(permissao)) {
            return "§aPossui";
        }
        return "§cNão possui";
    }

    private void criarKit(CommandSender sender, String nomeKit, String atrasoStr, String permissao) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem criar kits.");
            return;
        }

        Player jogador = (Player) sender;
        File arquivoKit = new File(pastaKits, nomeKit + ".yml");
        if (arquivoKit.exists()) {
            sender.sendMessage("§cKit já existe.");
            return;
        }

        long atraso = 0;
        if (atrasoStr != null) {
            try {
                atraso = Long.parseLong(atrasoStr);
                if (atraso < 0) {
                    sender.sendMessage("§cO delay deve ser um número não negativo.");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cO delay deve ser um número válido (em segundos).");
                return;
            }
        }

        try {
            arquivoKit.createNewFile();
            YamlConfiguration configKit = YamlConfiguration.loadConfiguration(arquivoKit);
            configKit.set("itens", jogador.getInventory().getContents());
            configKit.set("armadura", jogador.getInventory().getArmorContents());
            configKit.set("atraso", atraso);
            if (permissao != null) {
                configKit.set("permissao", permissao);
            }
            configKit.save(arquivoKit);
            String mensagem = "§eKit §f" + nomeKit + "§e criado com delay de §f" + formatarTempo(atraso * 1000);
            if (permissao != null) {
                mensagem += "§e e permissão §f" + permissao;
            }
            mensagem += "§e.";
            sender.sendMessage(mensagem);
        } catch (IOException e) {
            sender.sendMessage("§cErro ao criar o kit.");
            e.printStackTrace();
        }
    }

    private void editarKit(CommandSender sender, String nomeKit, String valor, boolean isAtraso) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem editar kits.");
            return;
        }

        File arquivoKit = new File(pastaKits, nomeKit + ".yml");
        if (!arquivoKit.exists()) {
            sender.sendMessage("§cKit não encontrado.");
            return;
        }

        Player jogador = (Player) sender;
        YamlConfiguration configKit = YamlConfiguration.loadConfiguration(arquivoKit);

        if (valor != null) {
            try {
                if (isAtraso) {
                    long atraso = Long.parseLong(valor);
                    if (atraso < 0) {
                        sender.sendMessage("§cO delay deve ser um número não negativo.");
                        return;
                    }
                    configKit.set("atraso", atraso);
                    configKit.save(arquivoKit);
                    sender.sendMessage("§eDelay do kit §f" + nomeKit + "§e atualizado para §f" + atraso + " segundos.");
                } else {
                    configKit.set("permissao", valor);
                    configKit.save(arquivoKit);
                    sender.sendMessage("§ePermissão do kit §f" + nomeKit + "§e atualizada para §f" + valor + "§e.");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cO atraso deve ser um número válido (em segundos).");
                return;
            } catch (IOException e) {
                sender.sendMessage("§cErro ao atualizar o kit.");
                e.printStackTrace();
                return;
            }
        }

        Inventory edicao = Bukkit.createInventory(jogador, 54, "Editar Kit: " + nomeKit);
        List<ItemStack> itens = (List<ItemStack>) configKit.getList("itens");
        if (itens != null) {
            for (ItemStack item : itens) {
                if (item != null) edicao.addItem(item.clone());
            }
        }
        editandoKits.put(jogador.getUniqueId(), nomeKit);
        jogador.openInventory(edicao);
    }

    private void deletarKit(CommandSender sender, String nomeKit) {
        File arquivoKit = new File(pastaKits, nomeKit + ".yml");
        if (!arquivoKit.exists()) {
            sender.sendMessage("§cKit não encontrado.");
            return;
        }

        if (arquivoKit.delete()) {
            sender.sendMessage("§aKit " + nomeKit + " deletado.");
        } else {
            sender.sendMessage("§cErro ao deletar o kit.");
        }
    }

    private void visualizarKit(CommandSender sender, String nomeKit) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem visualizar kits.");
            return;
        }

        File arquivoKit = new File(pastaKits, nomeKit + ".yml");
        if (!arquivoKit.exists()) {
            sender.sendMessage("§cKit não encontrado.");
            return;
        }

        YamlConfiguration configPlaceholder = YamlConfiguration.loadConfiguration(arquivoKit);
        Inventory visualizacao = Bukkit.createInventory(null, 54, "Visualizar Kit: " + nomeKit);
        List<ItemStack> itens = (List<ItemStack>) configPlaceholder.getList("itens");
        if (itens != null) {
            for (ItemStack item : itens) {
                if (item != null) visualizacao.addItem(item.clone());
            }
        }
        ((Player) sender).openInventory(visualizacao);
    }

    private void verificarAtrasoKit(CommandSender sender, String nomeKit, String nomeJogador) {
        Player alvo = Bukkit.getPlayer(nomeJogador);
        if (alvo == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return;
        }

        String chave = alvo.getUniqueId() + "." + nomeKit;
        long cooldownEnd = cooldownConfig.getLong("cooldowns." + chave, 0);

        if (cooldownEnd == 0) {
            sender.sendMessage("§eNão há delay para §f" + nomeJogador + "§e no kit §f" + nomeKit + "§e.");
            return;
        }

        long restante = cooldownEnd - System.currentTimeMillis();
        if (restante > 0) {
            sender.sendMessage("§eDelay restante para §f" + nomeJogador + "§e no kit §f" + nomeKit + "§e: §f" + formatarTempo(restante));
        } else {
            sender.sendMessage("§eNenhum delay para §f" + nomeJogador + "§e no kit §f" + nomeKit + "§e.");
            cooldownConfig.set("cooldowns." + chave, null);
            saveCooldownConfig();
        }
    }

    private void resetarAtrasoKit(CommandSender sender, String nomeKit, String nomeJogador) {
        Player alvo = Bukkit.getPlayer(nomeJogador);
        if (alvo == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return;
        }

        String chave = alvo.getUniqueId() + "." + nomeKit;
        cooldownConfig.set("cooldowns." + chave, null);
        saveCooldownConfig();
        sender.sendMessage("§eDelay de §f" + nomeJogador + "§e no kit §f" + nomeKit + "§e resetado.");
    }

    private void verPermissaoKit(CommandSender sender, String nomeKit) {
        File arquivoKit = new File(pastaKits, nomeKit + ".yml");
        if (!arquivoKit.exists()) {
            sender.sendMessage("§cKit não encontrado.");
            return;
        }

        YamlConfiguration configKit = YamlConfiguration.loadConfiguration(arquivoKit);
        String permissao = configKit.getString("permissao");
        if (permissao == null || permissao.isEmpty()) {
            sender.sendMessage("§eO kit §f" + nomeKit + "§e não possui uma permissão configurada.");
        } else {
            sender.sendMessage("§ePermissão do kit §f" + nomeKit + "§e: §f" + permissao);
        }
    }

    private void recarregarConfiguracoes(CommandSender sender) {
        sender.sendMessage("§aConfigurações recarregadas.");
    }
}