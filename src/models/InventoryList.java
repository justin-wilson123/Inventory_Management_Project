package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import database.InventoryTableGateway;
import database.GatewayException;

/**
 * A model that is a collection of Inventory models
 * @author Justin Wilson
 *
 */
public class InventoryList extends Observable implements Observer {
	/**
	 * Collection of Inventory objects that this list holds
	 */
	private List<Inventory> myList;
	
	/**
	 * Identity map for determining if Inventory is already in this list
	 */
	private HashMap<Long, Inventory> myIdMap;
	
	/**
	 * Collection of newly added records to know when to update key in Identity map
	 */
	private ArrayList<Inventory> newRecords;
	
	/**
	 * Database connection for the InventoryList 
	 */
	private InventoryTableGateway gateway;
	
	/**
	 * for multiple object inserts and deletes, set to true to notifyObservers at very end (in loadFromGateway)
	 * be sure to set it back to false when done so that addToList and removeFromList will notify after setChanged
	 */
	private boolean dontNotify;
	
	public InventoryList() {
		myList = new ArrayList<Inventory>();
		myIdMap = new HashMap<Long, Inventory>();
		dontNotify = false;
		newRecords = new ArrayList<Inventory>();
	}
	
	/**
	 * Replaces list contents with new Inventory objects fetched from Gateway
	 * Insert objects that are not already in list
	 * 
	 * TODO: refresh stale object contents already in list (use a timestamp)
	 * OR only do this when opening ListView is the only Inventory view open 
	 */
	public void loadFromGateway() {
		//fetch list of objects from the database
		List<Inventory> inventorys = null;
		try {
			inventorys = gateway.fetchInventorys();
			
		} catch (GatewayException e) {
			e.printStackTrace();
			return;
		}
		
		loadInventory(inventorys);
	}
	
	// load inventory of warehouse
	public void loadFromGateway(Long wId) {
		//fetch list of objects from the database
		List<Inventory> inventorys = null;
		try {
			inventorys = gateway.fetchWarehouseInventorys(wId);
			
		} catch (GatewayException e) {
			e.printStackTrace();
			return;
		}
		loadInventory(inventorys);
		
	}
			
	public void loadInventory( List<Inventory> inventorys){
	
	
		//since this method does a lot of adding and removing
		//don't notify observers until all done
		dontNotify = true;
		
		//any inventory in our list that is NOT in the db needs to be removed from our list
		for(int i = myList.size() - 1; i >= 0; i--) {
			Inventory invt = myList.get(i);
			boolean removeRecord = true;
			//don't remove a recently Added record that hasn't been saved yet
			if(invt.getId() == Inventory.INVALID_ID) {
				removeRecord = false;
			} else {
				for(Inventory dCheck : inventorys) {
					if(dCheck.getId() == invt.getId()) {
						removeRecord = false;
						break;
					}
				}
			}
			//p not found in db people array so delete it
			if(removeRecord)
				removeInventoryFromList(invt);
			//TODO: any detail view with p in it either needs to close or should have a lock to prevent this deletion
			//TODO: may also need to unregister all open views as observers of p
		}
		
		//for each object in fetched list, see if it is in the hashmap
		//if not, add it to the list
		for(Inventory invt : inventorys) {
			if(!myIdMap.containsKey(invt.getId())) {
				addInventoryToList(invt);
			}
		}
		
		//tell all observers of this list to update
		this.notifyObservers();

		//turn this off
		dontNotify = false;
	}
	
	// check has over one record have same warehouse ID and part ID
	
	public boolean duplicate(Inventory inventory){
		for(int i = myList.size() - 1; i >= 0; i--) {
			Inventory invt = myList.get(i);
			if( ( 	invt.getWarehouseId() == inventory.getWarehouseId()) &&
					invt.getPartId() == inventory.getPartId() &&
					invt.getId() != inventory.getId() )
				return true;
		}
		
		return false;
	}
	
	// exist of warehouse or part
	public boolean existWarehousePart(Long wId, Long pId){
		
		if( wId > 0 ){
			// for warehouse
			for(int i = myList.size() - 1; i >= 0; i--) {
				Inventory invt = myList.get(i);
				if( invt.getWarehouseId() == wId )
					return true;
			}	
			
		}else{
			// for part
			for(int i = myList.size() - 1; i >= 0; i--) {
				Inventory invt = myList.get(i);
				if( invt.getPartId() == pId )
					return true;
			}
			
		}
		
		return false;
	}
	
	public Inventory findById(long id) {
		//check the identity map
		if(myIdMap.containsKey(new Long(id)))
			return myIdMap.get(new Long(id));
		return null;
	}
	
	/**
	 * Add a inventory object to the list's collection and set its gateway to this list's gateway
	 * Also add list as observer of p
	 * @param p Person instance to add to the collection
	 */
	public void addInventoryToList(Inventory invt) {
		myList.add(invt);
		invt.setGateway(this.gateway);
		invt.addObserver(this);

		//add to identity map
		myIdMap.put(invt.getId(), invt);

		//tell all observers of this list to update
		this.setChanged();
		if(!dontNotify)
			this.notifyObservers();
	}

	/**
	 * Remove a inventory from the list and remove this as observer of p
	 * @return Person p if found in list, otherwise null
	 */
	public Inventory removeInventoryFromList(Inventory invt) {
		if(myList.contains(invt)) {
			myList.remove(invt);
			//also remove from hash map
			myIdMap.remove(invt.getId());

			//tell all observers of this list to update
			this.setChanged();
			if(!dontNotify)
				this.notifyObservers();

			return invt;
		}
		return null;
	}
	
	// get Total Quality of a warehouse
	public double getTotalQuantityWarehouseExceptCurrent(Inventory inventory){
		double lTotalquality = 0L;
		for(int i = myList.size() - 1; i >= 0; i--) {
			Inventory in = myList.get(i);
			if( 	in.getWarehouseId() == inventory.getWarehouseId() &&
					in.getId() != inventory.getId() )
			{
				lTotalquality += in.getQuantity();
			}
		}	
		
		return lTotalquality;
	}
	
	// get capacity remaining of specific warehouse
	public double remainCapacityInWarehouse(double capacity, Inventory inventory){
		
		return capacity  - (getTotalQuantityWarehouseExceptCurrent(inventory)+ inventory.getQuantity() );

	}
	/**
	 * Accessors
	 * @return
	 */
	public List<Inventory> getList() {
		return myList;
	}

	public void setList(List<Inventory> myList) {
		this.myList = myList;
	}

	public InventoryTableGateway getGateway() {
		return gateway;
	}

	public void setGateway(InventoryTableGateway gateway) {
		this.gateway = gateway;
	}

	/**
	 * adds new Inventory with invalid id to list of new records
	 * during update, if Inventory updating is a new record then will re-add it to the identity map
	 * @param invt
	 */
	public void addToNewRecords(Inventory invt) {
		newRecords.add(invt);
	}
	
	/**
	 * Notify list observers that an object has changed
	 * if Observed record is a new object and its Id has changed, re-add it to the hashmap
	 * @param o the observable that has changed
	 * @param arg
	 */
	@Override
	public void update(Observable o, Object arg) {
		//System.out.println("DEBUG: PersonList update");
		//if o is in the newRecords list, remove it from identity map
		//and add it back with new id
		Inventory invt = (Inventory) o;
		if(newRecords.contains(invt)) {
			myIdMap.remove(Inventory.INVALID_ID);
			myIdMap.put(invt.getId(), invt);
			newRecords.remove(invt);
		}
		
		this.setChanged();
		notifyObservers();
	}
}