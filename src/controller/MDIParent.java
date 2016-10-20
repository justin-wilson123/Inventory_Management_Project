package controller;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import database.GatewayException;
import models.Inventory;
import models.InventoryList;
import models.Part;
import models.PartList;
import models.User;
import models.UserList;
import models.Warehouse;
import models.WarehouseList;
import reports.ReportException;
import reports.ReportGatewayMySQL;
import reports.WarehousePartReportExcel;
import reports.WarehousePartReportPDF;
import utilities.UserSession;
import views.InventoryDetailView;
import views.InventoryListView;
import views.LoginView;
import views.PartDetailView;
import views.PartListView;
import views.UserDetailView;
import views.UserListView;
import views.WarehouseDetailView;
import views.WarehouseListView;

/**
 * MasterFrame : a little MDI skeleton that has communication from child to JInternalFrame 
 * 					and from child to the top-level JFrame (MasterFrame)  
 * @author Justin Wilson
 *
 */
public class MDIParent extends JFrame implements WindowListener {
	private static final long serialVersionUID = 1L;
	private JDesktopPane desktop;
	private int newFrameX = 0, newFrameY = 0; //used to cascade or stagger starting x,y of JInternalFrames
	
	//models and model-controllers
	private WarehouseList warehouseList;
	private PartList partList;
	private InventoryList inventoryList;
	private UserList userList;
	
	private UserSession userSession = null;
	
	//keep a list of currently open views
	//useful if the MDIParent needs to act on the open views or see if an instance is already open
	private List<MDIChild> openViews;
		
