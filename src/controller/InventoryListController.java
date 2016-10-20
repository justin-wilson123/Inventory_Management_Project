package controller;

import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractListModel;

import models.Inventory;
import models.InventoryList;

/**
 * Provides the InventoryList data to be used by InventoryListView JLists
 *  
 * @author Justin Wilson
 *
 */
public class InventoryListController extends AbstractListModel<Inventory> implements Observer {
	/**
	 * Collection for storing Person object refs
	 * Leave it abstract so we can possibly use different concrete list subclasses
	 */
	private InventoryList myList;
	
	/**
	 * GUI container housing this object's list controller's JList
	 * Allows this controller to tell the view to repaint() if models in list change
	 */
	private MDIChild myListView;
	
	public InventoryListController(InventoryList il) {
		super();
		myList = il;
		
		//register as observer to person list
		il.addObserver(this);
	}
	
	@Override
	public int getSize() {
		return myList.getList().size();
	}

	@Override
	public Inventory getElementAt(int index) {
		if(index >= getSize())
			throw new IndexOutOfBoundsException("Index " + index + " is out of list bounds!");
		return myList.getList().get(index);
	}

	public MDIChild getMyListView() {
		return myListView;
	}

	public void setMyListView(MDIChild myListView) {
		this.myListView = myListView;
	}

	/**
	 * unregister with person list as observer
	 */
	public void unregisterAsObserver() {
		myList.deleteObserver(this);
	}

	//model tells this observer that it has changed
	//so tell JList's view to repaint itself now
	@Override
	public void update(Observable o, Object arg) {
		//System.out.println("PersonListController update");
		fireContentsChanged(this, 0, getSize());
		myListView.repaint();
	}
}
