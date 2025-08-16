package me.nd.factionsutils.factions;

import org.bukkit.entity.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.*;
import java.util.*;

public class SpawnFireWork
{
    public static void empty( Player p) {
        p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
    }
    
    public static void small( Player p) {
         Firework fw = (Firework)p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
         FireworkMeta fm = fw.getFireworkMeta();
        fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(random()).build());
        fw.setFireworkMeta(fm);
    }
    
    public static void medium( Player p) {
         Firework fw = (Firework)p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
         FireworkMeta fm = fw.getFireworkMeta();
        fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(new Color[] { random(), random() }).build());
        fm.setPower(1);
        fw.setFireworkMeta(fm);
    }
    
    public static void big( Player p) {
         Firework fw = (Firework)p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
         FireworkMeta fm = fw.getFireworkMeta();
        fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(new Color[] { random(), random(), random(), random() }).build());
        fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(new Color[] { random(), random(), random(), random() }).build());
        fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(new Color[] { random(), random(), random(), random() }).build());
        fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(new Color[] { random(), random(), random(), random() }).build());
        fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(new Color[] { random(), random(), random(), random() }).build());
        fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(new Color[] { random(), random(), random(), random() }).build());
        fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(new Color[] { random(), random(), random(), random() }).build());
        fm.setPower(1);
        fw.setFireworkMeta(fm);
    }
    
    private static Color random() {
         Random rnd = new Random();
         int r = rnd.nextInt(255);
         int g = rnd.nextInt(255);
         int b = rnd.nextInt(255);
        return Color.fromRGB(r, g, b);
    }
}
