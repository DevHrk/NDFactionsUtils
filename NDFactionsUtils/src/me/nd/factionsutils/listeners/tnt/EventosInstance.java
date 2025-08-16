package me.nd.factionsutils.listeners.tnt;

import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EventosInstance
extends Event
implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    int x;
    int y;
    int z;
    public TNTPrimed tnt;
    boolean cancelled;

    public EventosInstance(int x, int y, int z, TNTPrimed tnt) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.tnt = tnt;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public TNTPrimed getTNT() {
        return this.tnt;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean arg0) {
        this.cancelled = arg0;
    }
}