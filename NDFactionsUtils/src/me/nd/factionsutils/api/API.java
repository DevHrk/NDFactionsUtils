package me.nd.factionsutils.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.factions.SpawnFireWork;
import me.nd.factionsutils.listeners.itens.ReparadorListener;
import me.nd.factionsutils.messages.JsonMessage;
import me.nd.factionsutils.messages.MessageUtils;

public class API {
	
	public static HashMap<Player, Location> saves = new HashMap();
	public static ArrayList<Player> playing = new ArrayList();
	public static Map<Player, List<ArmorStand>> playerHolograms = new HashMap<>();

    public static void addPlayer(Player p) {
        playing.add(p);
    }

    public static void delPlayer(Player p) {
        if (playing.contains(p)) {
            playing.remove(p);
        }
    }

    public static boolean isPlaying(Player p) {
        return playing.contains(p);
    }

    public static void saveLocation(Player p) {
        saves.put(p, p.getLocation());
    }

    public static void delLocation(Player p) {
        if (saves.containsKey(p)) {
            saves.remove(p);
        }
    }

    public static Location getLocation(Player p) {
        if (saves.containsKey(p)) {
            return saves.get(p);
        }
        return null;
    }
	
	public static void removeItem(Player p) {
		if (p.getItemInHand().getAmount() < 2) {
			p.setItemInHand(new ItemStack(Material.AIR));
		} else {
			ItemStack item = p.getItemInHand();
			item.setAmount(item.getAmount() - 1);
		}
	}
	
	public static void sphere(Location loc, int radio, Effect particle) {
          double i = 0.0;
          while (i <= Math.PI) {
              double radius = Math.sin(i);
              double y = Math.cos(i);
              double a = 0.0;
              while (a < Math.PI * 2) {
                  double x = Math.cos(a) * radius;
                  double z = Math.sin(a) * radius;
                  loc.add(x * (double)radio, y * (double)radio, z * (double)radio);
                  loc.getWorld().playEffect(loc, particle, Integer.MAX_VALUE);
                  loc.subtract(x * (double)radio, y * (double)radio, z * (double)radio);
                  a += Math.PI / (double)(radio * 2);
              }
              i += Math.PI / (double)(radio * 2);
          }
      }
	
	public static ArmorStand createCustomHologram(Location location, String name) {
	 ArmorStand am = (ArmorStand) location.getWorld().spawnEntity(location.add(0.5, 0.5, 0.5), EntityType.ARMOR_STAND);
     am.setVisible(false);
     am.setBasePlate(false);
     am.setCanPickupItems(true);
     am.setCustomName(name);
     am.setSmall(true);
     am.setRemoveWhenFarAway(false);
     am.setGravity(false);
     am.setCustomNameVisible(true);
     return am;
	  }
	
