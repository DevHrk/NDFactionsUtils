package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.hologram.BukkitUtils;
import me.nd.factionsutils.hologram.IArmorStand;
import me.nd.factionsutils.hologram.NMS;
import me.nd.factionsutils.itens.GodChestPlate;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class GodChestPlateListener implements Listener {
	
	private Map<Player, BukkitTask> spinningHeadsTasks = new HashMap<>();
	private Map<Player, BukkitTask> liftPlayerTasks = new HashMap<>();
	private Map<Player, BukkitTask> tasks = new HashMap<>();
	private Map<Player, Boolean> isTaskRunning = new HashMap<>();
	private Map<Player, Boolean> isTaskActivate = new HashMap<>();
	private Map<Player, Long> shiftStartTimes = new HashMap<>();
	private Map<Player, Long> cooldowns = new HashMap<>();
	private Map<Player, List<IArmorStand>> playerHeads = new HashMap<>();
    private Map<Player, Long> taskTimes = new HashMap<>();
    private static GodChestPlateListener instance;
    public static ArrayList<String> godchest = new ArrayList<String>();
    private Map<Player, Boolean> isInsideArea = new HashMap<>();
    FileConfiguration m = Main.get().getConfig(); 
    
    public static GodChestPlateListener getInstance() {
        if (instance == null) {
            instance = new GodChestPlateListener();
        }
        return instance;
    }
    
    @EventHandler
    private void onPlayerToggleSneakEvent(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        SConfig m1 = Main.get().getConfig("Mensagens");
        if (p != null) {
            ItemStack chestplate = p.getInventory().getChestplate();
            if (chestplate != null && GodChestPlate.ARMADILHA != null && chestplate.isSimilar(GodChestPlate.ARMADILHA)) {
                if (e.isSneaking()) {
                    // Player started sneaking, store the start time
                    shiftStartTimes.put(p, System.currentTimeMillis());
                } else {
                    // Player released shift key, check if they held it for x seconds
                    Long startTime = shiftStartTimes.get(p);
                    if (startTime != null) {
                        long currentTime = System.currentTimeMillis();
                        long shiftDuration = currentTime - startTime;
                        if (shiftDuration >= m.getInt("GodChestPlate.Shift")) { // 2000ms = 2 seconds
                            // Verifique se o jogador está em cooldown antes de iniciar a tarefa
                            if (cooldowns.containsKey(p)) {
                                long cooldownEndTime = cooldowns.get(p);
                                if (System.currentTimeMillis() < cooldownEndTime) {
                                    // O jogador está em cooldown, não permita que ele inicie a tarefa novamente
                                	MessageUtils.send(p, m1.getString("GodChestPlate.Cooldown").replace("{tempo}", API.formatTime((cooldownEndTime - System.currentTimeMillis()) / 1L)));
                                    return;
                                } else {
                                    // O cooldown expirou, remova o jogador do mapa
                                    cooldowns.remove(p);
                                }
                            }
                            if (isTaskActivate.getOrDefault(p, false)) {
                                MessageUtils.send(p, m1.getString("GodChestPlate.JaAtivado"),m1.getStringList("GodChestPlate.JaAtivado"));
                                return;
                            }
                            // Player held shift for at least 2 seconds, start tasks
                            if (!isTaskRunning.getOrDefault(p, false)) {
                                LiftPlayerTask liftPlayerTask = new LiftPlayerTask(p);
                                SpinningHeadsTask spinningHeadTask = new SpinningHeadsTask(p);

                                BukkitTask liftTask = Bukkit.getScheduler().runTaskTimer(Main.get(), liftPlayerTask, 0, 1);
                                BukkitTask spinningTask = Bukkit.getScheduler().runTaskTimer(Main.get(), spinningHeadTask, 0, 1);

                                liftPlayerTasks.put(p, liftTask);
                                spinningHeadsTasks.put(p, spinningTask);
                                isTaskRunning.put(p, true);
                                taskTimes.put(p, System.currentTimeMillis());
                                p.setHealth(m.getDouble("GodChestPlate.VidaComeço"));
                                p.setMaxHealth(m.getDouble("GodChestPlate.VidaComeço"));
                                API.dispatchCommands(p, m.getStringList("GodChestPlate.RemoverEfeitos"));
                                MessageUtils.send(p, m1.getString("GodChestPlate.Desativar"),m1.getStringList("GodChestPlate.Desativar"));
                                for (Entity s : p.getNearbyEntities(
                       		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue(), 
                       		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue(), 
                       		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue())) {
                                   	if (s instanceof Player) {
                                   	Player p1 = (Player)s;
                                    MessageUtils.send(p1, m1.getString("GodChestPlate.Ativando"),m1.getStringList("GodChestPlate.Ativando"));
                                   	}
                                }
                            }
                        }
                        shiftStartTimes.remove(p); // Remove the start time after checking
                    }
                }
            }
        }
    }

    @EventHandler
    private void onPlayerMoveEvent(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        SConfig m1 = Main.get().getConfig("Mensagens");
        if (isTaskRunning.getOrDefault(p, false) && e.getPlayer().isSneaking()) { // Verificar se o jogador está pressionando shift
            // Cancel the task only for the player who moved
            Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
                @Override
                public void run() {
                for (IArmorStand head : playerHeads.get(p)) {
                    head.killEntity();
                }
                playerHeads.get(p).clear();
                }
            }, 1); // 10 ticks = 0.5 segundos
            isTaskRunning.remove(p); // Remove the player from the isTaskRunning map
            p.setAllowFlight(false);
            p.setMaxHealth(20);
            p.setHealth(20);
            taskTimes.remove(p);
            cancelTasks(p);
            cancelTask(p);
            isTaskActivate.remove(p);
            godchest.remove(p.getName());
            // Remove the static reference to SpinningHeadsTask
            SpinningHeadsTask task = getSpinningHeadsTask(p);
            if (task != null) {
                task.setTime(0);
            }
            
        }
        if (isTaskRunning.getOrDefault(p, false) && spinningHeadsTasks.get(p) != null && System.currentTimeMillis() - taskTimes.get(p) >= 5000) {
            // Cancel the task only for the player who moved
            isTaskRunning.remove(p); // Remove the player from the isTaskRunning map
            p.setAllowFlight(false);
            p.setMaxHealth(20);
            p.setHealth(20);
            isTaskActivate.remove(p);
            for (Entity s : p.getNearbyEntities(
    		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue(), 
    		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue(), 
    		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue())) {
   if (s instanceof Player) {
       Player p1 = (Player)s;
       if (!isInsideArea.containsKey(p1) ||!isInsideArea.get(p1)) {
           isInsideArea.put(p1, true);
           MessageUtils.send(p1, m1.getString("GodChestPlate.Desativado"), m1.getStringList("GodChestPlate.Desativado"));
       }
   }
            }
            cancelTasks(p);
            cancelTask(p);
            taskTimes.remove(p);
            godchest.remove(p.getName());
            Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
                @Override
                public void run() {
                for (IArmorStand head : playerHeads.get(p)) {
                    head.killEntity();
                }
                playerHeads.get(p).clear();
                }
            }, 1); // 10 ticks = 0.5 segundos
            // Remove the static reference to SpinningHeadsTask
            SpinningHeadsTask task = getSpinningHeadsTask(p);
            if (task != null) {
                task.setTime(0);
            }
        }
    }
    

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
    	Player p = (Player)e.getEntity();
    	SConfig m1 = Main.get().getConfig("Mensagens");
    	if (isTaskRunning.getOrDefault(p, false)) {
    		isTaskActivate.remove(p);
            Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
                @Override
                public void run() {
                for (IArmorStand head : playerHeads.get(p)) {
                    head.killEntity();
                }
                playerHeads.get(p).clear();
                }
            }, 1); // 10 ticks = 0.5 segundos
            Bukkit.getScheduler().runTaskLater(Main.get(), () ->  {
            	p.setMaxHealth(20);
            },2);
            isTaskRunning.remove(p); // Remove the player from the isTaskRunning map
            p.setAllowFlight(false);
            taskTimes.remove(p);
            godchest.remove(p.getName());
            for (Entity s : p.getNearbyEntities(
    		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue(), 
    		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue(), 
    		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue())) {
   if (s instanceof Player) {
       Player p1 = (Player)s;
       if (!isInsideArea.containsKey(p1) ||!isInsideArea.get(p1)) {
           isInsideArea.put(p1, true);
           MessageUtils.send(p1, m1.getString("GodChestPlate.Morreu"), m1.getStringList("GodChestPlate.Morreu"));
       }
   }
            }
            cancelTasks(p);
            cancelTask(p);
            // Remove the static reference to SpinningHeadsTask
            SpinningHeadsTask task = getSpinningHeadsTask(p);
            if (task != null) {
                task.setTime(0);
            }
       }
    }
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
    	Player p = e.getPlayer();
    	if (isTaskRunning.getOrDefault(p, false)) {
    		isTaskActivate.remove(p);
            Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
                @Override
                public void run() {
                for (IArmorStand head : playerHeads.get(p)) {
                    head.killEntity();
                }
                playerHeads.get(p).clear();
                }
            }, 1); // 10 ticks = 0.5 segundos
            Bukkit.getScheduler().runTaskLater(Main.get(), () ->  {
            	p.setMaxHealth(20);
            },2);
            isTaskRunning.remove(p); // Remove the player from the isTaskRunning map
            p.setAllowFlight(false);
            taskTimes.remove(p);
            godchest.remove(p.getName());
            cancelTasks(p);
            cancelTask(p);
            // Remove the static reference to SpinningHeadsTask
            SpinningHeadsTask task = getSpinningHeadsTask(p);
            if (task != null) {
                task.setTime(0);
            }
       }
    }
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
    	Player p = e.getPlayer();
    	if (isTaskRunning.getOrDefault(p, false)) {
    		isTaskActivate.remove(p);
            Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
                @Override
                public void run() {
                for (IArmorStand head : playerHeads.get(p)) {
                    head.killEntity();
                }
                playerHeads.get(p).clear();
                }
            }, 1); // 10 ticks = 0.5 segundos
            Bukkit.getScheduler().runTaskLater(Main.get(), () ->  {
            	p.setMaxHealth(20);
            },2);
            isTaskRunning.remove(p); // Remove the player from the isTaskRunning map
            p.setAllowFlight(false);
            taskTimes.remove(p);
            godchest.remove(p.getName());
            cancelTasks(p);
            cancelTask(p);
            // Remove the static reference to SpinningHeadsTask
            SpinningHeadsTask task = getSpinningHeadsTask(p);
            if (task != null) {
                task.setTime(0);
            }
       }
    }    
    // Add a method to get the SpinningHeadsTask for a player
    private SpinningHeadsTask getSpinningHeadsTask(Player player) {
        BukkitTask task = spinningHeadsTasks.get(player);
        if (task instanceof SpinningHeadsTask) {
            return (SpinningHeadsTask) task;
        }
        return null;
    }

    private class SpinningHeadsTask extends BukkitRunnable {
        private Player player;
        private List<IArmorStand> heads;
        private int time = 0;

        public SpinningHeadsTask(Player player) {
            this.player = player;
            this.heads = new ArrayList<>();
            playerHeads.put(player, heads);
        }

        @Override
        public void run() {
            if (heads.size() < 8) { // se não há 8 cabeças criadas ainda
                for (int i = 0; i < 8; i++) { // criar 8 cabeças
                    IArmorStand head = NMS.createArmorStand(player.getLocation().clone().add(0, 1, 0), null, null);
                    head.getEntity().setHelmet(BukkitUtils.deserializeItemStack("SKULL_ITEM:3 : 1 : skin>eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRiNWNlMGQ0NGMzZTgxMzhkYzJlN2U1MmMyODk3YmI4NzhlMWRiYzIyMGQ3MDY4OWM3YjZiMThkMzE3NWUwZiJ9fX0="));
                    head.getEntity().setGravity(false); // desativar a gravidade para que as cabeças não caiam
                    head.getEntity().setSmall(true); // definir as cabeças como pequenas
                    heads.add(head); // adicionar a cabeça à lista de cabeças
                }
            }
            for (int i = 0; i < 36; i++) { // 36 points to create a smooth circle
            	final double angle = i * Math.PI / 18; // evenly spaced angles
            	final double radius = 12.0; // distância do jogador
            	final Location  loc = player.getLocation().clone().add(Math.cos(angle) * radius, 0.5, Math.sin(angle) * radius);
                player.getWorld().playEffect(loc, Effect.FLAME, 0);
            }
	        // Cria raios perto do jogador
	        Location loc = player.getLocation();
	        for (int i = 0; i < 2; i++) {
	        	final double angle = Math.random() * Math.PI * 2;
	        	final double radius = 12.5;
	        	final double x = loc.getX() + Math.cos(angle) * radius;
	        	final double y = loc.getY() + 0.5;
	        	final double z = loc.getZ() + Math.sin(angle) * radius;
	        	final Location rayLoc = new Location(loc.getWorld(), x, y, z);
	            player.getWorld().strikeLightning(rayLoc);
	        }
            // Update head positions and rotations
            for (final IArmorStand head : heads) { // para cada cabeça na lista de cabeças
            	final double angle = (System.currentTimeMillis() * 0.0025) + (heads.indexOf(head) * Math.PI / 4);
            	final double radius = 3.0; // distância do jogador
            	final double x = player.getLocation().getX() + Math.cos(angle) * radius;
            	final double y = player.getLocation().getY() + 0.1; // altura das cabeças
            	final double z = player.getLocation().getZ() + Math.sin(angle) * radius;
                head.getEntity().teleport(new Location(player.getWorld(), x, y, z));
                head.getEntity().setHeadPose(new EulerAngle(0, angle, 0));
            }
            
            // Remove heads after 1 second
            time++;
            if (time >= 400) {
                Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
                    @Override
                    public void run() {
                    for (IArmorStand head : playerHeads.get(player)) {
                        head.killEntity();
                    }
                    playerHeads.get(player).clear();
                    }
                }, 2); // 10 ticks = 0.5 segundos
                time = 0;
                taskTimes.remove(player);
                isTaskRunning.remove(player);
            }
        }
        @SuppressWarnings("unused")
		private int getTime() {
            return time;
        }

        private void setTime(int time) {
            this.time = time;
        }
    }

    private class LiftPlayerTask extends BukkitRunnable {
        private Player player;
        private int time = 0;
        private SConfig m1 = Main.get().getConfig("Mensagens");
        private boolean dontMoveMessageSent = false;
        public LiftPlayerTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            player.setVelocity(new Vector(0, 0.1, 0)); // Adjust the velocity to your liking
            player.getLocation().add(0, 0.1, 0); // Update the player's location
            player.setAllowFlight(true);

            // Incrementa o tempo de duração do evento
            time++;
            if (time >= 100) { // 100 ticks = 5 segundos
                player.setVelocity(new Vector(0, 0, 0)); // Zera a velocidade do jogador
                player.setAllowFlight(true); // Permite que o jogador flutue
                player.setFlying(true); // Faz o jogador flutuar
                if (!dontMoveMessageSent) { // Verifica se a mensagem não foi enviada
                    MessageUtils.send(player, m1.getString("GodChestPlate.DontMove"), m1.getStringList("GodChestPlate.DontMove"));
                    dontMoveMessageSent = true; // Marca a mensagem como enviada
                }
            }
            // Cancela o evento após 5 segundos (ou qualquer outro tempo que você deseje)
            if (time >= 400) { // 100 ticks = 5 segundos
                player.setVelocity(new Vector(0, -1, 0)); // Faz o jogador cair rapidamente
                player.setAllowFlight(false);
                time = 0;
                player.setMaxHealth(20);
                isTaskActivate.put(player, true);
                isTaskRunning.remove(player);
                API.dispatchCommands(player, m.getStringList("GodChestPlate.Efeitos"));
                MessageUtils.send(player, m1.getString("GodChestPlate.AtivadoPlayer"),m1.getStringList("GodChestPlate.AtivadoPlayer"));
                for (Entity s : player.getNearbyEntities(
        		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue(), 
        		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue(), 
        		         Integer.valueOf(m.getInt("GodChestPlate.RaioMensagem")).intValue())) {
                    	if (s instanceof Player) {
                    	Player p1 = (Player)s;
                     MessageUtils.send(p1, m1.getString("GodChestPlate.Ativado"),m1.getStringList("GodChestPlate.Ativado"));
                    	}
                 }
                Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
                    @Override
                    public void run() {
                        player.setHealth(20);
                        godchest.add(player.getName());
                        cancelTasks(player);
                    }
                }, 1); // 10 ticks = 0.5 segundos
                
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.get(), new Runnable() {
                    @Override
                    public void run() {
                        // Verifique se o jogador ainda está online
                        if (player.isOnline()) {
                            // Tire 0,5 corações de vida do jogador
                            player.setHealth(player.getHealth() - m.getDouble("GodChestPlate.Vida"));
                            
                            // Verifique se o jogador ainda tem vida
                            if (player.getHealth() <= 0) {
                                // Cancela a tarefa
                            	godchest.remove(player.getName());
                            	cooldowns.put(player, System.currentTimeMillis() + 1000 * m.getInt("GodChestPlate.Cooldown"));
                                cancelTask(player);
                            }
                        } else {
                            // Cancela a tarefa se o jogador sair do servidor
                        	godchest.remove(player.getName());
                            cancelTask(player);
                        }
                    }
                }, 0, m.getInt("GodChestPlate.TempoVida")); // 0 = inicia imediatamente, interval = 10 ticks
                tasks.put(player, task);
            }
        }
    }
    @EventHandler
    private void onEntityRegainHealth(EntityRegainHealthEvent event) {
        // Verifique se o entidade é um jogador
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (!p.isDead() && godchest.contains(p.getName())) {
                // Cancela a regeneração de vida
                event.setCancelled(true);
            }
        }
    }
    private void cancelTask(Player player) {
        // Verifique se o jogador tem uma tarefa
        if (tasks.containsKey(player)) {
            // Cancela a tarefa
            tasks.get(player).cancel();
            // Remova a tarefa do mapa
            tasks.remove(player);
        }
    }
    private void cancelTasks(Player player) {
        BukkitTask spinningHeadsTask = spinningHeadsTasks.get(player);
        BukkitTask liftPlayerTask = liftPlayerTasks.get(player);

        if (spinningHeadsTask != null) {
            spinningHeadsTask.cancel();
            spinningHeadsTasks.remove(player);
        }

        if (liftPlayerTask != null) {
            liftPlayerTask.cancel();
            liftPlayerTasks.remove(player);
        }
    }
    
}