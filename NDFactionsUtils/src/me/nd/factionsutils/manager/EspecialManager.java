package me.nd.factionsutils.manager;

import me.nd.factionsutils.itens.*;
import me.nd.factionsutils.manager.especial.Especial;

import com.google.common.collect.Lists;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class EspecialManager
{
    private static List<Especial> items = Lists.newArrayList();
    
    public EspecialManager() {
    	items.add(new Agrupador());
    	items.add(new AntiTrapItem());
    	items.add(new Armadilha());
    	items.add(new AspectoItem());
    	items.add(new AttackItem());
    	items.add(new BlazeExplosivo());
    	items.add(new BolaDeFogo());
    	items.add(new BoxItem());
    	items.add(new Creeper());
    	items.add(new CuboPerfeito());
    	items.add(new CabecaHydraItem());
    	items.add(new CaldeiraoItem());
    	items.add(new ClearChunkItem());
    	items.add(new CoinItem());
    	items.add(new CataclistItem());
    	items.add(new Detect());
    	items.add(new FragmentItem());
    	items.add(new GeradorItem());
    	items.add(new GodChestPlate());
    	items.add(new GosmaItem());
    	items.add(new IceWarItem());
    	items.add(new Incinerador());
    	items.add(new InversorItem());
    	items.add(new LauncherItem());
    	items.add(new MaquinaDoTempoItem());
    	items.add(new MembroMaximo());
    	items.add(new MeteorItem());
    	items.add(new MoedaItem());
    	items.add(new MagmaFlamejante());
    	items.add(new MuralhaItem());
    	items.add(new OlhoDeDeusItem());
    	items.add(new Picareta());
    	items.add(new PicaretaHItem());
    	items.add(new PoderMax());
    	items.add(new PoderInsta());
    	items.add(new Purificador());
    	items.add(new PulsoMagneticoItem());
    	items.add(new RaioMestre());
    	items.add(new RegeneradorItem());
    	items.add(new RelogioRedstone());
    	items.add(new RelogioDoTempo());
    	items.add(new Reparador());    	
    	items.add(new ResetKdr());
    	items.add(new SalvacaoItem());
    	items.add(new SaveLifeItem());
    	items.add(new SlimeBlockJumpItem());
    	items.add(new SuperCreeper());
    	items.add(new SuperSocoItem());
    	items.add(new TitaniumItem());
    	items.add(new TnT());
    	items.add(new TntRadioativaItem());
        items.add(new TntThrowItem());
        items.add(new TrackerItem());
        items.add(new Totem());
        items.add(new Schematic());
    }

    public static List<Especial> getItems() {
        return items;
    }

    public static Especial getItem(String name) {
        for (Especial item : items) {
            if (!item.getName().equalsIgnoreCase(name)) continue;
            return item;
        }
        return null;
    }

    public static Especial getItem(Integer id) {
        for (Especial item : items) {
            if (item.getId() != id) continue;
            return item;
        }
        return null;
    }

    public static Especial getItem(ItemStack item) {
        return items.stream().filter(customItem -> customItem.getItem().isSimilar(item)).findFirst().orElse(null);
    }
}
