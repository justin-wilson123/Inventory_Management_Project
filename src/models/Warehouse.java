package models;

import java.util.Observable;

import database.WarehouseTableGateway;
import database.GatewayException;

/**
 * Common Warehouse information
 * Extends Observable so observers can register for change notifications
 * @author Justin Wilson
 *
 */
public class Warehouse extends Observable {
	/**
	 * validation error messages
	 */
	public static final String ERRORMSG_INVALID_ID = "Invalid id!";
	public static final String ERRORMSG_INVALID_WAREHOUSENAME = "Invalid warehouse name!";
	public static final String ERRORMSG_INVALID_ADDRESS = "Invalid address!";
	public static final String ERRORMSG_INVALID_CITY = "Invalid city!";
	public static final String ERRORMSG_INVALID_STATE = "Invalid state!";
	public static final String ERRORMSG_INVALID_ZIP = "Invalid zip!";
	public static final String ERRORMSG_INVALID_STORAGE = "Invalid storage capacity!";
	public static final String ERRORMSG_WAREHOUSE_ALREADY_EXISTS = "A warehouse with that name already exists!";
	public static final String DEFAULT_EMPTY_WAREHOUSE = "Unknown";
	
	public static final int INVALID_ID = 0;
	
	/**
	 * Unique id of Warehouse record
	 * Note: Should be automatically generated by the system (no setter)
	 */
	private long id;
	
	/**
	 * Name of warehouse. non-unique and optional. 
	 * Default to empty string. toString will replace empty strings with "unknown"  
	 */
	private String warehouse_name;
	
	/**
	 * Street address of warehouse
	 */
	private String address;
	
	/**
	 * City where the warehouse is located
	 */
	private String city;
	
	/**
	 * State where the warehouse is located
	 */
	private String state;
	
	/**
	 * Zip-code where the warehouse is located
	 */
	private String zip;
	
	/**
	 * Maximum # of units that can be stored at that warehouse. Must be >= 0 (defaults to 0)
	 */
	private long storage_cap;
	
	/**
	 * Database connection for the Warehouse (same gateway used by PersonWarehouse) 
	 */
	private WarehouseTableGateway gateway;

	public Warehouse() {
		id = INVALID_ID;
		warehouse_name = "";
		address = "";
		city = "";
		state = "";
		zip = "";
		storage_cap = 0;
	}

	/**
	 * Creates a new Warehouse object with specified warehouse name, address, city, state, zip, and storage
	 * 
	 */
	public Warehouse(String wn, String addrss, String cit, String st, String z, long stor) {
		this();
		//validate parameters
		if(!validWareHouseName(wn))
			throw new IllegalArgumentException(ERRORMSG_INVALID_WAREHOUSENAME);
		if(!validAddress(addrss))
			throw new IllegalArgumentException(ERRORMSG_INVALID_ADDRESS);
		if(!validCity(cit))
			throw new IllegalArgumentException(ERRORMSG_INVALID_CITY);
		if(!validState(st))
			throw new IllegalArgumentException(ERRORMSG_INVALID_STATE);
		if(!validZip(z))
			throw new IllegalArgumentException(ERRORMSG_INVALID_ZIP);
		if(!validStorageCap(stor))
			throw new IllegalArgumentException(ERRORMSG_INVALID_STORAGE);
		
		
		warehouse_name = wn;
		address = addrss;
		city = cit;
		state = st;
		zip = z;
		storage_cap = stor;
	}
	
	/**
	 * 5-argument constructor for creating a warehouse object and setting the id (read from database)
	 * @param id Id of warehouse instance to create (given from database) CANNOT BE 0
	 * @param wn Warehouse name
	 * @param addrss Address
	 * @param cit City of warehouse
	 * @param st State of warehouse
	 * @param z Zip code of warehouse
	 * @param stor Storage capacity of warehouse
	 */
	public Warehouse(long id, String wn, String addrss, String cit, String st, String z, long stor) {
		this(wn, addrss, cit, st, z, stor);
		if(id < 1)
			throw new IllegalArgumentException(ERRORMSG_INVALID_ID);
		setId(id);
	}
	
