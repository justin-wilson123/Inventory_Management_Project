package testing;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import models.Inventory;

public class test_inventory {
	
	private static Inventory testInventory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testInventory = new Inventory();
	}
	
	@Before
	public void setUp() throws Exception {
	}
	//1
	@Test
	public void testPartIDInvalidNull() {
		Long test_inventory_part_Id = null;
		assertEquals(false, testInventory.validPartId(test_inventory_part_Id));
	}
	
	//2
	@Test
	public void testPartIDInvalidLessThanOne() {
		long test_inventory_part_Id = 0;
		assertEquals(false, testInventory.validPartId(test_inventory_part_Id));
	}
	
	//3
	@Test
	public void testPartIDValid() {
		long test_inventory_part_Id = 50;
		assertEquals(true, testInventory.validPartId(test_inventory_part_Id));
	}
	
	//4
	@Test
	public void testWarehouseIDInvalidNull() {
		Long test_inventory_warehouse_Id = null;
		assertEquals(false, testInventory.validWarehouseId(test_inventory_warehouse_Id));
	}
	
	//5
	@Test
	public void testWarehouseIDInvalidLessThanOne() {
		long test_inventory_warehouse_Id = 0;
		assertEquals(false, testInventory.validWarehouseId(test_inventory_warehouse_Id));
	}
	
	//6
	@Test
	public void testWarehouseIDValid() {
		long test_inventory_warehouse_Id = 50;
		assertEquals(true, testInventory.validWarehouseId(test_inventory_warehouse_Id));
	}
	
	//7
	@Test
	public void testQtyInvalidLessThanZero() {
		long test_inventory_qty = -1;
		assertEquals(false, testInventory.validQuantity(test_inventory_qty));
	}
	
	//8
	@Test
	public void testQtyValid() {
		long test_inventory_qty = 10;
		assertEquals(true, testInventory.validQuantity(test_inventory_qty));
	}

}
