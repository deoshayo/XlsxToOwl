package at.ac.tuwien.importer.main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.ac.tuwien.importer.helper.ExcelHelper;
import at.ac.tuwien.importer.helper.ExcelModel;
import at.ac.tuwien.importer.helper.ExcelModelEntry;
import at.ac.tuwien.importer.helper.OwlHelper;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Hello world!
 *
 */
public class Importer 
{	
	Map<String, ExcelModel> modelMapper = new HashMap<String, ExcelModel>();
	OntModel model;
	XSSFWorkbook workbook;
	
	public Importer() {
		model = null;
		workbook = null;
	}

	public void importXlsxModelToOwl(String excelSource, String owlTarget) {
		init(excelSource, owlTarget);
		importModel();
		OwlHelper.writeFile(model, owlTarget);
		System.out.println("Successfully importing '"+excelSource+"' Model into '"+owlTarget);
	}

	public void importXlsxDataToOwl(String excelSource, String owlTarget) {
		init(excelSource, owlTarget);
		importModel();
		importData();
		OwlHelper.writeFile(model, owlTarget);
		System.out.println("Successfully importing '"+excelSource+"' Model & Data into '"+owlTarget);
	}
	
	private void init(String excelSource, String owlTarget) {
		model = OwlHelper.readOrCreateFile(owlTarget);
		workbook = ExcelHelper.readFile(excelSource);
		storeModelMap();
	}
	
	private void importModel() {
		for (String sheetName : modelMapper.keySet()) {
			ExcelModel sheetModel = modelMapper.get(sheetName);
			OntClass sheetClass = model.createClass(model.getNsPrefixURI("")+sheetModel.getOntoConcept());
			for(Integer sheetColumnNumber : sheetModel.keySet()) {
				ExcelModelEntry modelEntry = sheetModel.get(sheetColumnNumber);
				
				OntProperty property;
				Resource classRange = XSD.xstring;
				
				if(modelEntry.getKey().equalsIgnoreCase("fk")) {
					property = model.createObjectProperty(model.getNsPrefixURI("")+modelEntry.getOntoProperty());
					classRange = model.createClass(model.getNsPrefixURI("")+modelEntry.getDatatype());
					if(!modelEntry.getAdditionalProperties().isEmpty()) {
						createAdditionalProperties(classRange, modelEntry.getAdditionalProperties());
					}
				} else {
					property = model.createDatatypeProperty(model.getNsPrefixURI("")+modelEntry.getOntoProperty());
					if(modelEntry.getKey().equalsIgnoreCase("pk")) {
						property.convertToFunctionalProperty();
					}
				}

				property.setRange(classRange);
				property.setDomain(sheetClass);
			}
		}
	}
	
	private void createAdditionalProperties(Resource ontClass, List<String> additionalProps) {
		Iterator<String> propsIterator = additionalProps.iterator();
		while(propsIterator.hasNext()) {
			String[] props = propsIterator.next().split("=");
			OntProperty prop = model.createDatatypeProperty(getDefaultNS()+props[0]);
			prop.setDomain(ontClass);
		}
	}
	 
	private void importData() {
		for (XSSFSheet xssfSheet : workbook) {
			if(xssfSheet.getSheetName().equalsIgnoreCase(OwlHelper.MAPPING_SHEET)) continue;
			processSheet(xssfSheet);
		}
	}
	
	private void processSheet(XSSFSheet sheet) {
		ExcelModel map = modelMapper.get(sheet.getSheetName());
		if(map==null) return;
		for(Row row: sheet) {
			Cell primaryKeyCell = row.getCell(map.getPrimaryKey());
			if(primaryKeyCell == null) continue;
			if(row.getCell(map.getPrimaryKey()).getStringCellValue().isEmpty() || (row.getRowNum()==0)) continue;
			
			// TODO: to be separated into processRow()
			OntClass concept = model.getOntClass(getDefaultNS()+map.getOntoConcept()); // ontoconcept
			String PK = transformToKey(row.getCell(map.getPrimaryKey()).getStringCellValue());
			
			Individual instance = model.createIndividual(getDefaultNS()+PK, concept);
			
			for(int i: map.keySet()) {
				ExcelModelEntry mapEntry = map.get(i);
				Cell cell = row.getCell(mapEntry.getColumnNumber());
				if(cell==null) continue;
				Statement s = processCell(instance, mapEntry, cell);
				if(s==null) continue;
				if(!model.contains(s)) {
					model.add(s);
				}
			}
		}
	}
	
	private Statement processCell(Individual instance, ExcelModelEntry mapEntry, Cell cell) {
		
		OntProperty instanceProperty = model.getOntProperty(getDefaultNS()+mapEntry.getOntoProperty());
		if(cell.getCellType()==Cell.CELL_TYPE_NUMERIC) cell.setCellType(Cell.CELL_TYPE_STRING);
		String instancePropertyValue = cell.getStringCellValue();
		if(instancePropertyValue.equalsIgnoreCase("BLANK")) return null;
		RDFNode instancePropertyObject = null;
		
		if(mapEntry.getKey().equalsIgnoreCase("fk")) {
			instancePropertyValue = transformToKey(instancePropertyValue);
			OntResource domain = instanceProperty.getRange();
			instancePropertyObject = model.createIndividual(getDefaultNS()+instancePropertyValue, domain);
		} else {
			instancePropertyObject = ResourceFactory.createPlainLiteral(instancePropertyValue);
		}
		
		return ResourceFactory.createStatement(instance, instanceProperty, instancePropertyObject);
	}
	
	private void storeModelMap() {
		XSSFSheet sheet = workbook.getSheet(OwlHelper.MAPPING_SHEET);
		
		for (Row row: sheet) {
			if(row.getRowNum()==0) continue; // header	
			
			String sheetName = row.getCell(0).getStringCellValue();
			String concept = row.getCell(1).getStringCellValue();
			
			ExcelModel map;
			if(modelMapper.containsKey(sheetName)) {
				map = modelMapper.get(sheetName);
			} else {
				map = new ExcelModel(sheetName, concept, null);
				modelMapper.put(sheetName, map);
			}
			
			String excelColumn = row.getCell(2).getStringCellValue();
			String ontoProperty = row.getCell(3).getStringCellValue();
			String key = row.getCell(4).getStringCellValue();
			String datatype = row.getCell(5).getStringCellValue();
			int num = (int) row.getCell(6).getNumericCellValue();
			if(key.equalsIgnoreCase("pk")) map.setPrimaryKey(num);
			int card = (int) row.getCell(7).getNumericCellValue();
			List<String> allowedVals = Arrays.asList(row.getCell(8).getStringCellValue().split(";"));
			List<String> addProps = Arrays.asList(row.getCell(8).getStringCellValue().split(";"));
			
			ExcelModelEntry entry = new ExcelModelEntry(excelColumn, ontoProperty, key, datatype, num, card, allowedVals, addProps);
			map.put(num, entry);
		}
	}
	
	private String getDefaultNS() {
		return model.getNsPrefixURI("");
	}
	
	private String transformToKey(String entry) {
		return entry.replaceAll(" ", "_");
	}
}
