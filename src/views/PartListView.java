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

import controller.PartListController;
import database.GatewayException;
import controller.MDIChild;
import controller.MDIParent;
import controller.MenuCommands;
import models.InventoryList;
import models.Part;
import models.TransferablePart;

/**
 * Displays a list of Part objects
 * Double click Part creates and shows detail view of that Part object 

 * NOTE: this view is now coupled with MDI controller classes
 * In order for it to possibly communicate with the MDI master/parent frame
 * @author Justin Wilson
 *
 */
public class PartListView extends MDIChild {	
	/**
	 * GUI instance variables
	 */
	private JList<Part> listParts;
	private PartListController myList;
	//saves reference to last selected model in JList
	//parent asks fo;r this when opening a detail view
	private Part selectedModel;
	
	private InventoryList myInventoryList;
	
	/**
	 * Constructor
	 * @param title Window title
	 * @param list PartListController contains collection of Part objects
	 * @param mdiParent MasterFrame MDI parent window reference
	 */
	public PartListView(String title, PartListController list, MDIParent m) {
		super(title, m);
		
		//set self to list's view (allows ListModel to tell this view to repaint when models change)
		//PartListController is an observer of the models
		list.setMyListView(this);
		
		myInventoryList = m.getInventoryList();
		
		//prep list view
		myList = list;
		listParts = new JList<Part>(myList);
		//allow drag and drop from part list to person detail view part list
		listParts.setDragEnabled(true);
		listParts.setTransferHandler(new PartDragTransferHandler());
		
		//use our custom cell renderer instead of default (don't want to use Part.toString())
		listParts.setCellRenderer(new PartListCellRenderer());
		listParts.setPreferredSize(new Dimension(200, 200));
		
		//add event handler for double click
		listParts.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				//if double-click then get index and open new detail view with record at that index
		        if(evt.getClickCount() == 2) {
		        	
		        	// check User permission
		    		if( ! m.getUserSessionOption("edit") ){
		    			m.displayChildMessage("Sorry !! You don't have permisstion to Edit Part");
		    			return;
		    		}else{
		        	
			        	int index = listParts.locationToIndex(evt.getPoint());
			        	//get the Part at that index
			        	selectedModel = myList.getElementAt(index);
			        	
			        	// check lock element
			        	try {
							if(selectedModel.getGateway().blockPart( selectedModel.getId() , m.getUserSessionID())){
								m.displayChildMessage("Sorry !! This Part is blocked because another user is editing it");
								return;
							}
						} catch (GatewayException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        	
			        	//open a new detail view
			        	openDetailView();
		    		}
		        }
		    }
		});
		
		//add to content pane
		this.add(new JScrollPane(listParts));
		
		//add a Delete button to delete selected person
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton button = new JButton("Delete Part");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deletePart();
			}
		});
		panel.add(button);
		
		this.add(panel, BorderLayout.SOUTH);

		this.setPreferredSize(new Dimension(240, 200));
	}

	/**
	 * Tells MDI parent to delete the selected part. if none selected then ignore
	 */
	private void deletePart() {
		//get the selected model and set as selectedPart instance variable
		//mdi parent will ask for this when handling delete person call
		int idx = listParts.getSelectedIndex();
		if(idx < 0)
			return;
		//idx COULD end up > list size so make sure idx is < list size
		if(idx >= myList.getSize())
			return;
		Part d = myList.getElementAt(idx);
		if(d == null)
			return;
		selectedModel = d;
		
		// check exist in inventory
		if( myInventoryList.existWarehousePart( 0l, d.getId()) ){
			
			parent.displayChildMessage("Can not delete this part because it exists in Inventory table");
			return;
			
		}
		
		//ask user to confirm deletion
		String [] options = {"Yes", "No"};
		if(JOptionPane.showOptionDialog(myFrame
				, "Do you really want to delete " + d.getPartNumber() + " ?"
				, "Confirm Deletion"
				, JOptionPane.YES_NO_OPTION
			    , JOptionPane.QUESTION_MESSAGE
			    , null
			    , options
				, options[1]) == JOptionPane.NO_OPTION) {
			return;
		}

		//tell the controller to do the deletion
		parent.doCommand(MenuCommands.DELETE_PART, this);
		
	}
	
	/**
	 * Opens a PartDetailView with the given Part object
	 */
	public void openDetailView() {
		parent.doCommand(MenuCommands.SHOW_DETAIL_PART, this);
	}
	
	/**
	 * returns selected person in list
	 * @return
	 */
	public Part getSelectedPart() {
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
	 * Accessors for PartListController
	 * @return
	 */
	public PartListController getMyList() {
		return myList;
	}

	public void setMyList(PartListController myList) {
		this.myList = myList;
	}

	public JList<Part> getListParts() {
		return listParts;
	}

	public void setListParts(JList<Part> listPeople) {
		this.listParts = listPeople;
	}

	public Part getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(Part selectedModel) {
		this.selectedModel = selectedModel;
	}
	
	private class PartDragTransferHandler extends TransferHandler {
		private int index = 0;

		public int getSourceActions(JComponent comp) {
	        return COPY_OR_MOVE;
	    }
				
		public Transferable createTransferable(JComponent comp) {
	        index = listParts.getSelectedIndex();
	        if (index < 0 || index >= myList.getSize()) {
	            return null;
	        }
	        return new TransferablePart( (Part) listParts.getSelectedValue());
	    }
	    
	    public void exportDone(JComponent comp, Transferable trans, int action) {
	        if (action != MOVE) {
	            return;
	        }
	    }
	}
}
