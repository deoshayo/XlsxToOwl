package at.ac.tuwien.importer.helper;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hp.hpl.jena.util.FileManager;

public class ExcelHelper {
	
	public static XSSFWorkbook readFile(String fileString) {
		try {
			InputStream in = FileManager.get().open(fileString);
			if(in==null) throw new IllegalArgumentException("File: '"+fileString+"' not found");
			return new XSSFWorkbook(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
