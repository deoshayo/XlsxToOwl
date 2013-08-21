package at.ac.tuwien.importer.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHelper {
	
	public static XSSFWorkbook readFile(String fileString) {
		try {
			InputStream inp = new FileInputStream(fileString);
			return new XSSFWorkbook(inp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
