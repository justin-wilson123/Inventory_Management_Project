package database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import models.Warehouse;


/**
 * MySQL implementation of WarehouseTableGateway
 * 
 * @author Justin Wilson
 *
 */
public class WarehouseTableGatewayMySQL implements WarehouseTableGateway {
	private static final boolean DEBUG = true;

	/**
	 * external DB connection
	 */
	private Connection conn = null;
	
	/**
	 * Constructor: creates database connection
	 * @throws GatewayException
	 */
	public WarehouseTableGatewayMySQL() throws GatewayException {
		//read the properties file to establish the db connection
		DataSource ds = null;
		try {
			ds = getDataSource();
		} catch (RuntimeException | IOException e1) {
			throw new GatewayException(e1.getMessage());
		}
		if(ds == null) {
        	throw new GatewayException("Datasource is null!");
        }
		try {
        	conn = ds.getConnection();
			//default isolation level of allow Phantom Reads is ok for this application
		} catch (SQLException e) {
			throw new GatewayException("SQL Error: " + e.getMessage());
		}
	}

	@Override
	public Warehouse fetchWarehouse(long id) throws GatewayException {
		Warehouse d = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch person
			st = conn.prepareStatement("select * from WAREHOUSE where id = ? ");
			st.setLong(1, id);
			rs = st.executeQuery();
			//should only be 1
			rs.next();
			d = new Warehouse(rs.getLong("id"), rs.getString("warehouse_name"), rs.getString("address"),rs.getString("city"),rs.getString("state"),rs.getString("zip"),rs.getLong("storage_cap"));
			
		} catch (SQLException e) {
			//e.printStackTrace();
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(rs != null)
					rs.close();
				if(st != null)
					st.close();
			} catch (SQLException e) {
				throw new GatewayException("SQL Error: " + e.getMessage());
			}
		}
		return d;
	}
	
	/**
	 * determines if given first and last name already exist in the warehouse table
	 * @param wn warehouse's  name
	 * @return true if warehouse exists in database, else false
	 */
	public boolean warehouseAlreadyExists(long id, String wn) throws GatewayException {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch warehouse
			st = conn.prepareStatement("select count(id) as num_records "
					+ " from WAREHOUSE where warehouse_name = ? and id <> ? ");
			st.setString(1, wn);
			st.setLong(2, id);
			rs = st.executeQuery();
			//should only be 1
			rs.next();
			if(rs.getInt("num_records") > 0)
				return true;
			//give warehouse object a reference to this gateway
			//NOTE: this is now the responsibility of the WarehouseList
			//p.setGateway(this);
		} catch (SQLException e) {
			//e.printStackTrace();
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(rs != null)
					rs.close();
				if(st != null)
					st.close();
			} catch (SQLException e) {
				throw new GatewayException("SQL Error: " + e.getMessage());
			}
		}
		return false;
	}

	/**
	 * Deletes warehouse from the database
	 * Note: uses a transaction to perform deletion since it involves multiple tables
	 * @param id Id of the warehouse in the db to fetch
	 */
	@Override
	public void deleteWarehouse(long id) throws GatewayException {
		PreparedStatement st = null;
		try {
			//turn off autocommit to start the tx
			conn.setAutoCommit(false);
			
			//use this statement to force tx exception to see rollback
			st = conn.prepareStatement("delete from WAREHOUSE where id = ? ");
			st.setLong(1, id);
			st.executeUpdate();
			
			//if we get here, everything worked without exception so commit the changes
			conn.commit();

		} catch (SQLException e) {
			//roll the tx back
			try {
				conn.rollback();
			} catch (SQLException e1) {
				throw new GatewayException(e1.getMessage());
			}
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(st != null)
					st.close();
				//turn autocommit on again regardless if commit or rollback
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				throw new GatewayException(e.getMessage());
			}
		}
	}

	@Override
	public long insertWarehouse(Warehouse w) throws GatewayException {
		//init new id to invalid
		long newId = Warehouse.INVALID_ID;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("insert WAREHOUSE (warehouse_name, address, city, state, zip, storage_cap) "
					+ " values ( ?, ?, ?, ?, ?, ? ) ", PreparedStatement.RETURN_GENERATED_KEYS);
			
			st.setString(1, w.getWareHouseName());
			st.setString(2, w.getAddress());
			st.setString(3, w.getCity());
			st.setString(4, w.getState());
			st.setString(5, w.getZip());
			st.setLong(6, w.getStorageCapacity());
			
			
			st.executeUpdate();
			//get the generated key
			rs = st.getGeneratedKeys();
			if(rs != null && rs.next()) {
			    newId = rs.getLong(1);
			} else {
				throw new GatewayException("Could not fetch new record Id");
			}
		} catch (SQLException e) {
			System.out.print(e.getMessage());
			//e.printStackTrace();
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(st != null)
					st.close();
			} catch (SQLException e) {
				throw new GatewayException("SQL Error: " + e.getMessage());
			}
		}
		return newId;
	}

	@Override
	public void saveWarehouse(Warehouse w) throws GatewayException {
		//execute the update and throw exception if any problem
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("update WAREHOUSE "
					+ " set warehouse_name = ?, address = ?, city = ?, state = ?, zip = ?, storage_cap = ? "
					+ " where id = ? ");
			
			st.setString(1, w.getWareHouseName());
			st.setString(2, w.getAddress());
			st.setString(3, w.getCity());
			st.setString(4, w.getState());
			st.setString(5, w.getZip());
			st.setLong(6, w.getStorageCapacity());
			st.setLong(7, w.getId());
			st.executeUpdate();
		} catch (SQLException e) {
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(st != null)
					st.close();
			} catch (SQLException e) {
				throw new GatewayException("SQL Error: " + e.getMessage());
			}
		}
	}

	@Override
	public List<Warehouse> fetchWarehouses() throws GatewayException {
		ArrayList<Warehouse> ret = new ArrayList<Warehouse>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//fetch WAREHOUSEs
			st = conn.prepareStatement("select * from WAREHOUSE");
			rs = st.executeQuery();
			//add each to list of WAREHOUSEs to return
			while(rs.next()) {
				Warehouse w = new Warehouse(rs.getLong("id"), rs.getString("warehouse_name"), rs.getString("address"),rs.getString("city"),rs.getString("state"),rs.getString("zip"),rs.getLong("storage_cap"));
				ret.add(w);
			}
		} catch (SQLException e) {
			throw new GatewayException(e.getMessage());
		} finally {
			//clean up
			try {
				if(rs != null)
					rs.close();
				if(st != null)
					st.close();
			} catch (SQLException e) {
				throw new GatewayException("SQL Error: " + e.getMessage());
			}
		}
		
		return ret;
	}
	
	public void close() {
		if(DEBUG)
			System.out.println("Closing db connection...");
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * create a MySQL datasource with credentials and DB URL in db.properties file
	 * @return
	 * @throws RuntimeException
	 * @throws IOException
	 */
	private DataSource getDataSource() throws RuntimeException, IOException {
		//read db credentials from properties file
		Properties props = new Properties();
		FileInputStream fis = null;
        fis = new FileInputStream("db.properties");
        props.load(fis);
        fis.close();
        
        //create the datasource
        MysqlDataSource mysqlDS = new MysqlDataSource();
        mysqlDS.setURL(props.getProperty("MYSQL_DB_URL"));
        mysqlDS.setUser(props.getProperty("MYSQL_DB_USERNAME"));
        mysqlDS.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));
        return mysqlDS;
	}
	
}
