package models;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class TransferableUser implements Transferable {
	private User user;
	
	public static final DataFlavor USER_FLAVOR = new DataFlavor(User.class, "A User Object");

	protected static DataFlavor[] supportedFlavors = {USER_FLAVOR, DataFlavor.stringFlavor};
	    
	public TransferableUser(User u) { 
		user = u;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.equals(USER_FLAVOR) || flavor.equals(DataFlavor.stringFlavor)) 
			return true;
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(USER_FLAVOR))
	         return user;
	     else if (flavor.equals(DataFlavor.stringFlavor)) 
	         return user.toString();
	     else 
	         throw new UnsupportedFlavorException(flavor);
	}

}
