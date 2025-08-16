package me.nd.factionsutils.menus.item;

import org.bukkit.inventory.ItemStack;

public class MItem {
	  private final ItemStack item;
	  
	  private final int slot;
	  
	  private final int cost;
	  
	  private final int costCash;
	  
	  private final boolean consoleRight;
	  
	  private final boolean consoleLeft;
	  
	  private final String commandRight;
	  
	  private final String commandLeft;
	  
	  MItem(ItemStack item, int slot, int cost, int costCash, boolean consoleRight, boolean consoleLeft, String commandRight, String commandLeft) {
	    this.item = item;
	    this.slot = slot;
	    this.cost = cost;
	    this.costCash = costCash;
	    this.consoleRight = consoleRight;
	    this.consoleLeft = consoleLeft;
	    this.commandRight = commandRight;
	    this.commandLeft = commandLeft;
	  }
	  
	  public static MItemBuilder builder() {
	    return new MItemBuilder();
	  }
	  
	  public static class MItemBuilder {
	    private ItemStack item;
	    
	    private int slot;
	    
	    private int cost;
	    
	    private int costCash;
	    
	    private boolean consoleRight;
	    
	    private boolean consoleLeft;
	    
	    private String commandRight;
	    
	    private String commandLeft;
	    
	    public MItemBuilder item(ItemStack item) {
	      this.item = item;
	      return this;
	    }
	    
	    public MItemBuilder slot(int slot) {
	      this.slot = slot;
	      return this;
	    }
	    
	    public MItemBuilder cost(int cost) {
	      this.cost = cost;
	      return this;
	    }
	    
	    public MItemBuilder costCash(int costCash) {
	      this.costCash = costCash;
	      return this;
	    }
	    
	    public MItemBuilder consoleRight(boolean consoleRight) {
	      this.consoleRight = consoleRight;
	      return this;
	    }
	    
	    public MItemBuilder consoleLeft(boolean consoleLeft) {
	      this.consoleLeft = consoleLeft;
	      return this;
	    }
	    
	    public MItemBuilder commandRight(String commandRight) {
	      this.commandRight = commandRight;
	      return this;
	    }
	    
	    public MItemBuilder commandLeft(String commandLeft) {
	      this.commandLeft = commandLeft;
	      return this;
	    }
	    
	    public MItem build() {
	      return new MItem(this.item, this.slot, this.cost, this.costCash, this.consoleRight, this.consoleLeft, this.commandRight, this.commandLeft);
	    }
	    
	    public String toString() {
	      return "MItem.MItemBuilder(item=" + this.item + ", slot=" + this.slot + ", cost=" + this.cost + ", costCash=" + this.costCash + ", consoleRight=" + this.consoleRight + ", consoleLeft=" + this.consoleLeft + ", commandRight=" + this.commandRight + ", commandLeft=" + this.commandLeft + ")";
	    }
	  }
	  
	  public ItemStack getItem() {
	    return this.item;
	  }
	  
	  public int getSlot() {
	    return this.slot;
	  }
	  
	  public int getCost() {
	    return this.cost;
	  }
	  
	  public int getCostCash() {
	    return this.costCash;
	  }
	  
	  public boolean isConsoleRight() {
	    return this.consoleRight;
	  }
	  
	  public boolean isConsoleLeft() {
	    return this.consoleLeft;
	  }
	  
	  public String getCommandRight() {
	    return this.commandRight;
	  }
	  
	  public String getCommandLeft() {
	    return this.commandLeft;
	  }
	}
