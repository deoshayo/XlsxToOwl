package at.ac.tuwien.importer.helper;

import java.util.HashMap;
import java.util.Map;

public class ExcelToOnto {

	private String excelSheetName;
	private String ontoConcept;
	private int key;
	Map<Integer, ExcelToOntoEntry> mappingMap;
	
	public ExcelToOnto(String sheetName, String concept, Map<Integer, ExcelToOntoEntry> entries) {
		excelSheetName = sheetName;
		ontoConcept = concept;
		if(entries!=null) {
			mappingMap = entries;
		} else {
			mappingMap = new HashMap<Integer, ExcelToOntoEntry>();
		}
	}
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public String getExcelSheetName() {
		return excelSheetName;
	}
	public void setExcelSheetName(String excelSheetName) {
		this.excelSheetName = excelSheetName;
	}
	public String getOntoConcept() {
		return ontoConcept;
	}
	public void setOntoConcept(String ontoConcept) {
		this.ontoConcept = ontoConcept;
	}
	public Map<Integer, ExcelToOntoEntry> getMappingMap() {
		return mappingMap;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("## ").append(ontoConcept).append(" ##").append("\n");
		sb.append("column: ").append(excelSheetName).append("\n");
		for (ExcelToOntoEntry entry : mappingMap.values()) {
			sb.append(entry.toString());
		}
		sb.append("##").append("\n").append("\n");
		return sb.toString();
	}
}
