package database;

import java.util.List;

import models.User;

/**
 * Interface for Table Gateways
 * Provides methods for Table and Row-level DB calls
 * @author Justin Wilson
 *
 */
public interface UserTableGateway {
	public abstract User fetchUser(long id) throws GatewayException;
	public User fetchLogin(User u) throws GatewayException;	
	public abstract void deleteUser(long id) throws GatewayException;
	public abstract long insertUser(User u) throws GatewayException;
	public abstract void saveUser(User u) throws GatewayException;
	public abstract List<User> fetchUsers() throws GatewayException;
	public abstract void close();
}