	/**
	 * Returns the Warehouse's hopefully unique id
	 * @return
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets Warehouse's id
	 * Should only be called by the gateway when fetching warehouse record from database (via fetch or add)
	 * @param id
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * Returns the warehouse name
	 * @return
	 */
	public String getWareHouseName() {
		return warehouse_name;
	}
	
	/**
	 * Determines if given warehouse name value is valid
	 * Rules: cannot be null, length must be >= 0 and <= 255 
	 * @param fn First name value to test
	 * @return
	 */
	public boolean validWareHouseName(String wn) {
		if(wn == null)
			return false;
		if(wn.length() > 255)
			return false;
		if(wn.length() == 0)
			return false;
		return true;
	}
	
	
	

	/**
	 * Sets name of warehouse
	 * Warehouse name cannot be null
	 * @return
	 */
	public void setWareHouseName(String warehouse_name) {
		if(!validWareHouseName(warehouse_name))
			throw new IllegalArgumentException(ERRORMSG_INVALID_WAREHOUSENAME);
		this.warehouse_name = warehouse_name;
		//get ready to notify observers (notify is called in finishUpdate())
		setChanged();
	}

	/**
	 * Returns address of warehouse
	 * @return
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * Determines if given first name value is valid
	 * Rules: cannot be null, length must be >= 0 and <= 255 
	 * @param fn First name value to test
	 * @return
	 */
	public boolean validAddress(String addrss) {
		if(addrss == null)
			return false;
		if(addrss.length() > 255)
			return false;
		if(addrss.length() == 0)
			return false;
		return true;
	}
	
	
	
	/**
	 * Sets address of warehouse
	 * Address cannot be null
	 * @return
	 */
	public void setAddress(String address) {
		if(!validAddress(address))
			throw new IllegalArgumentException(ERRORMSG_INVALID_ADDRESS);
		this.address = address;
		//get ready to notify observers (notify is called in finishUpdate())
		setChanged();
	}

	/**
	 * Returns city of warehouse
	 * @return
	 */
	public String getCity() {
		return city;
	}
	
	/**
	 * Determines if given City is valid
	 * Rules: cannot be null, length must be >= 0 and <= 255 
	 * @param fn First name value to test
	 * @return
	 */
	public boolean validCity(String cit) {
		if(cit == null)
			return false;
		if(cit.length() > 100)
			return false;
		return true;
	}
	

	/**
	 * Sets city of warehouse
	 * City cannot be null
	 * @return
	 */
	public void setCity(String city) {
		if(!validCity(city))
			throw new IllegalArgumentException(ERRORMSG_INVALID_CITY);
		this.city = city;
		//get ready to notify observers (notify is called in finishUpdate())
		setChanged();
	}
	
	/**
	 * Returns state of warehouse
	 * @return
	 */
	public String getState() {
		return state;
	}

	/**
	 * Determines if given first name value is valid
	 * Rules: cannot be null, length must be >= 0 and <= 20 
	 * @param fn First name value to test
	 * @return
	 */
	public boolean validState(String st) {
		if(st == null)
			return false;
		if(st.length() > 50)
			return false;
		return true;
	}
	
	
	
	/**
	 * Sets state of warehouse
	 * State cannot be null
	 * @return
	 */
	public void setState(String state) {
		if(!validState(state))
			throw new IllegalArgumentException(ERRORMSG_INVALID_STATE);
		this.state = state;
		//get ready to notify observers (notify is called in finishUpdate())
		setChanged();
	}
	
	/**
	 * Returns zip code of warehouse
	 * @return
	 */
	public String getZip() {
		return zip;
	}
	
