package models;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * 
 * @author Justin Wilson
 *
 */
public class TransferableInventory implements Transferable {
	private Inventory inventory;
	
	public static final DataFlavor INVENTORY_FLAVOR = new DataFlavor(Inventory.class, "A Inventory Object");

	protected static DataFlavor[] supportedFlavors = {INVENTORY_FLAVOR, DataFlavor.stringFlavor};
	    
	public TransferableInventory(Inventory d) { 
		inventory = d;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.equals(INVENTORY_FLAVOR) || flavor.equals(DataFlavor.stringFlavor)) 
			return true;
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(INVENTORY_FLAVOR))
	         return inventory;
	     else if (flavor.equals(DataFlavor.stringFlavor)) 
	         return inventory.toString();
	     else 
	         throw new UnsupportedFlavorException(flavor);
	}

}
