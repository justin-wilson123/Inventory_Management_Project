package database;

import java.util.List;

import models.Part;

/**
 * Interface for Person Table Gateways
 * Provides methods for Table and Row-level DB calls
 * @author Justin Wilson
 *
 */
public interface PartTableGateway {
	public abstract Part fetchPart(long id) throws GatewayException;
	public abstract void deletePart(long id) throws GatewayException;
	public abstract long insertPart(Part p) throws GatewayException;
	public abstract void savePart(Part p) throws GatewayException;
	public abstract List<Part> fetchParts() throws GatewayException;
	public abstract void close();
	boolean blockPart(long id, String userName) throws GatewayException;
	public abstract void updateAccessTime(int i);
}
