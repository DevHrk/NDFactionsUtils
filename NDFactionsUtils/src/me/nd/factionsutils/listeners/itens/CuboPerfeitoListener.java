package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.hologram.BukkitUtils;
import me.nd.factionsutils.hologram.IArmorStand;
import me.nd.factionsutils.hologram.NMS;
import me.nd.factionsutils.itens.CuboPerfeito;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class CuboPerfeitoListener implements Listener {

    private FileConfiguration m = Main.get().getConfig();
    private HashMap<UUID, CubeData> activeCubes = new HashMap<>(); // Armazena os cubos ativos
    private HashMap<UUID, List<IArmorStand>> playerHeads = new HashMap<>();


    // Classe para armazenar os dados de cada cubo
    private class CubeData {
        public Location center;
        public int size;
        public BukkitRunnable cubeOutlineTask;
        public BukkitTask countdownTask;
    }
    
    
    @EventHandler
    void onClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        UUID playerUUID = p.getUniqueId(); // Obtém o UUID do jogador
        
        SConfig m1 = Main.get().getConfig("Mensagens");
        List<String> allowedWorlds = m.getStringList("CuboPerfeito.MundosPermitidos");
        if (p.getInventory().getItemInHand().isSimilar(CuboPerfeito.AGRUPADOR) && !Utils.isZonaProtegida(p.getLocation())) {
            p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
            e.setCancelled(true);
            return;
        }
        
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (p.getItemInHand().isSimilar(CuboPerfeito.AGRUPADOR)) {
                if (allowedWorlds.contains(p.getWorld().getName())) {
                    e.setCancelled(true);

                    // Desativa o cubo anterior do jogador (se houver)
                    deactivateCube(playerUUID); 

                    // Lançar o item
                    Vector direction = p.getLocation().getDirection().normalize().multiply(1.5);
                    Item item = p.getWorld().dropItem(p.getEyeLocation().add(direction), CuboPerfeito.AGRUPADOR);
                    item.setVelocity(direction);

                    // Remover o item da mão do jogador
                    if (p.getItemInHand().getAmount() > 1) {
                        p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                    } else {
                        p.setItemInHand(null);
                    }

                    // Monitorar o item lançado
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Verificar se o item ainda existe
                            if (item.isDead()) {
                                cancel();
                                return;
                            }

                            // Verificar se atingiu alguma coisa (chão ou entidade)
                            if (item.isOnGround() || item.getVelocity().length() < 0.1) { 
                                activateCube(p, item.getLocation(), m.getInt("CuboPerfeito.Raio"), m.getInt("CuboPerfeito.Tempo"));
                                item.remove();
                                MessageUtils.send(p, m1.getStringList("CuboP.Ativada"));
                                cancel();
                            }
                        }
                    }.runTaskTimer(Main.get(), 1L, 1L); 
                } else {
                    MessageUtils.send(p, m1.getStringList("CuboP.MundoNaoPermitido"));
                }
            }
        }
    }


    private void activateCube(Player p, Location center, int size, int durationSeconds) {
        UUID playerUUID = p.getUniqueId();

        if (activeCubes.containsKey(playerUUID)) {
            deactivateCube(playerUUID); 
        }

        CubeData cubeData = new CubeData();
        cubeData.center = center;
        cubeData.size = size;

        createCubeCorners(center, size, playerUUID);
        createCubeOutline(cubeData); 

        // Agenda a desativação com o tempo restante
        cubeData.countdownTask = Bukkit.getScheduler().runTaskTimer(Main.get(), new Runnable() {
            int currentTime = 0; // Define o tempo atual como 0

            @Override
            public void run() {
                // Aqui você pode adicionar lógica para cada segundo que passa, se necessário
                // por exemplo: enviar uma mensagem ao jogador a cada segundo.

                currentTime++;

                if (currentTime >= durationSeconds) {
                    deactivateCube(playerUUID); 
                }
            }
        }, 0L, 20L); // Executa a cada segundo (20 ticks)

        activeCubes.put(playerUUID, cubeData);
    }
    
    private void deactivateCube(UUID playerUUID) {
        CubeData cubeData = activeCubes.remove(playerUUID);
        if (cubeData != null) {
            cubeData.center = null;
            cubeData.size = 0;
            
            if (cubeData.cubeOutlineTask != null) {
                cubeData.cubeOutlineTask.cancel();
                cubeData.cubeOutlineTask = null;
            }
            
            if (cubeData.countdownTask != null) {
                cubeData.countdownTask.cancel();
                cubeData.countdownTask = null;
            }
            
            List<IArmorStand> headsToRemove = playerHeads.remove(playerUUID);
            if (headsToRemove != null) {
                for (IArmorStand head : headsToRemove) {
                    head.getEntity().remove();
                }
            }
        }
    }
    
    private void createCubeCorners(Location center, int size,UUID playerUUID) {
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                for (int z = -1; z <= 1; z += 2) {
                    Location corner = center.clone().add(x * size / 2.0, y * size / 2.0, z * size / 2.0);
                    spawnHead(corner, playerUUID);
                }
            }
        }
    }

    private void createCubeOutline(CubeData cubeData) { // Recebe o CubeData como parâmetro
        cubeData.cubeOutlineTask = new BukkitRunnable() { 
            @Override
            public void run() {
                drawCubeOutline(cubeData.center, cubeData.size); // Acessa os dados
            }
         };
         cubeData.cubeOutlineTask.runTaskTimer(Main.get(), 0L, 60L);
     }
    private void drawCubeOutline(Location center, int size) {
        // Altura adicional para as partículas spawnarem acima do jogador
        double offsetY = 2.0; // Ajuste este valor conforme necessário

        for (double i = 0; i <= size; i += 0.2) {
            // Lines along X axis
            spawnParticle(center.clone().add(i - size / 2.0, -size / 2.0 + offsetY, -size / 2.0));
            spawnParticle(center.clone().add(i - size / 2.0, size / 2.0 + offsetY, -size / 2.0));
            spawnParticle(center.clone().add(i - size / 2.0, -size / 2.0 + offsetY, size / 2.0));
            spawnParticle(center.clone().add(i - size / 2.0, size / 2.0 + offsetY, size / 2.0));

            // Lines along Y axis
            spawnParticle(center.clone().add(-size / 2.0, i - size / 2.0 + offsetY, -size / 2.0));
            spawnParticle(center.clone().add(size / 2.0, i - size / 2.0 + offsetY, -size / 2.0));
            spawnParticle(center.clone().add(-size / 2.0, i - size / 2.0 + offsetY, size / 2.0));
            spawnParticle(center.clone().add(size / 2.0, i - size / 2.0 + offsetY, size / 2.0));

            // Lines along Z axis
            spawnParticle(center.clone().add(-size / 2.0, -size / 2.0 + offsetY, i - size / 2.0));
            spawnParticle(center.clone().add(size / 2.0, -size / 2.0 + offsetY, i - size / 2.0));
            spawnParticle(center.clone().add(-size / 2.0, size / 2.0 + offsetY, i - size / 2.0));
            spawnParticle(center.clone().add(size / 2.0, size / 2.0 + offsetY, i - size / 2.0));
        }
    }
    private boolean isInsideCube(Location point, Location center, int size) {
    	  double xMin = center.getX() - size / 2.0;
    	  double xMax = center.getX() + size / 2.0;
    	  double yMin = center.getY() - size / 2.0;
    	  double yMax = center.getY() + size / 2.0;
    	  double zMin = center.getZ() - size / 2.0;
    	  double zMax = center.getZ() + size / 2.0;

    	  return point.getX() >= xMin && point.getX() <= xMax &&
    	       point.getY() >= yMin && point.getY() <= yMax &&
    	       point.getZ() >= zMin && point.getZ() <= zMax;
    	}
    
    @EventHandler
    public void onEntityMove(PlayerMoveEvent event) {
            Player p = event.getPlayer(); // Obtém o jogador do evento
            // Itera pelos cubos ativos
            for (CubeData cubeData : activeCubes.values()) {
                if (cubeData.center != null) {
                    Location to = event.getTo();
                    Location from = event.getFrom();

                    if (isInsideCube(from, cubeData.center, cubeData.size) && !isInsideCube(to, cubeData.center, cubeData.size) || 
                        !isInsideCube(from, cubeData.center, cubeData.size) && isInsideCube(to, cubeData.center, cubeData.size)) {
                        p.teleport(from); // Teleporta apenas o jogador do evento
                    }
                }
            }
            for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
                if (!(entity instanceof Player)) { 
                    for (CubeData cube : activeCubes.values()) {
                        if (cube.center != null && entity.getLocation() != null && entity.getWorld() == cube.center.getWorld()) {
                            // Calcula a distância da entidade ao centro do cubo
                            double distanceToCenter = entity.getLocation().distance(cube.center);

                            // Calcula a distância da superfície do cubo ao centro
                            double surfaceDistance = cube.size / 2.0;

                            // Define uma margem de erro para fora do cubo
                            double outerMargin = 1.0; // Ajuste este valor conforme necessário

                            // Verifica se a entidade está dentro do cubo ou na margem externa
                            if (distanceToCenter <= surfaceDistance + outerMargin) {
                                // Define uma pequena margem de erro para a colisão
                                double collisionMargin = 0.5;

                                // Verifica se a distância da entidade ao centro está próxima da distância da superfície
                                if (Math.abs(distanceToCenter - surfaceDistance) <= collisionMargin) {
                                    handleEntityInsideCube(entity, cube.center, cube.size);
                                    entity.setVelocity(new Vector(0, 0, 0)); 
                                }
                            }
                        }
                    }
                }
            }
    }

    private void handleEntityInsideCube(Entity entity, Location center, int size) {
        Vector direction = entity.getLocation().toVector().subtract(center.toVector());
        
        // Se a direção for zero (entidade no centro ou sem movimento), escolha uma direção aleatória
        if (direction.lengthSquared() < 0.0001) {
            direction = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
        }
        
        direction = direction.normalize();
        
        double distanciaSegura = (size / 2.0) - 2; // Adiciona 1 bloco extra para garantir que está fora
        Location outsideCube = center.clone().add(direction.multiply(distanciaSegura));
        
        // Garante que a localização é segura para teleporte
        Location localizacaoSegura = encontrarLocalizacaoSegura(outsideCube);
        
        // Teleporta a entidade
        entity.teleport(localizacaoSegura);
    }

    private Location encontrarLocalizacaoSegura(Location location) {
        int maxAttempts = 10;
        
        for (int i = 0; i < maxAttempts; i++) {
            if (location.getBlock().getType().isBlock() && 
                location.add(0, 1, 0).getBlock().getType().isBlock()) {
                return location.subtract(0, 1, 0); // Volta para a posição original
            }
            location = location.add(0, 1, 0); // Tenta um bloco acima
        }
        
        // Se não encontrar um local seguro, retorna a localização original
        return location;
    }

            private void spawnHead(Location location, UUID playerUUID) { // Adicione playerUUID como parâmetro
                IArmorStand head = NMS.createArmorStand(location.clone().add(0, 1, 0), null, null);
                head.getEntity().setHelmet(BukkitUtils.deserializeItemStack("SKULL_ITEM:3 : 1 : skin>eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRiNWNlMGQ0NGMzZTgxMzhkYzJlN2U1MmMyODk3YmI4NzhlMWRiYzIyMGQ3MDY4OWM3YjZiMThkMzE3NWUwZiJ9fX0="));
                head.getEntity().setGravity(false);
                head.getEntity().setSmall(true);
                head.getEntity().setHeadPose(new EulerAngle(0, 0, 0));

                // Adiciona a cabeça à lista do jogador correspondente
                playerHeads.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(head); 
            }


    private void spawnParticle(Location location) {
        location.getWorld().playEffect(location, Effect.HAPPY_VILLAGER,  0); 
    }
}
