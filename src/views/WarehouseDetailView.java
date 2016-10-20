package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.MDIChild;
import controller.MDIParent;
import database.GatewayException;
import models.Warehouse;

/**
 * Editable view of Warehouse model
 * @author Justin Wilson
 *
 */
public class WarehouseDetailView extends MDIChild implements Observer {
	/**
	 * Warehouse object shown in view instance
	 */
	private Warehouse myWarehouse;
	
	/**
	 * Fields for Warehouse data access
	 */
	private JLabel fldId;
	private JTextField fldName, fldAdd, fldCity, fldState;
	private JTextField fldZip, fldStoreCap;
		
	/**
	 * Constructor
	 * @param title
	 */
	public WarehouseDetailView(String title, Warehouse d, MDIParent m) {
		super(title, m);
		
		myWarehouse = d;

		//register as an observer
		myWarehouse.addObserver(this);
		
		//prep layout and fields
		JPanel panel = new JPanel(); 
		panel.setLayout(new GridLayout(12, 2, 5, 3));
		
		//init fields to record data
		panel.add(new JLabel("Id"));
		fldId = new JLabel("");
		panel.add(fldId);
		
		
		panel.add(new JLabel("Name"));
		fldName = new JTextField("");
		fldName.addKeyListener(new TextfieldChangeListener());
		panel.add(fldName);
		
		panel.add(new JLabel("Address"));
		fldAdd = new JTextField("");
		fldAdd.addKeyListener(new TextfieldChangeListener());
		panel.add(fldAdd);
		
		panel.add(new JLabel("City"));
		fldCity = new JTextField("");
		fldCity.addKeyListener(new TextfieldChangeListener());
		panel.add(fldCity);
		
		panel.add(new JLabel("State"));
		fldState = new JTextField("");
		fldState.addKeyListener(new TextfieldChangeListener());
		panel.add(fldState);
		
		panel.add(new JLabel("ZipCode"));
		fldZip = new JTextField("");
		fldZip.addKeyListener(new TextfieldChangeListener());
		panel.add(fldZip);
		
		panel.add(new JLabel("Storage Capacity"));
		fldStoreCap = new JTextField("");
		fldStoreCap.addKeyListener(new TextfieldChangeListener());
		panel.add(fldStoreCap);
				

		this.add(panel, BorderLayout.CENTER);
		
		//add a Save button to write field changes back to model data
		panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JButton button = new JButton("Save Record");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveModel();
			}
		});
		panel.add(button);
		
		this.add(panel, BorderLayout.SOUTH);

		//load fields with model data
		refreshFields();
		
		//can't call this on JPanel
		//this.pack();
		this.setPreferredSize(new Dimension(360, 210));
	}
	
	/**
	 * Reload fields with model data
	 * Used when model notifies view of change
	 */
	public void refreshFields() {
		fldId.setText("" + myWarehouse.getId());
		fldName.setText( myWarehouse.getWareHouseName());
		fldAdd.setText( myWarehouse.getAddress());
		fldZip.setText( "" + myWarehouse.getZip());
		fldStoreCap.setText( "" + myWarehouse.getStorageCapacity());
		fldCity.setText( myWarehouse.getCity());
		fldState.setText( myWarehouse.getState());
		//update window title
		this.setTitle(myWarehouse.getWareHouseName());
		//flag as unchanged
		setChanged(false);
	}

	/**
	 * saves changes to the view's Warehouse model 
	 */
	//if any of them fail then no fields should be changed
	//and previous values reloaded
	//this is called rollback
	@Override
	public boolean saveModel() {
		//display any error message if field data are invalid
				String testName = fldName.getText().trim();
				if(!myWarehouse.validWareHouseName(testName)) {
					parent.displayChildMessage("Invalid Warehouse name!");
					refreshFields();
					return false;
				}
				String testAdd = fldAdd.getText().trim();
				if(!myWarehouse.validAddress(testAdd)) {
					parent.displayChildMessage("Invalid Address!");
					refreshFields();
					return false;
				}
				String testCity = fldCity.getText().trim();
				if(!myWarehouse.validCity(testCity)) {
					parent.displayChildMessage("Invalid City!");
					refreshFields();
					return false;
				}
				String testState = fldState.getText().trim();
				if(!myWarehouse.validState(testState)) {
					parent.displayChildMessage("Invalid State!");
					refreshFields();
					return false;
				}
				String testZip = fldZip.getText().trim();
				if(!myWarehouse.validZip(testZip)) {
					parent.displayChildMessage("Invalid ZipCode!");
					refreshFields();
					return false;
				}
				
				Long testStorageCap = 0L;
				try {
					testStorageCap = Long.parseLong(fldStoreCap.getText().trim());
				} catch(Exception e) {
					parent.displayChildMessage("Invalid Storage Capacity!");
					refreshFields();
					return false;
				}
				
				if(!myWarehouse.validStorageCap(testStorageCap)) {
					parent.displayChildMessage("Invalid Storage Capacity!");
					refreshFields();
					return false;
				}

		//fields are valid so save to model
		try {
			myWarehouse.setWareHouseName(testName);
			myWarehouse.setAddress(testAdd);
			myWarehouse.setZip(testZip);
			myWarehouse.setStorageCapacity(testStorageCap);
			myWarehouse.setCity(testCity);
			myWarehouse.setState(testState);
		} catch(Exception e) {
			parent.displayChildMessage(e.getMessage());
			refreshFields();
			return false;
		}
		
		//tell model that update is done (in case it needs to notify observers
		try {
			myWarehouse.finishUpdate();
			setChanged(false);
			
		} catch (GatewayException e) {
			//e.printStackTrace();
			//reset fields to db copy of warehouse if save fails
			refreshFields();
			parent.displayChildMessage(e.getMessage());
			return false;
		}
		
		parent.displayChildMessage("Changes saved");
		return true;
	}

	/**
	 * Subclass-specific cleanup
	 */
	@Override
	protected void cleanup() {
		//let superclass do its thing
		super.cleanup();
				
		//unregister from observable
		myWarehouse.deleteObserver(this);
	}

	/**
	 * Called by Observable
	 */
	@Override
	public void update(Observable o, Object arg) {
		refreshFields();
	}

	public Warehouse getMyWarehouse() {
		return myWarehouse;
	}

	public void setMyWarehouse(Warehouse myWarehouse) {
		this.myWarehouse = myWarehouse;
	}
	
	private class TextfieldChangeListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
			//any typing in a text field flags view as having changed
			setChanged(true);
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}
}
