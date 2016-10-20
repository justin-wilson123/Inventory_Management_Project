package database;

import java.util.List;

import models.Warehouse;

/**
 * Interface for Warehouse Table Gateways
 * Provides methods for Table and Row-level DB calls
 * @author Justin Wilson
 *
 */
public interface WarehouseTableGateway {
	public abstract Warehouse fetchWarehouse(long id) throws GatewayException;
	public abstract boolean warehouseAlreadyExists(long id, String wn) throws GatewayException;
	public abstract void deleteWarehouse(long id) throws GatewayException;
	public abstract long insertWarehouse(Warehouse w) throws GatewayException;
	public abstract void saveWarehouse(Warehouse w) throws GatewayException;
	public abstract List<Warehouse> fetchWarehouses() throws GatewayException;
	public abstract void close();
}
