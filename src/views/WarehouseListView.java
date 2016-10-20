package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import controller.WarehouseListController;
import controller.MDIChild;
import controller.MDIParent;
import controller.MenuCommands;
import models.Warehouse;
import models.InventoryList;
import models.TransferableWarehouse;

/**
 * Displays a list of Warehouse objects
 * Double click Warehouse creates and shows detail view of that Warehouse object 

 * NOTE: this view is now coupled with MDI controller classes
 * In order for it to possibly communicate with the MDI master/parent frame
 * @author Justin Wilson
 *
 */
public class WarehouseListView extends MDIChild {	
	/**
	 * GUI instance variables
	 */
	private JList<Warehouse> listWarehouses;
	private WarehouseListController myList;
	//saves reference to last selected model in JList
	//parent asks for this when opening a detail view
	private Warehouse selectedModel;
	
	private InventoryList myInventoryList;
	
	/**
	 * Constructor
	 * @param title Window title
	 * @param list WarehouseListController contains collection of Warehouse objects
	 * @param mdiParent MasterFrame MDI parent window reference
	 */
	public WarehouseListView(String title, WarehouseListController list, MDIParent m) {
		super(title, m);
		
		//set self to list's view (allows ListModel to tell this view to repaint when models change)
		//WarehouseListController is an observer of the models
		list.setMyListView(this);
		
		myInventoryList = m.getInventoryList();
		
		//prep list view
		myList = list;
		listWarehouses = new JList<Warehouse>(myList);
		//allow drag and drop from warehouse list to person detail view warehouse list
		listWarehouses.setDragEnabled(true);
		listWarehouses.setTransferHandler(new WarehouseDragTransferHandler());
		
		//use our custom cell renderer instead of default (don't want to use Warehouse.toString())
		listWarehouses.setCellRenderer(new WarehouseListCellRenderer());
		listWarehouses.setPreferredSize(new Dimension(200, 200));
		
		//add event handler for double click
		listWarehouses.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				//if double-click then get index and open new detail view with record at that index
		        if(evt.getClickCount() == 2) {
		        	int index = listWarehouses.locationToIndex(evt.getPoint());
		        	//get the Warehouse at that index
		        	selectedModel = myList.getElementAt(index);
		        	
		        	//open a new detail view
		        	openDetailView();
		        }
		    }
		});
		
		//add to content pane
		this.add(new JScrollPane(listWarehouses));
		
		//add a Delete button to delete selected person
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton button = new JButton("Delete Warehouse");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteWarehouse();
			}
		});
		panel.add(button);
		
		this.add(panel, BorderLayout.SOUTH);

		this.setPreferredSize(new Dimension(240, 200));
	}

	/**
	 * Tells MDI parent to delete the selected warehouse. if none selected then ignore
	 */
	private void deleteWarehouse() {
		//get the selected model and set as selectedWarehouse instance variable
		//mdi parent will ask for this when handling delete person call
		int idx = listWarehouses.getSelectedIndex();
		if(idx < 0)
			return;
		//idx COULD end up > list size so make sure idx is < list size
		if(idx >= myList.getSize())
			return;
		Warehouse d = myList.getElementAt(idx);
		if(d == null)
			return;
		selectedModel = d;
		
		// check exist in inventory
		if( myInventoryList.existWarehousePart( d.getId(), 0l) ){
			
			parent.displayChildMessage("Can not delete this warehouse because it exists in Inventory table");
			return;
			
		}
		
		//ask user to confirm deletion
		String [] options = {"Yes", "No"};
		if(JOptionPane.showOptionDialog(myFrame
				, "Do you really want to delete " + d.getWareHouseName() + " ?"
				, "Confirm Deletion"
				, JOptionPane.YES_NO_OPTION
			    , JOptionPane.QUESTION_MESSAGE
			    , null
			    , options
				, options[1]) == JOptionPane.NO_OPTION) {
			return;
		}

		//tell the controller to do the deletion
		parent.doCommand(MenuCommands.DELETE_WAREHOUSE, this);
		
	}
	
	/**
	 * Opens a WarehouseDetailView with the given Warehouse object
	 */
	public void openDetailView() {
		parent.doCommand(MenuCommands.SHOW_DETAIL_WAREHOUSE, this);
	}
	
	/**
	 * returns selected person in list
	 * @return
	 */
	public Warehouse getSelectedWarehouse() {
		return selectedModel;
	}

	/**
	 * Subclass-specific cleanup
	 */
	@Override
	protected void cleanup() {
		//let superclass do its thing
		super.cleanup();
				
		//unregister from observables
		myList.unregisterAsObserver();
	}

	/**
	 * Accessors for WarehouseListController
	 * @return
	 */
	public WarehouseListController getMyList() {
		return myList;
	}

	public void setMyList(WarehouseListController myList) {
		this.myList = myList;
	}

	public JList<Warehouse> getListWarehouses() {
		return listWarehouses;
	}

	public void setListWarehouses(JList<Warehouse> listPeople) {
		this.listWarehouses = listPeople;
	}

	public Warehouse getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(Warehouse selectedModel) {
		this.selectedModel = selectedModel;
	}
	
	private class WarehouseDragTransferHandler extends TransferHandler {
		private int index = 0;

		public int getSourceActions(JComponent comp) {
	        return COPY_OR_MOVE;
	    }
				
		public Transferable createTransferable(JComponent comp) {
	        index = listWarehouses.getSelectedIndex();
	        if (index < 0 || index >= myList.getSize()) {
	            return null;
	        }
	        return new TransferableWarehouse( (Warehouse) listWarehouses.getSelectedValue());
	    }
	    
	    public void exportDone(JComponent comp, Transferable trans, int action) {
	        if (action != MOVE) {
	            return;
	        }
	    }
	}
}
