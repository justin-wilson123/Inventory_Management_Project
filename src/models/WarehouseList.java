package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import database.WarehouseTableGateway;
import database.GatewayException;

/**
 * A model that is a collection of Warehouse models
 * @author Justin Wilson
 *
 */
public class WarehouseList extends Observable implements Observer {
	/**
	 * Collection of Warehouse objects that this list holds
	 */
	private List<Warehouse> myList;
	
	/**
	 * Identity map for determining if Warehouse is already in this list
	 */
	private HashMap<Long, Warehouse> myIdMap;
	
	/**
	 * Collection of newly added records to know when to update key in Identity map
	 */
	private ArrayList<Warehouse> newRecords;
	
	/**
	 * Database connection for the WarehouseList 
	 */
	private WarehouseTableGateway gateway;
	
	/**
	 * for multiple object inserts and deletes, set to true to notifyObservers at very end (in loadFromGateway)
	 * be sure to set it back to false when done so that addToList and removeFromList will notify after setChanged
	 */
	private boolean dontNotify;
	
	public WarehouseList() {
		myList = new ArrayList<Warehouse>();
		myIdMap = new HashMap<Long, Warehouse>();
		dontNotify = false;
		newRecords = new ArrayList<Warehouse>();
	}
	
	/**
	 * Replaces list contents with new Warehouse objects fetched from Gateway
	 * Insert objects that are not already in list
	 * 
	 * TODO: refresh stale object contents already in list (use a timestamp)
	 * OR only do this when opening ListView is the only Warehouse view open 
	 */
	public void loadFromGateway() {
		//fetch list of objects from the database
		List<Warehouse> warehouses = null;
		try {
			warehouses = gateway.fetchWarehouses();
			
		} catch (GatewayException e) {
			e.printStackTrace();
			return;
		}
		
		//since this method does a lot of adding and removing
		//don't notify observers until all done
		dontNotify = true;
		
		//any person in our list that is NOT in the db needs to be removed from our list
		for(int i = myList.size() - 1; i >= 0; i--) {
			Warehouse d = myList.get(i);
			boolean removeRecord = true;
			//don't remove a recently Added record that hasn't been saved yet
			if(d.getId() == Warehouse.INVALID_ID) {
				removeRecord = false;
			} else {
				for(Warehouse dCheck : warehouses) {
					if(dCheck.getId() == d.getId()) {
						removeRecord = false;
						break;
					}
				}
			}
			//p not found in db people array so delete it
			if(removeRecord)
				removeWarehouseFromList(d);
			//TODO: any detail view with p in it either needs to close or should have a lock to prevent this deletion
			//TODO: may also need to unregister all open views as observers of p
		}
		
		//for each object in fetched list, see if it is in the hashmap using Person.id
		//if not, add it to the list
		for(Warehouse d : warehouses) {
			if(!myIdMap.containsKey(d.getId())) {
				addWarehouseToList(d);
			}
		}
		
		//tell all observers of this list to update
		this.notifyObservers();

		//turn this off
		dontNotify = false;
	}
	
	public HashMap< Long, String> getWList(){
		HashMap< Long, String> nameList = new HashMap< Long, String>();
		
		for(int i = myList.size() - 1; i >= 0; i--) {
			Warehouse w = myList.get(i);
			nameList.put(w.getId(), w.getWareHouseName());
		}	
		
		return nameList;
	}
	
	public HashMap< Long, Long> getWCapacityList(){
		HashMap< Long, Long> nameList = new HashMap< Long, Long>();
		
		for(int i = myList.size() - 1; i >= 0; i--) {
			Warehouse w = myList.get(i);
			nameList.put(w.getId(), w.getStorageCapacity());
		}	
		
		return nameList;
	}
	
	public Warehouse findById(long id) {
		//check the identity map
		if(myIdMap.containsKey(new Long(id)))
			return myIdMap.get(new Long(id));
		return null;
	}
	
	/**
	 * Add a person object to the list's collection and set its gateway to this list's gateway
	 * Also add list as observer of p
	 * @param p Person instance to add to the collection
	 */
	public void addWarehouseToList(Warehouse d) {
		myList.add(d);
		d.setGateway(this.gateway);
		d.addObserver(this);

		//add to identity map
		myIdMap.put(d.getId(), d);

		//tell all observers of this list to update
		this.setChanged();
		if(!dontNotify)
			this.notifyObservers();
	}

	/**
	 * Remove a person from the list and remove this as observer of p
	 * @return Person p if found in list, otherwise null
	 */
	public Warehouse removeWarehouseFromList(Warehouse d) {
		if(myList.contains(d)) {
			myList.remove(d);
			//also remove from hash map
			myIdMap.remove(d.getId());

			//tell all observers of this list to update
			this.setChanged();
			if(!dontNotify)
				this.notifyObservers();

			return d;
		}
		return null;
	}
	
	/**
	 * Accessors
	 * @return
	 */
	public List<Warehouse> getList() {
		return myList;
	}

	public void setList(List<Warehouse> myList) {
		this.myList = myList;
	}

	public WarehouseTableGateway getGateway() {
		return gateway;
	}

	public void setGateway(WarehouseTableGateway gateway) {
		this.gateway = gateway;
	}

	/**
	 * adds new warehouse with invalid id to list of new records
	 * during update, if warehouse updating is a new record then will re-add it to the identity map
	 * @param d
	 */
	public void addToNewRecords(Warehouse d) {
		newRecords.add(d);
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
		Warehouse d = (Warehouse) o;
		if(newRecords.contains(d)) {
			myIdMap.remove(Warehouse.INVALID_ID);
			myIdMap.put(d.getId(), d);
			newRecords.remove(d);
		}
		
		this.setChanged();
		notifyObservers();
	}
}