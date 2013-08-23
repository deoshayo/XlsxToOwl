package at.ac.tuwien.importer.main;


public class Xlsx2Owl {
	
	private Importer importer;
	private Exporter exporter;
	
	public Xlsx2Owl() {
		importer = new Importer();
		exporter = new Exporter();
	}
	
	public void importXlsxModelToOwl(String excelSource, String owlTarget) {
		if(excelSource!=null && owlTarget!=null) {
			importer.importXlsxModelToOwl(excelSource, owlTarget);
		}
	}
	
	public void importXlsxDataToOwl(String excelSource, String owlTarget) {
		if(excelSource!=null && owlTarget!=null) {
			importer.importXlsxDataToOwl(excelSource, owlTarget);
		}
	}
	
	public void exportOwlModelToXlsx(String owlSource, String excelTarget) {
		if(owlSource!=null && excelTarget!=null) {
			exporter.exportOwlModelToXlsx(owlSource, excelTarget);
		}
	}
	
	public void exportOwlDataToXlsx(String owlSource, String excelTarget) {
		if(owlSource!=null && excelTarget!=null) {
			exporter.exportOwlDataToXlsx(owlSource, excelTarget);
		}
	}
	
	public static void main(String[] args) {
		Xlsx2Owl owl = new Xlsx2Owl();
		owl.importXlsxModelToOwl("Data Extraction.xlsx", "emsebokinspection.owl");
		owl.importXlsxDataToOwl("Data Extraction.xlsx", "emsebokinspection.owl");
	}
	
}
