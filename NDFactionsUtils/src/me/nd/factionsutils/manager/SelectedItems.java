package me.nd.factionsutils.manager;

import java.util.List;

import me.nd.factionsutils.manager.especial.Especial;

public class SelectedItems {
	
	 private List<Especial> selectedItems;

	    public SelectedItems(List<Especial> selectedItems) {
	        this.selectedItems = selectedItems;
	    }

	    public List<Especial> getSelectedItems() {
	        return selectedItems;
	    }	

}
