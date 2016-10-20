package reports;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import database.GatewayException;

/**
 * Output warehouse part report as Excel spreadsheet using StringBuilder
 * @author Justin Wilson
 *
 */
public class WarehousePartReportExcel extends ReportMaster {
	/**
	 * Excel document variable
	 * @param gw
	 */
	private StringBuilder doc;
	
	public WarehousePartReportExcel(ReportGateway gw) {
		super(gw);
		
		//pdfbox uses log4j so we need to run a configurator
		BasicConfigurator.configure();
		
		//init doc
		doc = null;
	}

	@Override
	public void generateReport() throws ReportException {
		//declare data variables
		List< HashMap<String, String> > records = null;
		try {
			records = gateway.fetchInventory();
		} catch(GatewayException e) {
			throw new ReportException("Error in report generation: " + e.getMessage());
		}
		
		//prep the report page 1
		doc = new StringBuilder();
		
		doc.append("Warehouse Inventory Summary\n\n");
			
		doc.append("Warehouse Name\t");
		doc.append("Part #\t");
		doc.append("Part Name\t");
		doc.append("Quantity\t");
		doc.append("Unit\n");

		int size = records.size();
		
		for(int i=0; i < size; i++) {

			doc.append( records.get(i).get("warehouse_name") + "\t");
			doc.append(records.get(i).get("part_number") + "\t");
			doc.append(records.get(i).get("part_name") + "\t");
			doc.append(records.get(i).get("quantity") + "\t");			
			doc.append(records.get(i).get("unit_of_qty") + "\n");
		}
	}
	
	/**
	 * write Excel report to file
	 */
	@Override
	public void outputReportToFile(String fileName) throws ReportException {
		//Save the results and ensure that the document is properly closed:
		try(PrintWriter out = new PrintWriter(fileName)){
			out.print(doc.toString());
		} catch (IOException e) {
			throw new ReportException("Error in report save to file: " + e.getMessage());
		}
	}

	@Override
	public void close() {
		super.close();
	}
	
}