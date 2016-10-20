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

import controller.InventoryListController;
import controller.MDIChild;
import controller.MDIParent;
import controller.MenuCommands;
import models.Inventory;
import models.TransferableInventory;

/**
 * Displays a list of Inventory objects
 * Double click Inventory creates and shows detail view of that Inventory object 

 * NOTE: this view is now coupled with MDI controller classes
 * In order for it to possibly communicate with the MDI master/parent frame
 * @author Justin Wilson
 *
 */
public class InventoryListView extends MDIChild {	
	/**
	 * GUI instance variables
	 */
	private JList<Inventory> listInventorys;
	private InventoryListController myList;
	//saves reference to last selected model in JList
	//parent asks for this when opening a detail view
	private Inventory selectedModel;
	
	/**
	 * Constructor
	 * @param title Window title
	 * @param list InventoryListController contains collection of Inventory objects
	 * @param mdiParent MasterFrame MDI parent window reference
	 */
	public InventoryListView(String title, InventoryListController list, MDIParent m) {
		super(title, m);
		
		//set self to list's view (allows ListModel to tell this view to repaint when models change)
		//InventoryListController is an observer of the models
		list.setMyListView(this);
		
		//prep list view
		myList = list;
		listInventorys = new JList<Inventory>(myList);
		//allow drag and drop from inventoy list to person detail view inventoy list
		listInventorys.setDragEnabled(true);
		listInventorys.setTransferHandler(new InventoryDragTransferHandler());
		
		//use our custom cell renderer instead of default (don't want to use Inventory.toString())
		listInventorys.setCellRenderer(new InventoryListCellRenderer());
		listInventorys.setPreferredSize(new Dimension(200, 200));
		
		//add event handler for double click
		listInventorys.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				//if double-click then get index and open new detail view with record at that index
		        if(evt.getClickCount() == 2) {
		        	int index = listInventorys.locationToIndex(evt.getPoint());
		        	//get the Inventory at that index
		        	selectedModel = myList.getElementAt(index);
		        	
		        	//open a new detail view
		        	openDetailView();
		        }
		    }
		});
		
		//add to content pane
		this.add(new JScrollPane(listInventorys));
		
		//add a Delete button to delete selected person
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton button = new JButton("Delete Inventory");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteInventory();
			}
		});
		panel.add(button);
		
		this.add(panel, BorderLayout.SOUTH);

		this.setPreferredSize(new Dimension(240, 200));
	}

	/**
	 * Tells MDI parent to delete the selected inventoy. if none selected then ignore
	 */
	private void deleteInventory() {
		//get the selected model and set as selectedInventory instance variable
		//mdi parent will ask for this when handling delete person call
		int idx = listInventorys.getSelectedIndex();
		if(idx < 0)
			return;
		//idx COULD end up > list size so make sure idx is < list size
		if(idx >= myList.getSize())
			return;
		Inventory d = myList.getElementAt(idx);
		if(d == null)
			return;
		selectedModel = d;
		
		//ask user to confirm deletion
		String [] options = {"Yes", "No"};
		if(JOptionPane.showOptionDialog(myFrame
				, "Do you really want to delete " + d.getId() + " ?"
				, "Confirm Deletion"
				, JOptionPane.YES_NO_OPTION
			    , JOptionPane.QUESTION_MESSAGE
			    , null
			    , options
				, options[1]) == JOptionPane.NO_OPTION) {
			return;
		}

		//tell the controller to do the deletion
		parent.doCommand(MenuCommands.DELETE_INVENTORY, this);
		
	}
	
	/**
	 * Opens a InventoryDetailView with the given Inventory object
	 */
	public void openDetailView() {
		parent.doCommand(MenuCommands.SHOW_DETAIL_INVENTORY, this);
	}
	
	/**
	 * returns selected person in list
	 * @return
	 */
	public Inventory getSelectedInventory() {
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
	 * Accessors for InventoryListController
	 * @return
	 */
	public InventoryListController getMyList() {
		return myList;
	}

	public void setMyList(InventoryListController myList) {
		this.myList = myList;
	}

	public JList<Inventory> getListInventorys() {
		return listInventorys;
	}

	public void setListInventorys(JList<Inventory> listPeople) {
		this.listInventorys = listPeople;
	}

	public Inventory getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(Inventory selectedModel) {
		this.selectedModel = selectedModel;
	}
	
	private class InventoryDragTransferHandler extends TransferHandler {
		private int index = 0;

		public int getSourceActions(JComponent comp) {
	        return COPY_OR_MOVE;
	    }
				
		public Transferable createTransferable(JComponent comp) {
	        index = listInventorys.getSelectedIndex();
	        if (index < 0 || index >= myList.getSize()) {
	            return null;
	        }
	        return new TransferableInventory( (Inventory) listInventorys.getSelectedValue());
	    }
	    
	    public void exportDone(JComponent comp, Transferable trans, int action) {
	        if (action != MOVE) {
	            return;
	        }
	    }
	}
}
