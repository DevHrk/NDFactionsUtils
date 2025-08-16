package me.nd.factionsutils.gen;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import me.nd.factionsutils.hologram.StringUtils;
import me.nd.factionsutils.reflection.SerializeBase64;

public class Gerador {
    private String location;
    private String item;

    public Gerador(Location loc, ItemStack item) {
        this.location = StringUtils.serializeLocation(loc);
        this.item = SerializeBase64.toBase64(item);
    }

    public String getLocation() {
        return this.location;
    }

    public String getItem() {
        return this.item;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Gerador)) {
            return false;
        }
        Gerador other = (Gerador)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$location = this.getLocation();
        String other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) {
            return false;
        }
        String this$item = this.getItem();
        String other$item = other.getItem();
        return !(this$item == null ? other$item != null : !this$item.equals(other$item));
    }

    protected boolean canEqual(Object other) {
        return other instanceof Gerador;
    }

	public int hashCode() {
        int result = 1;
        String $location = this.getLocation();
        result = result * 59 + ($location == null ? 43 : $location.hashCode());
        String $item = this.getItem();
        result = result * 59 + ($item == null ? 43 : $item.hashCode());
        return result;
    }

    public String toString() {
        return "Gerador(location=" + this.getLocation() + ", item=" + this.getItem() + ")";
    }
}
