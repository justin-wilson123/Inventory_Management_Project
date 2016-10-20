package database;

import java.util.List;

import models.Inventory;

/**
 * Interface for Table Gateways
 * Provides methods for Table and Row-level DB calls
 * @author Justin Wilson
 *
 */
public interface InventoryTableGateway {
	public abstract Inventory fetchInventory(long id) throws GatewayException;
	public abstract void deleteInventory(long id) throws GatewayException;
	public abstract long insertInventory(Inventory p) throws GatewayException;
	public abstract void saveInventory(Inventory p) throws GatewayException;
	public abstract List<Inventory> fetchInventorys() throws GatewayException;
	public abstract List<Inventory> fetchWarehouseInventorys(Long wId) throws GatewayException;
	public abstract void close();
}