	public MDIParent(String title, WarehouseList wList, PartList pList, InventoryList iList, UserList uList) {
		super(title);
		
		userSession = new UserSession();
		
		
		warehouseList = wList;
		partList = pList;
		inventoryList = iList;
		userList = uList;
		
		
		
		//init the view list
		openViews = new LinkedList<MDIChild>();
		
		//create menu for adding inner frames
		MDIMenu menuBar = new MDIMenu(this);
		setJMenuBar(menuBar);
		   
		//create the MDI desktop
		desktop = new JDesktopPane();
		add(desktop);
		
		this.addWindowListener(this);

		//add shutdown hook to clean up properly even when VM quits (e.g., Command-Q)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cleanup();
			}
		});

	}
	
	/**
	 * responds to menu events or action calls from child windows (e.g., opening a detail view)
	 * @param cmd Command to perform (e.g., show detail of Person object)
	 * @param caller Calling child window reference in case Command requires more info from caller (e.g., selected Person)
	 */
	public void doCommand(MenuCommands cmd, Container caller) {
		switch(cmd) {
			case APP_QUIT :
				//close all child windows first
				//closeChildren();
				this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
				break;
				
			case SHOW_LIST_WAREHOUSES :
				//sync warehouse list with db contents using id map to avoid duplicating or overwriting exist persons
				//already in the list (Identity Map)
				warehouseList.loadFromGateway();

				WarehouseListView w = new WarehouseListView("Warehouse List", new WarehouseListController(warehouseList), this);
				//v1.setSingleOpenOnly(true);
				openMDIChild(w);
				
				break;

			case SHOW_DETAIL_WAREHOUSE :
				Warehouse w1 = ((WarehouseListView) caller).getSelectedWarehouse();
				WarehouseDetailView vWarehouse = new WarehouseDetailView(w1.getWareHouseName(), w1, this);
				openMDIChild(vWarehouse);
				break;

			case ADD_WAREHOUSE :
				//if there is a new unsaved warehouse already in the list then show message and don't add a new one
				for(Warehouse dCheck : warehouseList.getList()) {
					if(dCheck.getId() == Warehouse.INVALID_ID || dCheck.getWareHouseName().equalsIgnoreCase(Warehouse.DEFAULT_EMPTY_WAREHOUSE)) {
						this.displayChildMessage("Please save changes to new Warehouse \"" + dCheck.getWareHouseName() + "\" before trying to add another.");
						return;
					}
				}
				//make a new Warehouse instance
				Warehouse wAdd = new Warehouse();
				
				//add the new warehouse to the model list and set its gateway
				warehouseList.addWarehouseToList(wAdd);
				warehouseList.addToNewRecords(wAdd);
				
				//lastly open a new warehouse detail using the newly added Person
				WarehouseDetailView vWarehouseAdd = new WarehouseDetailView(wAdd.getWareHouseName(), wAdd, this);
				openMDIChild(vWarehouseAdd);
				
				break;

			case DELETE_WAREHOUSE :
				//remove the model from the model list
				Warehouse wDelete = ((WarehouseListView) caller).getSelectedWarehouse();
				warehouseList.removeWarehouseFromList(wDelete);
				
				//close all details that are based on this object
				//NOTE: closing the detail view changes the openViews collection so traverse it in reverse
				for(int i = openViews.size() - 1; i >= 0; i--) {
					MDIChild c = openViews.get(i);
					if(c instanceof WarehouseDetailView) {
						//if detail view is showing the deleted object then close the detail view without asking
						if(((WarehouseDetailView) c).getMyWarehouse().getId() == wDelete.getId())
							c.closeFrame();
					}
				}
				
				//lastly, delete the warehouse from the db
				//NOTE: this will also delete all warehouse/owner relationships
				try {
					wDelete.delete();
					this.displayChildMessage("Warehouse deleted.");
				} catch (GatewayException e) {
					System.err.println(e.getMessage());
					this.displayChildMessage("Error trying to delete warehouse.");
				}
				break;
				
			case SHOW_LIST_PARTS :
				//sync part list with db contents using id map to avoid duplicating or overwriting exist persons
				//already in the list (Identity Map)
				partList.loadFromGateway();
				
				// update the datetime
				partList.getGateway().updateAccessTime(30);
				
				PartListView dv1 = new PartListView("Part List", new PartListController(partList), this);
				//v1.setSingleOpenOnly(true);
				openMDIChild(dv1);
				
				break;

			case SHOW_DETAIL_PART :
				Part d = ((PartListView) caller).getSelectedPart();
		    	PartDetailView vPart = new PartDetailView(d.getPartName(), d, this);
				openMDIChild(vPart);
				break;

			case ADD_PART :
				// check User permission
				if( ! this.getUserSessionOption("add") ){
					this.displayChildMessage("Sorry !! You don't have permisstion to Add Part");
					return;
				}
				
				//if there is a new unsaved part already in the list then show message and don't add a new one
				for(Part dCheck : partList.getList()) {
					if(dCheck.getId() == Part.INVALID_ID || dCheck.getPartName().equalsIgnoreCase(Part.DEFAULT_EMPTY_PART)) {
						this.displayChildMessage("Please save changes to new Part \"" + dCheck.getPartName() + "\" before trying to add another.");
						return;
					}
				}
				//make a new Part instance
				Part dAdd = new Part();
				
				//add the new part to the model list and set its gateway
				partList.addPartToList(dAdd);
				partList.addToNewRecords(dAdd);
				
				//lastly open a new part detail using the newly added Person
				PartDetailView vPartAdd = new PartDetailView(dAdd.getPartName(), dAdd, this);
				openMDIChild(vPartAdd);
				
				break;

			case DELETE_PART :
				// check the permissions
				if( ! this.getUserSessionOption("delete") ){
					this.displayChildMessage("Sorry !! You don't have permisstion to Delete Part");
					return;
				}
				
				//remove the model from the model list
				Part dDelete = ((PartListView) caller).getSelectedPart();
				partList.removePartFromList(dDelete);
				
				//close all details that are based on this object
				//NOTE: closing the detail view changes the openViews collection so traverse it in reverse
				for(int i = openViews.size() - 1; i >= 0; i--) {
					MDIChild c = openViews.get(i);
					if(c instanceof PartDetailView) {
						//if detail view is showing the deleted object then close the detail view without asking
						if(((PartDetailView) c).getMyPart().getId() == dDelete.getId())
							c.closeFrame();
					}
				}
				
				//lastly, delete the part from the db
				//NOTE: this will also delete all part/owner relationships
				try {
					dDelete.delete();
					this.displayChildMessage("Part deleted.");
				} catch (GatewayException e) {
					System.err.println(e.getMessage());
					this.displayChildMessage("Error trying to delete part.");
				}
				break;
								
			case SHOW_LIST_INVENTORY :
				//sync inventory list with db contents using id map to avoid duplicating or overwriting exist persons
				//already in the list (Identity Map)
				inventoryList.loadFromGateway();
				
				InventoryListView inv1 = new InventoryListView("Inventory List", new InventoryListController(inventoryList), this);
				//v1.setSingleOpenOnly(true);
				openMDIChild(inv1);
				
				break;

			case SHOW_DETAIL_INVENTORY :
				Inventory inv = ((InventoryListView) caller).getSelectedInventory();
				InventoryDetailView vinv = new InventoryDetailView(""+inv.getId(), inv, this);
				openMDIChild(vinv);
				break;

			case ADD_INVENTORY :
				//if there is a new unsaved inventory already in the list then show message and don't add a new one
				for(Inventory dCheck : inventoryList.getList()) {
					if(dCheck.getId() == Inventory.INVALID_ID) {
						this.displayChildMessage("Please save changes to new Inventory \"" + dCheck.getId() + "\" before trying to add another.");
						return;
					}
				}
				//make a new Part instance
				Inventory iAdd = new Inventory();
				
				//add the new part to the model list and set its gateway
				inventoryList.addInventoryToList(iAdd);
				inventoryList.addToNewRecords(iAdd);
				
				//lastly open a new inventory detail using the newly added Person
				InventoryDetailView vInventoryAdd = new InventoryDetailView(""+ iAdd.getId(), iAdd, this);
				openMDIChild(vInventoryAdd);
				
				break;

			case DELETE_INVENTORY :
				//remove the model from the model list
				Inventory iDelete = ((InventoryListView) caller).getSelectedInventory();
				inventoryList.removeInventoryFromList(iDelete);
				
				//close all details that are based on this object
				//NOTE: closing the detail view changes the openViews collection so traverse it in reverse
				for(int i = openViews.size() - 1; i >= 0; i--) {
					MDIChild c = openViews.get(i);
					if(c instanceof InventoryDetailView) {
						//if detail view is showing the deleted object then close the detail view without asking
						if(((InventoryDetailView) c).getMyInventory().getId() == iDelete.getId())
							c.closeFrame();
					}
				}
				
				//lastly, delete the inventory from the db
				//NOTE: this will also delete all part/owner relationships
				try {
					iDelete.delete();
					this.displayChildMessage("Inventory deleted.");
				} catch (GatewayException e) {
					System.err.println(e.getMessage());
					this.displayChildMessage("Error trying to delete Inventory.");
				}
				break;	
				
			case SHOW_LOGOUT:
				
				userSession = new UserSession();
				setTitle("No user is active now");
				break;
			
			
			case SHOW_LOGIN :
				//sync person list with db contents using id map to avoid duplicating or overwriting exist persons
				//already in the list (Identity Map)
				
				LoginView dv2 = new LoginView("Login page", userList.getGateway(), this);
	
				openMDIChild(dv2);
				
				break;	
		
	
			case SHOW_LIST_USERS :
				//sync person list with db contents using id map to avoid duplicating or overwriting exist persons
				//already in the list (Identity Map)
				userList.loadFromGateway();
				
				UserListView user1 = new UserListView("User List", new UserListController(userList), this);
				//v1.setSingleOpenOnly(true);
				openMDIChild(user1);
				
				break;
	
			case SHOW_DETAIL_USER :
				User u = ((UserListView) caller).getSelectedUser();
		    	UserDetailView vUser = new UserDetailView(u.getFullname(), u, this);
				openMDIChild(vUser);
				break;
	
			case ADD_USER :
				//if there is a new unsaved person already in the list then show message and don't add a new one
				for(User dCheck : userList.getList()) {
					if(dCheck.getId() == User.INVALID_ID ) {
						this.displayChildMessage("Please save changes to new User \"" + dCheck.getFullname() + "\" before trying to add another.");
						return;
					}
				}
				//make a new User instance
				User uAdd = new User();
				
				//add the new user to the model list and set its gateway
				userList.addUserToList(uAdd);
				userList.addToNewRecords(uAdd);
				
				//lastly open a new person detail using the newly added Person
				UserDetailView vUserAdd = new UserDetailView(uAdd.getFullname(), uAdd, this);
				openMDIChild(vUserAdd);
				
				break;
	
			case DELETE_USER :
				//remove the model from the model list
				User uDelete = ((UserListView) caller).getSelectedUser();
				userList.removeUserFromList(uDelete);
				
				//close all details that are based on this object
				//NOTE: closing the detail view changes the openViews collection so traverse it in reverse
				for(int i = openViews.size() - 1; i >= 0; i--) {
					MDIChild c = openViews.get(i);
					if(c instanceof UserDetailView) {
						//if detail view is showing the deleted object then close the detail view without asking
						if(((UserDetailView) c).getMyUser().getId() == uDelete.getId())
							c.closeFrame();
					}
				}
				
				//lastly, delete the user from the db
				//NOTE: this will also delete all user/owner relationships
				try {
					uDelete.delete();
					this.displayChildMessage("user deleted.");
				} catch (GatewayException e) {
					System.err.println(e.getMessage());
					this.displayChildMessage("Error trying to delete user.");
				}
				break;
				
				case PDF_REPORT :
					try {
						String fileName_pdf = "report.pdf";
						
						WarehousePartReportPDF report = new WarehousePartReportPDF(new ReportGatewayMySQL());
						report.generateReport();
						report.outputReportToFile( fileName_pdf );
						report.close();
					} catch (GatewayException | ReportException e) {
						this.displayChildMessage(e.getMessage());
						return;
					} 

				case EXCEL_REPORT :
					try {
						
						String fileName_xls = "report.xls";
						
						WarehousePartReportExcel report = new WarehousePartReportExcel(new ReportGatewayMySQL());
						report.generateReport();
						report.outputReportToFile( fileName_xls );
						report.close();
					} catch (GatewayException | ReportException e) {
						this.displayChildMessage(e.getMessage());
						return;
					} 	
		}
	}
	
	/**
	 * This method will always be called when the app quits since it hooks into the JVM
	 * Force all MDI child frames to call cleanup methods
	 * Children are NOT allowed to abort closing here
	 * NOTE: there is NO NEED to close any MDIChildFrames here. 
	 */
	public void cleanup() {
		//System.out.println("     *** In MDIParent.cleanup");

		//iterate through all child frames
		JInternalFrame [] children = desktop.getAllFrames();
		for(int i = children.length - 1; i >= 0; i--) {
			if(children[i] instanceof MDIChildFrame) {
				MDIChildFrame cf = (MDIChildFrame) children[i];
				//tell child frame to cleanup which then tells its child view to clean up
				cf.cleanup();
			}
		}
		//close any model table gateways
		warehouseList.getGateway().close();
		partList.getGateway().close();
	}

	/**
	 * create the child panel, insert it into a JInternalFrame and show it
	 * @param child
	 * @return
	 */
	public JInternalFrame openMDIChild(MDIChild child) {
		//first, if child's class is single open only and already open,
		//then restore and show that child
		//System.out.println(openViewNames.contains(child));
		if(child.isSingleOpenOnly()) {
			for(MDIChild testChild : openViews) {
				if(testChild.getClass().getSimpleName().equals(child.getClass().getSimpleName())) {
					try {
						testChild.restoreWindowState();
					} catch(PropertyVetoException e) {
						e.printStackTrace();
					}
					JInternalFrame c = (JInternalFrame) testChild.getMDIChildFrame();
					return c;
				}
			}
		}
		
		//create then new frame and set it in child
		MDIChildFrame frame = new MDIChildFrame(child.getTitle(), true, true, true, true, child);
		child.setMyFrame(frame);
				
		//pack works but the child panels need to use setPreferredSize to tell pack how much space they need
		//otherwise, MDI children default to a standard size that I find too small
		frame.pack();
		frame.setLocation(newFrameX, newFrameY);
		
		//tile its position
		newFrameX = (newFrameX + 10) % desktop.getWidth(); 
		newFrameY = (newFrameY + 10) % desktop.getHeight(); 
		desktop.add(frame);
		//show it
		frame.setVisible(true);
		
		//add child to openViews
		openViews.add(child);
				
		return frame;
	}
	
	//display a child's message in a dialog centered on MDI frame
	public void displayChildMessage(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}
	
	/**
	 * When MDIChild closes, we need to unregister it from the list of open views
	 * @param child
	 */
	public void removeFromOpenViews(MDIChild child) {
		openViews.remove(child);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method is called when we select Quit on menu OR click the close button on the window title bar
	 * Check each MDIChild to see if its changed and we cannot close. If MDIChild says we can't close then abort close. 
	 * @param e
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		//ask each MDIChild if it is ok to close 
		//System.out.println("     *** In MDIParent.windowClosing");

		//iterate through all child frames
		JInternalFrame [] children = desktop.getAllFrames();
		for(int i = children.length - 1; i >= 0; i--) {
			if(children[i] instanceof MDIChildFrame) {
				MDIChildFrame cf = (MDIChildFrame) children[i];
				if(!cf.okToClose())
					return;
			}
		}
		
		//if we get here then ok to close MDI parent (also closes all child frames)
		this.dispose();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
	
	
	// get warehouse list
	public WarehouseList getWarehouseList(){
		return warehouseList;
	}
	
	// get part list
	public PartList getPartList(){
		return partList;
	}
	
	// get inventory list
	public InventoryList getInventoryList(){
		return inventoryList;
	}
	

	public void setUserSession(User u){
		setTitle("User Access control of " + u.getUser()+ " ---- ADD: "+u.getAdd() + " | EDIT: "+u.getEdit() + "| DELETE: "+u.getDelete());
		this.userSession = new UserSession(u);
	}
	
	public boolean getUserSessionOption(String type){
		
		boolean value = false;
		
		type = type.toLowerCase();
		
		if( type.equals("add") ){			
			value = userSession.checkAdd();			
		}else if( type.equals("edit") ){			
					value = userSession.checkEdit();					
				} else if( type.equals("delete") ){
							value = userSession.checkDelete();
						}					
		
		return value;
	}
	
	public String getUserSessionID(){
		return userSession.getUser();
	}
	
}
