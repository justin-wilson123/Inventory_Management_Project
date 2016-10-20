package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import database.PartTableGateway;
import database.GatewayException;

/**
 * A model that is a collection of Part models
 * @author Justin Wilson
 *
 */
public class PartList extends Observable implements Observer {
	/**
	 * Collection of Part objects that this list holds
	 */
	private List<Part> myList;
	
	/**
	 * Identity map for determining if Part is already in this list
	 */
	private HashMap<Long, Part> myIdMap;
	
	/**
	 * Collection of newly added records to know when to update key in Identity map
	 */
	private ArrayList<Part> newRecords;
	
	/**
	 * Database connection for the PartList 
	 */
	private PartTableGateway gateway;
	
	/**
	 * for multiple object inserts and deletes, set to true to notifyObservers at very end (in loadFromGateway)
	 * be sure to set it back to false when done so that addToList and removeFromList will notify after setChanged
	 */
	private boolean dontNotify;
	
	public PartList() {
		myList = new ArrayList<Part>();
		myIdMap = new HashMap<Long, Part>();
		dontNotify = false;
		newRecords = new ArrayList<Part>();
	}
	
	/**
	 * Replaces list contents with new Part objects fetched from Gateway
	 * Insert objects that are not already in list
	 * 
	 * TODO: refresh stale object contents already in list (use a timestamp)
	 * OR only do this when opening ListView is the only Part view open 
	 */
	public void loadFromGateway() {
		//fetch list of objects from the database
		List<Part> parts = null;
		try {
			parts = gateway.fetchParts();
			
		} catch (GatewayException e) {
			e.printStackTrace();
			return;
		}
		
		//since this method does a lot of adding and removing
		//don't notify observers until all done
		dontNotify = true;
		
		//any person in our list that is NOT in the db needs to be removed from our list
		for(int i = myList.size() - 1; i >= 0; i--) {
			Part d = myList.get(i);
			boolean removeRecord = true;
			//don't remove a recently Added record that hasn't been saved yet
			if(d.getId() == Part.INVALID_ID) {
				removeRecord = false;
			} else {
				for(Part dCheck : parts) {
					if(dCheck.getId() == d.getId()) {
						removeRecord = false;
						break;
					}
				}
			}
			//p not found in db people array so delete it
			if(removeRecord)
				removePartFromList(d);
			//TODO: any detail view with p in it either needs to close or should have a lock to prevent this deletion
			//TODO: may also need to unregister all open views as observers of p
		}
		
		//for each object in fetched list, see if it is in the hashmap using Person.id
		//if not, add it to the list
		for(Part d : parts) {
			if(!myIdMap.containsKey(d.getId())) {
				addPartToList(d);
			}
		}
		
		//tell all observers of this list to update
		this.notifyObservers();

		//turn this off
		dontNotify = false;
	}
	
	public HashMap< Long, String> getPList(){
		HashMap< Long, String> nameList = new HashMap< Long, String>();
		
		for(int i = myList.size() - 1; i >= 0; i--) {
			Part p = myList.get(i);
			nameList.put(p.getId(), p.getPartNumber());
		}	
		
		return nameList;
	}
	
	public Part findById(long id) {
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
	public void addPartToList(Part d) {
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
	public Part removePartFromList(Part d) {
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
	public List<Part> getList() {
		return myList;
	}

	public void setList(List<Part> myList) {
		this.myList = myList;
	}

	public PartTableGateway getGateway() {
		return gateway;
	}

	public void setGateway(PartTableGateway gateway) {
		this.gateway = gateway;
	}

	/**
	 * adds new Part with invalid id to list of new records
	 * during update, if Part updating is a new record then will re-add it to the identity map
	 * @param d
	 */
	public void addToNewRecords(Part d) {
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
		Part d = (Part) o;
		if(newRecords.contains(d)) {
			myIdMap.remove(Part.INVALID_ID);
			myIdMap.put(d.getId(), d);
			newRecords.remove(d);
		}
		
		this.setChanged();
		notifyObservers();
	}
}