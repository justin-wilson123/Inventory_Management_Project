package controller;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import database.PartTableGateway;
import database.PartTableGatewayMySQL;
import database.UserTableGateway;
import database.UserTableGatewayMySQL;
import database.WarehouseTableGateway;
import database.WarehouseTableGatewayMySQL;
import database.GatewayException;
import database.InventoryTableGateway;
import database.InventoryTableGatewayMySQL;
import models.InventoryList;
import models.PartList;
import models.UserList;
import models.WarehouseList;

/**
 * Starting class for this application
 * CS 4743 Assignment 5 by Justin Wilson
 * @author Justin Wilson
 *
 */
public class Launcher {

	/**
	 * Configures and Launches initial view(s) of the application on the Event Dispatch Thread
	 */
	public static void createAndShowGUI() {
		//create a model table gateways; abort if fails (need a db connection)
		WarehouseTableGateway wtg = null;
		PartTableGateway ptg = null;
		InventoryTableGateway itg = null;
		UserTableGateway utg = null;
		try {

			wtg = new WarehouseTableGatewayMySQL();
			ptg = new PartTableGatewayMySQL();
			itg = new InventoryTableGatewayMySQL();
			utg = new UserTableGatewayMySQL();

		} catch (GatewayException e) {
			JOptionPane.showMessageDialog(null, "Database is not responding. Please reboot your computer and maybe the database will magically appear (not really).", "Database Offline!", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		//init model(s); do an initial load from gateways 
		WarehouseList warehouseList = new WarehouseList();
		warehouseList.setGateway(wtg);
		warehouseList.loadFromGateway();
		
		PartList partList = new PartList();
		partList.setGateway(ptg);
		partList.loadFromGateway();
		
		InventoryList inventoryList = new InventoryList();
		inventoryList.setGateway(itg);
		inventoryList.loadFromGateway();
		
		UserList userList = new UserList();
		userList.setGateway(utg);
		userList.loadFromGateway();
		
		MDIParent appFrame = new MDIParent("CS 4743 Assignment 5 ", warehouseList, partList, inventoryList, userList);
		
		//use exit on close if you only want windowClosing to be called (can abort closing here also)
		//appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//use dispose on close if you want windowClosed to also be called
		appFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		//need to set initial size of MDI frame
		appFrame.setSize(640, 480);
		
		appFrame.setVisible(true);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