	public boolean validZip(String z) {
		if(z == null)
			return false;
		if(z.length() == 5)
			return true;
		if(z.length() > 5)
			return false;

		return true;
	}
	
	
	

	/**
	 * Sets zip code of warehouse
	 * Zip Code cannot be null
	 * @return
	 */
	public void setZip(String zip) {
		if(!validZip(zip))
			throw new IllegalArgumentException(ERRORMSG_INVALID_ZIP);
		this.zip = zip;
		//get ready to notify observers (notify is called in finishUpdate())
		setChanged();
	}
	
	/**
	 * Returns storage capacity of warehouse
	 * @return
	 */
	public long getStorageCapacity() {
		return storage_cap;
	}
	
	public boolean validStorageCap(long stor) {
		if(stor <= 0)
			return false;
		return true;
	}

	/**
	 * Sets storage capacity of warehouse
	 * storage capacity must be >= 0
	 * @return
	 */
	public void setStorageCapacity(long storage_cap) {
		if(!validStorageCap(storage_cap))
			throw new IllegalArgumentException(ERRORMSG_INVALID_STORAGE);
		this.storage_cap = storage_cap;
		//get ready to notify observers (notify is called in finishUpdate())
		setChanged();
	}

	/**
	 * Pass-through to gateway to determine if warehoues name already exists in database
	 * @param id Id of warehouse 
	 * @param wn Name of warehouse to check in database
	 * @return true if warehouse already exists, else false
	 */
	public boolean warehouseAlreadyExists(long id, String wn) {
		//if warehouse name is already in the database AND id does not match
		//then return true
		//else return false
		try {
			return gateway.warehouseAlreadyExists(id, wn);
		} catch (GatewayException e) {
			return true;
		}
	}
	
	
	/**
	 * Tells the model that update has finished so it can finish the update
	 * E.g., notify observers
	 */
	public void finishUpdate() throws GatewayException {
		Warehouse orig = null;
		//if insert, check if this warehouse's name already exists in the database
		//if so then cancel update 
		if(this.getId() == 0) {
			if(gateway.warehouseAlreadyExists(0, this.getWareHouseName()))
				throw new GatewayException(this.getWareHouseName() + " is already in the database");
		}
		try {
			//if id is 0 then this is a new Warehouse to insert, else its an update
			if(this.getId() == 0) {
				//set id to the long returned by insertWarehouse
				this.setId(gateway.insertWarehouse(this));
				
			} else {
				//fetch warehouse from db table in case this fails
			
				orig = gateway.fetchWarehouse(this.getId());
		
				//try to save to the database
				gateway.saveWarehouse(this);

			}
			//if gateway ok then notify observers
			notifyObservers();
			
		} catch(GatewayException e) {
			
			System.out.println(e.getMessage());
			
			//if fails then try to refetch model fields from the database
			if(orig != null) {
				this.setWareHouseName(orig.getWareHouseName());
				this.setAddress(orig.getAddress());
				this.setCity(orig.getCity());
				this.setState(orig.getState());
				this.setZip(orig.getZip());
				this.setStorageCapacity(orig.getStorageCapacity());
			}
			throw new GatewayException("Error trying to save the Warehouse object!");
		}
	}
	
	/**
	 * delete this object through the gateway (i.e., db)
	 */
	public void delete() throws GatewayException {
		//if id is 0 then nothing to do in the gateway (record has not been saved yet
		if(this.getId() == 0) 
			return;
			try {
				gateway.deleteWarehouse(this.getId());
			} catch (GatewayException e) {
				throw new GatewayException(e.getMessage());
			}
	}

	/**
	 * Accessors for gateway
	 * @return
	 */
	public WarehouseTableGateway getGateway() {
		return gateway;
	}

	public void setGateway(WarehouseTableGateway gateway) {
		this.gateway = gateway;
	}
	
	public void update(Observable o, Object arg) {
		
	}
}
