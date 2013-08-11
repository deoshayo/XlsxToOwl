package at.ac.tuwien.importer.helper;

public class ExcelToOntoEntry {
	private String excelColumnName;
	private String ontoProperty;
	private String key;
	private String datatype;
	private int	columnNumber;
	
	public ExcelToOntoEntry(String columnName, String ontoProperty, String key, String datatype, int colNum) {
		this.excelColumnName = columnName;
		this.ontoProperty = ontoProperty;
		this.key = key;
		this.datatype = datatype;
		this.columnNumber = colNum;
	}
	
	public String getExcelColumnName() {
		return excelColumnName;
	}
	public void setExcelColumnName(String excelColumnName) {
		this.excelColumnName = excelColumnName;
	}
	public String getOntoProperty() {
		return ontoProperty;
	}
	public void setOntoProperty(String ontoProperty) {
		this.ontoProperty = ontoProperty;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	public int getColumnNumber() {
		return columnNumber;
	}
	public void setColumnNumber(int columnNumber) {
		this.columnNumber = columnNumber;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("# ").append(ontoProperty).append(" #").append("\n");
		sb.append("column: ").append(excelColumnName).append("\n");
		sb.append("key: ").append(key).append("\n");
		sb.append("datatype: ").append(datatype).append("\n");
		sb.append("number: ").append(columnNumber).append("\n");
		sb.append("#").append("\n").append("\n");
		return sb.toString();
	}
}
