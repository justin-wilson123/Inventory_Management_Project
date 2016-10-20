package reports;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import database.GatewayException;

public class ReportGatewayMySQL implements ReportGateway {

	/**
	 * external DB connection
	 */
	private Connection conn = null;

	/**
	 * Constructor: creates database connection
	 * @throws GatewayException
	 */
	public ReportGatewayMySQL() throws GatewayException {
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
			//conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE); //prevent even phantom reads
		} catch (SQLException e) {
			throw new GatewayException("SQL Error: " + e.getMessage());
		}
	}
	
	
	public List< HashMap<String, String> > fetchInventory() throws GatewayException {
		
		List< HashMap<String, String> > warehousePart = new ArrayList< HashMap<String, String> >();
		HashMap<String, String> record = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			
			st = conn.prepareStatement("SELECT wh.warehouse_name as warehouse_name, par.part_number as part_number, par.part_name as part_name, inv.quantity as quantity, par.unit_of_qty as unit_of_qty "
					+ "FROM INVENTORY inv "
					+ "INNER JOIN WAREHOUSE wh ON inv.warehouse_id = wh.id "
					+ "INNER JOIN PART par ON inv.part_id = par.id "
					+ "WHERE inv.quantity > 0 "
					+ "ORDER BY wh.warehouse_name, par.part_name ");
			rs = st.executeQuery();
			
			//add each to list of people to return
			while(rs.next()) {
				record = new HashMap<String, String>();
				
				record.put("warehouse_name", rs.getString("warehouse_name"));
				record.put("part_number", rs.getString("part_number"));
				record.put("part_name", rs.getString("part_name"));
				record.put("quantity", rs.getString("quantity"));
				record.put("unit_of_qty", rs.getString("unit_of_qty"));
				
				warehousePart.add(record);
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
		return warehousePart;
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


	@Override
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
