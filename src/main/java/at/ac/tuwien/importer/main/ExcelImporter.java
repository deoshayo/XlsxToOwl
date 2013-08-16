package at.ac.tuwien.importer.main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.ac.tuwien.importer.helper.ExcelHelper;
import at.ac.tuwien.importer.helper.ExcelToOnto;
import at.ac.tuwien.importer.helper.ExcelToOntoEntry;
import at.ac.tuwien.importer.helper.OwlHelper;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Hello world!
 *
 */
public class ExcelImporter 
{
	private String MAPPING_SHEET = "Ontology Mapping";
	private String OUTPUT_ONTOLOGY = "smallonto_mod.owl";
	
	Map<String, ExcelToOnto> mapper = new HashMap<String, ExcelToOnto>();
	OntModel model;
	XSSFWorkbook workbook;
	
	public ExcelImporter(String owlFile, String xlsxFile) {
		workbook = ExcelHelper.readFile(xlsxFile);
		model = OwlHelper.readFile(owlFile);
		getMapping();
	}
	
	public void ImportXlsxToOwl() {
		for (XSSFSheet xssfSheet : workbook) {
			if(xssfSheet.getSheetName().equalsIgnoreCase(MAPPING_SHEET)) continue;
			processSheet(xssfSheet);
		}

		try {
			model.write(new FileWriter(OUTPUT_ONTOLOGY), "RDF/XML");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getMapping() {
		XSSFSheet sheet = workbook.getSheet(MAPPING_SHEET);

		for (Row row: sheet) {
			if(row.getRowNum()==0) continue;			
			
			String sheetName = row.getCell(0).getStringCellValue();
			String concept = row.getCell(1).getStringCellValue();
			
			ExcelToOnto map;
			if(mapper.containsKey(sheetName)) {
				map = mapper.get(sheetName);
			} else {
				map = new ExcelToOnto(sheetName, concept, null);
				mapper.put(sheetName, map);
			}
			
			String excelColumn = row.getCell(2).getStringCellValue();
			String ontoProperty = row.getCell(3).getStringCellValue();
			String key = row.getCell(4).getStringCellValue();
			String datatype = row.getCell(5).getStringCellValue();
			int num = (int) row.getCell(6).getNumericCellValue();
			
			if(key.equalsIgnoreCase("pk")) map.setKey(num);
			
			ExcelToOntoEntry entry = new ExcelToOntoEntry(excelColumn, ontoProperty, key, datatype, num);
			
			map.getMappingMap().put(num, entry);
		}
	}
	
	private void processSheet(XSSFSheet sheet) {
		ExcelToOnto map = mapper.get(sheet.getSheetName());
		for(Row row: sheet) {
			
			if(row.getCell(map.getKey()).getStringCellValue().isEmpty() || (row.getRowNum()==0)) continue;
			
			Map<Integer, ExcelToOntoEntry> mapEntries = map.getMappingMap();
			OntClass concept = model.getOntClass(getDefaultNS()+map.getOntoConcept()); // ontoconcept
			String PK = transformToKey(row.getCell(map.getKey()).getStringCellValue());
			
			Individual instance = model.getIndividual(getDefaultNS()+PK);
			if(instance==null) {
				instance = model.createIndividual(getDefaultNS()+PK, concept);
			}
			
			for(int i: mapEntries.keySet()) {
				if(i==map.getKey()) continue; 					// key--omitted
				
				ExcelToOntoEntry mapEntry = mapEntries.get(i);
				OntProperty instanceProperty = model.getOntProperty(getDefaultNS()+mapEntry.getOntoProperty());
				String instancePropertyValue = row.getCell(mapEntry.getColumnNumber()).getStringCellValue();
				RDFNode instancePropertyObject = null;
				
				if(mapEntry.getKey().equalsIgnoreCase("fk")) {
					instancePropertyValue = transformToKey(instancePropertyValue);
					instancePropertyObject = model.getIndividual(getDefaultNS()+instancePropertyValue);
					if(instancePropertyObject==null) {
						instancePropertyObject = model.createIndividual(instancePropertyValue, concept);
					}
				} else {
					instancePropertyObject = ResourceFactory.createPlainLiteral(instancePropertyValue);
				}

				Statement s = ResourceFactory.createStatement(instance, instanceProperty, instancePropertyObject);
				if(!model.contains(s)) {
					model.add(s);
				}
				
				model.add(s);
			}
		}
	}
	
	private String transformToKey(String entry) {
		return entry.replaceAll(" ", "_");
	}
	
	private String getDefaultNS() {
		return model.getNsPrefixURI("");
	}
	 
    public static void main( String[] args )
    {	
        ExcelImporter importer = new ExcelImporter("smallonto_empty.owl", "small_trial.xlsx");
        importer.ImportXlsxToOwl();
    }
}
