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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.MDIChild;
import controller.MDIParent;
import database.GatewayException;
import models.Part;



/**
 * Editable view of Part model
 * @author Justin Wilson
 *
 */
public class PartDetailView extends MDIChild implements Observer {
	/**
	 * Part object shown in view instance
	 */
	private Part myPart;
	
	/**
	 * Fields for Part data access
	 */
	private JLabel fldId;
	private JTextField fldPartId, fldPartName, fldPartVendor, fldPartUnit, fldPartVendorId;
		
	/**
	 * Constructor
	 * @param title
	 */
	public PartDetailView(String title, Part d, MDIParent m) {
		super(title, m);
		
		myPart = d;

		//register as an observer
		myPart.addObserver(this);
		
		//prep layout and fields
		JPanel panel = new JPanel(); 
		
		panel.setLayout(new GridLayout( 8, 2, 5, 3));
		//init fields to record data
		panel.add(new JLabel("Id"));
		fldId = new JLabel("");
		panel.add(fldId);
		
		
		panel.add(new JLabel("Part Number"));
		fldPartId = new JTextField("");
		fldPartId.addKeyListener(new TextfieldChangeListener());
		panel.add(fldPartId);
		
		
		panel.add(new JLabel("Name"));
		fldPartName = new JTextField("");
		fldPartName.addKeyListener(new TextfieldChangeListener());
		panel.add(fldPartName);
		
		panel.add(new JLabel("Vendor"));
		fldPartVendor = new JTextField("");
		fldPartVendor.addKeyListener(new TextfieldChangeListener());
		panel.add(fldPartVendor);
		
		panel.add(new JLabel("Unit of Quality"));
		fldPartUnit = new JTextField("");
		fldPartUnit.addKeyListener(new TextfieldChangeListener());
		panel.add(fldPartUnit);
		
		panel.add(new JLabel("Vendor's Id"));
		fldPartVendorId = new JTextField("");
		fldPartVendorId.addKeyListener(new TextfieldChangeListener());
		panel.add(fldPartVendorId);
		

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
		this.setPreferredSize(new Dimension(400, 260));
	}
	
	/**
	 * Reload fields with model data
	 * Used when model notifies view of change
	 */
	public void refreshFields() {
		fldId.setText("" + myPart.getId());
		fldPartId.setText(myPart.getPartNumber());
		fldPartName.setText(myPart.getPartName());
		fldPartVendor.setText("" + myPart.getVendor());
		fldPartUnit.setText("" + myPart.getUnitOfQuantity());
		fldPartVendorId.setText("" + myPart.getVendorsPartNumber());
		//update window title
		this.setTitle(myPart.getPartName());
		//flag as unchanged
		setChanged(false);
	}

	/**
	 * saves changes to the view's Part model 
	 */
	//if any of them fail then no fields should be changed
	//and previous values reloaded
	//this is called rollback
	@Override
	public boolean saveModel() {
		//display any error message if field data are invalid
		String testPartId = fldPartId.getText().trim();
		if(!myPart.validPartNumber(testPartId)) {
			parent.displayChildMessage(Part.ERRORMSG_INVALID_PART_NUMBER);
			refreshFields();
			return false;
		}
		
		String testPartName = fldPartName.getText().trim();
		if(!myPart.validPartName(testPartName)) {
			parent.displayChildMessage(Part.ERRORMSG_INVALID_PART_NAME);
			refreshFields();
			return false;
		}
		
		String testPartVendor = fldPartVendor.getText().trim();
		if(!myPart.validVendor(testPartVendor)) {
			parent.displayChildMessage(Part.ERRORMSG_INVALID_VENDOR);
			refreshFields();
			return false;
		}
		
		// check unit

		String testPartUnit = fldPartUnit.getText().trim();
		
		String testPartVendorId = fldPartVendorId.getText().trim();
		if(!myPart.validVendorPart(testPartVendorId)) {
			parent.displayChildMessage(Part.ERRORMSG_INVALID_VENDOR_ID);
			refreshFields();
			return false;
		}

		//fields are valid so save to model
		try {
			myPart.setPartNumber(testPartId);
			myPart.setPartName(testPartName);
			myPart.setVendor(testPartVendor);
			myPart.setUnitOfQuantity(testPartUnit);
			myPart.setVendorsPartNumber(testPartVendorId);
		} catch(Exception e) {
			parent.displayChildMessage(e.getMessage());
			refreshFields();
			return false;
		}
		
		//tell model that update is done (in case it needs to notify observers
		try {
			myPart.finishUpdate();
			setChanged(false);
			
		} catch (GatewayException e) {
			//e.printStackTrace();
			//reset fields to db copy of part if save fails
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
		
		// clear user access
		try {
			myPart.getGateway().blockPart( myPart.getId() , null);
		} catch (GatewayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		//unregister from observable
		myPart.deleteObserver(this);
	}

	/**
	 * Called by Observable
	 */
	@Override
	public void update(Observable o, Object arg) {
		refreshFields();
	}

	public Part getMyPart() {
		return myPart;
	}

	public void setMyPart(Part myPart) {
		this.myPart = myPart;
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
