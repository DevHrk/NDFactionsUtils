package me.nd.factionsutils.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.TitleAPI;

import java.util.Random;

public class Zeus extends Commands {

    public Zeus() {
        super("zeus");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        // Verifica se o sender é o console
        if (!(sender instanceof Player)) {
            // Verifica se há argumento do mundo
            if (args.length != 1) {
                sender.sendMessage("§cUso: /zeus <mundo>");
                return;
            }

            // Obtém o mundo
            String worldName = args[0];
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage("§cMundo '" + worldName + "' não encontrado!");
                return;
            }

            // Aplica efeitos visuais e sonoros
            applyZeusEffects(world);

            // Escolhe um evento aleatório
            Random random = new Random();
            int event = random.nextInt(4) + 1; // 1 a 4
            String eventMessage;
            switch (event) {
                case 1:
                    eventMessage = "§eShift na água você ganha impulso!";
                    Main.get().setZeusEvent("water_boost");
                    break;
                case 3:
                    eventMessage = "§eZeus removeu a física da areia e cascalho! Eles não caem mais!";
                    Main.get().setZeusEvent("no_gravity_sand");
                    break;
                case 4:
                    eventMessage = "§eZeus inverteu as regras! Água agora causa dano, e lava é segura!";
                    Main.get().setZeusEvent("water_lava_swap");
                    break;
                default:
                    eventMessage = "§eZeus bugou, peraí que já conserta!";
                    Main.get().setZeusEvent("none");
            }

            // Envia mensagem do evento
            Bukkit.broadcastMessage(eventMessage);
        } else {
            sender.sendMessage("§cEste comando só pode ser usado pelo console!");
        }
    }

    private void applyZeusEffects(World world) {
    	for (Player player : Bukkit.getOnlinePlayers()) {
        TitleAPI.sendTitle(player, 20, 30, 20, "§4§lZeus", "§cAlterou as Leis do mundo!");
    	}

        // Efeitos para todos os jogadores online
        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 100; // 5 segundos (20 ticks/segundo)

            @Override
            public void run() {
                if (ticks >= duration) {
                    // Remove cegueira no final
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                    }
                    cancel();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Trovões
                    player.getWorld().strikeLightningEffect(player.getLocation());
                    // Cegueira intermitente (liga/desliga a cada 10 ticks)
                    if (ticks % 20 == 0) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1, false, false));
                    } else if (ticks % 20 == 10) {
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                    }
                }
                ticks += 10;
            }
        }.runTaskTimer(Main.get(), 0L, 10L); // Executa a cada 0.5 segundos
    }

}