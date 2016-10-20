package testing;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import models.Warehouse;


public class test_warehouse {

	
	private static Warehouse testWarehouse;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testWarehouse = new Warehouse();
	}
	
	@Before
	public void setUp() throws Exception {
	}
	//1
	@Test
	public void testWarehouseInvalidNull() {
		String testWN = null;
		assertEquals(false, testWarehouse.validWareHouseName(testWN));
	}

	//2
	@Test
	public void testStorageCapacityInvalidNeg() {
		int testCap;
			testCap = -1;
		assertEquals(false, testWarehouse.validStorageCap(testCap));
	}


	//4
	@Test
	public void testZipCodeInvalidTooLarge() {
		String testZip = "";
		for(int i = 0; i < 6; i++)
			testZip += "a";
		assertEquals(false, testWarehouse.validZip(testZip));
	}

	//5
	@Test
	public void testZipCodeInvalidNULL() {
		String testZip = null;
		assertEquals(false, testWarehouse.validZip(testZip));
	}
	
	
	//6
	@Test
	public void testZipCodeValid() {
		String testZip = "";
		for(int i = 0; i < 5; i++)
			testZip += "a";
		assertEquals(true, testWarehouse.validZip(testZip));
	}
	
	//7
	@Test
	public void testStateInvalidTooLarge() {
		String testState = "";
		for(int i = 0; i < 51; i++)
			testState += "a";
		assertEquals(false, testWarehouse.validState(testState));
	}
	
	//8
	@Test
	public void testStateValid() {
		String testState = "";
		for(int i = 0; i < 50; i++)
			testState += "a";
		assertEquals(true, testWarehouse.validState(testState));
	}
	
	//9
	@Test
	public void testCityInvalidTooLarge() {
		String testCity = "";
		for(int i = 0; i < 101; i++)
			testCity += "a";
		assertEquals(false, testWarehouse.validCity(testCity));
	}
	
	//10
	@Test
	public void testCityValid() {
		String testCity = "";
		for(int i = 0; i < 100; i++)
			testCity += "a";
		assertEquals(true, testWarehouse.validCity(testCity));
	}
	
	//11
	@Test
	public void testCityInvalidNull() {
		String testCity = null;
		assertEquals(false, testWarehouse.validCity(testCity));
	}
	
	//12
	@Test
	public void testAddressInvalidTooLarge() {
		String testAddress = "";
		for(int i = 0; i < 256; i++)
			testAddress += "a";
		assertEquals(false, testWarehouse.validAddress(testAddress));
	}
	
	//13
	@Test
	public void testAddressValid() {
		String testAddress = "";
		for(int i = 0; i < 255; i++)
			testAddress += "a";
		assertEquals(true, testWarehouse.validAddress(testAddress));
	}
	
	//14
	@Test
	public void testAddressInvalidNull() {
		String testAddress = null;
		assertEquals(false, testWarehouse.validAddress(testAddress));
	}
	
	//15
	@Test
	public void testWarehouseNameInvalidTooLarge() {
		String testWN = "";
		for(int i = 0; i < 256; i++)
			testWN += "a";
		assertEquals(false, testWarehouse.validWareHouseName(testWN));
	}

}
