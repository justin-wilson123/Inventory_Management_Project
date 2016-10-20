package testing;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import models.Part;


public class test_part {

	
	private static Part testPart;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testPart = new Part();
	}
	
	@Before
	public void setUp() throws Exception {
	}
	//1
	@Test
	public void testPartNumberInvalidNull() {
		String testPNUM = null;
		assertEquals(false, testPart.validPartNumber(testPNUM));
	}

	//2
	@Test
	public void testPartNumberInvalidTooLarge() {
		String testPNUM = "";
		for(int i = 0; i < 21; i++)
			testPNUM += "a";
		assertEquals(false, testPart.validPartNumber(testPNUM));
	}

	//3
	@Test
	public void testPartNumberValid() {
		String testPNUM = "";
		for(int i = 0; i < 20; i++)
			testPNUM += "a";
		assertEquals(true, testPart.validPartNumber(testPNUM));
	}

	//4
	@Test
	public void testPartNumberInvalidEmpty() {
		String testPNUM = "";
		assertEquals(false, testPart.validPartNumber(testPNUM));
	}
	
	//5
	@Test
	public void testPartNameInvalidNull() {
		String testPNAME = null;
		assertEquals(false, testPart.validPartName(testPNAME));
	}

	//6
	@Test
	public void testPartNameInvalidTooLarge() {
		String testPNAME = "";
		for(int i = 0; i < 256; i++)
			testPNAME += "a";
		assertEquals(false, testPart.validPartName(testPNAME));
	}

	//7
	@Test
	public void testPartNameValid() {
		String testPNAME = "";
		for(int i = 0; i < 255; i++)
			testPNAME += "a";
		assertEquals(true, testPart.validPartName(testPNAME));
	}

	//8
	@Test
	public void testPartNameInvalidEmpty() {
		String testPNAME = "";
		assertEquals(false, testPart.validPartName(testPNAME));
	}
	
	//9
	@Test
	public void testPartVendorInvalidNull() {
		String testVENDOR = null;
		assertEquals(false, testPart.validVendor(testVENDOR));
	}

	//10
	@Test
	public void testPartVendorInvalidTooLarge() {
		String testVENDOR = "";
		for(int i = 0; i < 256; i++)
			testVENDOR += "a";
		assertEquals(false, testPart.validVendor(testVENDOR));
	}

	//11
	@Test
	public void testPartVendorValid() {
		String testVENDOR = "";
		for(int i = 0; i < 255; i++)
			testVENDOR += "a";
		assertEquals(true, testPart.validVendor(testVENDOR));
	}
	
	
	//12
	@Test
	public void testUnitOfQuantityInvalid() {
		String testUNIT_OF_QTY = "Miles";
		assertEquals(false, testPart.validUnitOfQty(testUNIT_OF_QTY));
	}
	
	//13
	@Test
	public void testUnitOfQuantityValid() {
		String testUNIT_OF_QTY = "Pieces";
		assertEquals(true, testPart.validUnitOfQty(testUNIT_OF_QTY));
	}
	
	//13.1
	@Test
	public void testUnitOfQuantity1Valid() {
		String testUNIT_OF_QTY = "Linear Ft.";
		assertEquals(true, testPart.validUnitOfQty(testUNIT_OF_QTY));
	}
	
	//14
	@Test
	public void testVendorPartNumberInvalid() {
		String testVENDOR_PART_NUMBER = "";
		for(int i = 0; i < 256; i++)
			testVENDOR_PART_NUMBER += "a";
		assertEquals(false, testPart.validVendorPart(testVENDOR_PART_NUMBER));
	}
	
	//15
	@Test
	public void testVendorPartNumberNull() {
		String testVENDOR_PART_NUMBER = null;
		assertEquals(false, testPart.validVendorPart(testVENDOR_PART_NUMBER));
	}
	
	//16
	@Test
	public void testVendorPartNumberValid() {
		String testVENDOR_PART_NUMBER = "";
		for(int i = 0; i < 255; i++)
			testVENDOR_PART_NUMBER += "a";
		assertEquals(true, testPart.validVendorPart(testVENDOR_PART_NUMBER));
	}
	
}
	