	public static ArmorStand createCustomArmorStand(Location location, ItemStack item) {
	  ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.add(0.9, -0.4, 0.01), EntityType.ARMOR_STAND);
	  armorStand.setVisible(false);
	  armorStand.setBasePlate(false);
	  armorStand.setCanPickupItems(true);
	  armorStand.setSmall(false);
	  armorStand.getEquipment().setItemInHand(item);
	  armorStand.setRightArmPose(new EulerAngle(300.0, 0.0, 270.0));
	  armorStand.setRemoveWhenFarAway(false);
	  armorStand.setGravity(false);
	  armorStand.setCustomNameVisible(false);
	 return armorStand;
	}
	    
	public static void removeCustomArmorStand(Location location) {
	 // Obtenha todas as entidades na localização
	 Collection<Entity> entities = location.getWorld().getNearbyEntities(location, 1, 1, 1);

     for (Entity entity : entities) {
	   // Verifique se a entidade é um ArmorStand
	    if (entity instanceof ArmorStand) {
	     // Remova o ArmorStand
	      entity.remove();
	     }
	   }
     }
	
		public static void dispatchCommands(Player p, List<String> commands) {
			for (String command : commands) {

				if (command.startsWith("console: ")) {
					String line = command.split("console: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\n", "\\n");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line);
					continue;
				}

				if (command.startsWith("player: ")) {
					String line = command.split("player: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n");
					p.chat("/" + line);
					continue;
				}

				if (command.startsWith("mensagem: ")) {
					String msg = command.split("mensagem: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n");
					p.sendMessage(msg);
					continue;
				}

				if (command.startsWith("broadcast: ")) {
					String msg = command.split("broadcast: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n");
					Bukkit.broadcastMessage(msg);
					continue;
				}
				
				if (command.startsWith("json: ")) {
					String msg = command.split("json: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n");
					JsonMessage.send(p, msg);
					continue;
				}
				
				if (command.startsWith("actionbar: ")) {
					String msg = command.split("actionbar: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n");
					ActionbarAPI.sendActionBarMessage(p, msg);
					continue;
				}
				
				if (command.startsWith("title: ")) {
					String msg = command.split("title: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n");
					TitleAPI.sendTitle(p, 20, 30, 20, msg, "");
					continue;
				}
				if (command.startsWith("titlesub: ")) {
				    String[] parts = command.split("titlesub: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    String title = parts[0];
				    String subtitle = parts[1];
				    TitleAPI.sendTitle(p, 20, 30, 20, title, subtitle);
				    continue;
				}
				if (command.startsWith("schematic: ")) {
				    String msg = command.split("schematic: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n");
				    SchematicAPI.loadBlocks(p, p.getLocation(), msg);
				    continue;
				}
				if (command.startsWith("cleareffects")) {
					clearEffects(p);
				    continue;
				}
				if (command.startsWith("fireempty")) {
					SpawnFireWork.empty(p);
				    continue;
				}
				if (command.startsWith("firesmall")) {
					SpawnFireWork.small(p);
				    continue;
				}
				if (command.startsWith("firemedium")) {
					SpawnFireWork.medium(p);
				    continue;
				}
				if (command.startsWith("repair")) {
					ReparadorListener.repairArmor(p);
				    continue;
				}
				if (command.startsWith("agrouppotion")) {
					ReparadorListener.agroupPotions(p);
				    continue;
				}
				
				if (command.startsWith("potion: ")) {
				    String[] parts = command.split("potion: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    String potion = parts[0];
				    int level = Integer.parseInt(parts[1]);
				    int duration = Integer.parseInt(parts[2]);
				    addPotionEffect(p, PotionEffectType.getByName(potion), level, duration);
				    continue;
				}
				Location localizacaoAnterior = p.getLocation();
				if (command.startsWith("sphere: ")) {
				    String[] parts = command.split("sphere: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    int raio = Integer.parseInt(parts[0]);
				    String particula = parts[1];
				    Effect efeito = Effect.valueOf(particula);
				    // Defina a duração em segundos
				    int duracao = Integer.parseInt(parts[2]); // Altere este valor para a duração desejada
				    new BukkitRunnable() {
				        int counter = 0;
				        public void run() {
				            if (counter >= duracao) {
				                this.cancel();
				            } else {
				                sphere(localizacaoAnterior, raio, efeito);
				                counter++;
				            }
				        }
				    }.runTaskTimer(Main.get(), 0L, 20L); // 20 ticks = 1 segundo
				    
				    continue;
				}
				
				if (command.startsWith("hologramaitem: ")) {
				    String[] parts = command.split("hologramaitem: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    String material = parts[0];
				    int tempo = Integer.parseInt(parts[1]);
				    ItemStack item2 = new ItemStack(Material.getMaterial(material));
				    ArmorStand hologram = createCustomArmorStand(p.getLocation(), item2);
					List<ArmorStand> holograms = playerHolograms.getOrDefault(p, new ArrayList<>());
					holograms.add(hologram);
					playerHolograms.put(p, holograms);
					for (int i = 0; i < holograms.size(); i++) {
					    ArmorStand holograma = holograms.get(i);
					    Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
					        holograma.remove();
					    }, 20L * tempo * (i + 1));
					}
					holograms.clear();
				    continue;
				}
				
				if (command.startsWith("hologramamensagem: ")) {
				    String[] parts = command.split("hologramamensagem: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    String mensagem = parts[0];
				    int tempo = Integer.parseInt(parts[1]);
				    ArmorStand hologram = createCustomHologram(p.getLocation(), mensagem);
					List<ArmorStand> holograms = playerHolograms.getOrDefault(p, new ArrayList<>());
					holograms.add(hologram);
					playerHolograms.put(p, holograms);
					for (int i = 0; i < holograms.size(); i++) {
					    ArmorStand holograma = holograms.get(i);
					    Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
					        holograma.remove();
					    }, 20L * tempo * (i + 1));
					}
					holograms.clear();
				    continue;
				}
				
				if (command.startsWith("nearbyeffect: ")) {
				    String[] parts = command.split("nearbyeffect: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    String effect = parts[0];
				    int level = Integer.parseInt(parts[1]);
				    int duration = Integer.parseInt(parts[2]);
				    int raio = Integer.parseInt(parts[3]);
				    List<Entity> nearbyEntities = p.getNearbyEntities(raio, raio, raio);
				    for (Entity s : nearbyEntities) {
				        if (s instanceof Player) {
				            Player t = (Player)s;
				            addPotionEffect(t, PotionEffectType.getByName(effect), level, duration);
				        }
				    }
				    continue;
				}
				
				if (command.startsWith("nearbyeffectf: ")) {
				    String[] parts = command.split("nearbyeffectf: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    String effect = parts[0];
				    int level = Integer.parseInt(parts[1]);
				    int duration = Integer.parseInt(parts[2]);
				    int raio = Integer.parseInt(parts[3]);
				    List<Entity> nearbyEntities = p.getNearbyEntities(raio, raio, raio);
				    for (Entity s : nearbyEntities) {
				        if (s instanceof Player) {
				            Player t = (Player)s;
				            NDPlayer mt = DataManager.players.get(t.getName());
		                    NDFaction fact = mt.getFaction();
		                    NDPlayer mp = DataManager.players.get(p.getName());
		                    NDFaction facp = mp.getFaction();
		                    if (fact == null || fact != facp) {
				            addPotionEffect(t, PotionEffectType.getByName(effect), level, duration);
		                    }
				        }
				    }
				    continue;
				}
				if (command.startsWith("nearbyeffectclears: ")) {
				    String[] parts = command.split("nearbyeffectclears: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    int raio = Integer.parseInt(parts[0]);
				    List<Entity> nearbyEntities = p.getNearbyEntities(raio, raio, raio);
				    for (Entity s : nearbyEntities) {
				        if (s instanceof Player) {
				            Player t = (Player)s;
				            clearEffects(t);
				        }
				    }
				    continue;
				}
				
				
				if (command.startsWith("nearbyeffectclearsf: ")) {
				    String[] parts = command.split("nearbyeffectclearsf: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    int raio = Integer.parseInt(parts[0]);
				    List<Entity> nearbyEntities = p.getNearbyEntities(raio, raio, raio);
				    for (Entity s : nearbyEntities) {
				        if (s instanceof Player) {
				            Player t = (Player)s;
				            NDPlayer mt = DataManager.players.get(t.getName());
		                    NDFaction fact = mt.getFaction();
		                    NDPlayer mp = DataManager.players.get(p.getName());
		                    NDFaction facp = mp.getFaction();
		                    if (fact == null || fact != facp) {
					            clearEffects(t);
		                    }
				        }
				    }
				    continue;
				}
				
				if (command.startsWith("nearbyeffectclear: ")) {
				    String[] parts = command.split("nearbyeffectclear: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    int raio = Integer.parseInt(parts[1]);
				    List<String> messages = new ArrayList<>();
				    if (parts[0].contains(",")) {
				        messages = Arrays.asList(parts[0].split(","));
				    } else {
				        messages.add(parts[0]);
				    }
				    List<Entity> nearbyEntities = p.getNearbyEntities(raio, raio, raio);
				    for (Entity s : nearbyEntities) {
				        if (s instanceof Player) {
				            Player t = (Player)s;
		                    List<PotionEffectType> potionEffectsToRemove = messages.stream().map(effect -> PotionEffectType.getByName(effect.toUpperCase())).collect(Collectors.toList());
		    				clearSelectedEffects(t, potionEffectsToRemove);
				        }
				    }
				    continue;
				}
				
				if (command.startsWith("nearbyeffectclearf: ")) {
				    String[] parts = command.split("nearbyeffectclearf: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(":");
				    int raio = Integer.parseInt(parts[1]);
				    List<String> messages = new ArrayList<>();
				    if (parts[0].contains(",")) {
				        messages = Arrays.asList(parts[0].split(","));
				    } else {
				        messages.add(parts[0]);
				    }
				    List<Entity> nearbyEntities = p.getNearbyEntities(raio, raio, raio);
				    for (Entity s : nearbyEntities) {
				        if (s instanceof Player) {
				            Player t = (Player)s;
				            NDPlayer mt = DataManager.players.get(t.getName());
		                    NDFaction fact = mt.getFaction();
		                    NDPlayer mp = DataManager.players.get(p.getName());
		                    NDFaction facp = mp.getFaction();
		                    if (fact == null || fact != facp) {
		                    	List<PotionEffectType> potionEffectsToRemove = messages.stream().map(effect -> PotionEffectType.getByName(effect.toUpperCase())).collect(Collectors.toList());
		    				    clearSelectedEffects(t, potionEffectsToRemove);
		                    }
				        }
				    }
				    continue;
				}
				
				if (command.startsWith("cleareffect: ")) {
				    String[] parts = command.split("cleareffect: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(";");
				    List<String> messages = new ArrayList<>();
				    if (parts[0].contains(",")) {
				        messages = Arrays.asList(parts[0].split(","));
				    } else {
				        messages.add(parts[0]);
				    }
				    List<PotionEffectType> potionEffectsToRemove = messages.stream().map(effect -> PotionEffectType.getByName(effect.toUpperCase())).collect(Collectors.toList());
				    clearSelectedEffects(p, potionEffectsToRemove);
				    continue;
				}
				
				if (command.startsWith("nearbymessage: ")) {
				    String[] parts = command.split("nearbymessage: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(";");
				    List<String> messages = new ArrayList<>();
				    if (parts[0].contains(",")) {
				        messages = Arrays.asList(parts[0].split(","));
				    } else {
				        messages.add(parts[0]);
				    }
				    int raio = Integer.parseInt(parts[1]);
				    List<Entity> nearbyEntities = p.getNearbyEntities(raio, raio, raio);
				    for (Entity s : nearbyEntities) {
				        if (s instanceof Player) {
				            Player t = (Player)s;
				            MessageUtils.send(t, messages);
				        }
				    }
				    continue;
				}
				
				if (command.startsWith("nearbymessagef: ")) {
				    String[] parts = command.split("nearbymessagef: ")[1].replace('&', '§').replace("{player}", p.getName()).replace("\\n", "\n").split(";");
				    List<String> messages = new ArrayList<>();
				    if (parts[0].contains(",")) {
				        messages = Arrays.asList(parts[0].split(","));
				    } else {
				        messages.add(parts[0]);
				    }
				    int raio = Integer.parseInt(parts[1]);
				    List<Entity> nearbyEntities = p.getNearbyEntities(raio, raio, raio);
				    for (Entity s : nearbyEntities) {
				        if (s instanceof Player) {
				            Player t = (Player)s;
				            NDPlayer mt = DataManager.players.get(t.getName());
		                    NDFaction fact = mt.getFaction();
		                    NDPlayer mp = DataManager.players.get(p.getName());
		                    NDFaction facp = mp.getFaction();
		                    if (fact == null || fact != facp) {
				            MessageUtils.send(t, messages);
		                    }
				        }
				    }
				    continue;
				}
			}
		}
		private static void clearEffects(Player p) {
			for (PotionEffect pe : p.getActivePotionEffects()) {
				p.removePotionEffect(pe.getType());
			}
		}
		private static void clearSelectedEffects(Player p, List<PotionEffectType> effectsToRemove) {
		    for (PotionEffect pe : p.getActivePotionEffects()) {
		        if (effectsToRemove.contains(pe.getType())) {
		            p.removePotionEffect(pe.getType());
		        }
		    }
		}
		
		private static void addPotionEffect(Player p, PotionEffectType effectType, int level, int duration) {
		    p.addPotionEffect(new PotionEffect(effectType, duration * 20, level - 1));
		}
	    public static String formatTime(long tempo) {
	        if (tempo == 0L)
	          return "0 segundos"; 
	        long dias = TimeUnit.MILLISECONDS.toDays(tempo);
	        long horas = TimeUnit.MILLISECONDS.toHours(tempo) - dias * 24L;
	        long minutos = TimeUnit.MILLISECONDS.toMinutes(tempo) - TimeUnit.MILLISECONDS.toHours(tempo) * 60L;
	        long segundos = TimeUnit.MILLISECONDS.toSeconds(tempo) - TimeUnit.MILLISECONDS.toMinutes(tempo) * 60L;
	        StringBuilder sb = new StringBuilder();
	        if (dias > 0L)
	          sb.append(dias + ((dias == 1L) ? "d" : "d")); 
	        if (horas > 0L)
	          sb.append((dias > 0L) ? ((minutos > 0L) ? ", " : " ") : "").append(horas + ((horas == 1L) ? "h" : "h")); 
	        if (minutos > 0L)
	          sb.append((dias <= 0L && horas <= 0L) ? "" : ((segundos > 0L) ? ", " : " ")).append(minutos + ((minutos == 1L) ? "m" : "m")); 
	        if (segundos > 0L)
	          sb.append((dias <= 0L && horas <= 0L && minutos <= 0L) ? ((sb.length() > 0) ? ", " : "") : " ").append(segundos + ((segundos == 1L) ? "s" : "s")); 
	        String s = sb.toString();
	        return s.isEmpty() ? "0 segundos" : s;
	      }
}
