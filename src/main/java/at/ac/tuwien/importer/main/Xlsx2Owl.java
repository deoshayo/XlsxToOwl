package at.ac.tuwien.importer.main;

public class Xlsx2Owl {

    private Importer importer;
    private Exporter exporter;

    public Xlsx2Owl() {
        importer = new Importer();
        exporter = new Exporter();
    }

    public void importXlsxModelToOwl(String excelSource, String owlTarget) {
        if (excelSource != null && owlTarget != null) {
            importer.importXlsxModelToOwl(excelSource, owlTarget);
        }
    }

    public void importXlsxDataToOwl(String excelSource, String owlTarget) {
        if (excelSource != null && owlTarget != null) {
            importer.importXlsxDataToOwl(excelSource, owlTarget);
        }
    }

    public void exportOwlModelToXlsx(String owlSource, String excelTarget) {
        if (owlSource != null && excelTarget != null) {
            exporter.exportOwlModelToXlsx(owlSource, excelTarget);
        }
    }

    public void exportOwlDataToXlsx(String owlSource, String excelTarget) {
        if (owlSource != null && excelTarget != null) {
            exporter.exportOwlDataToXlsx(owlSource, excelTarget);
        }
    }

    public static void main(String[] args) {
        Xlsx2Owl owl = new Xlsx2Owl();
        owl.importXlsxModelToOwl("ESEM/ESEM_Mapper_140302_SB.xlsx", "ESEM/ESEM.owl");
        owl.importXlsxDataToOwl("ESEM/ESEM_Instances_140302_SB.xlsx", "ESEM/ESEM.owl");

//        owl.importXlsxModelToOwl("Data Extraction.xlsx", "EMSE.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_CK.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_DM_Fixed.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_FW.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_GM.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_GM2.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_JFM.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_MA.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_MA2.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_MK.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_MR.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_MR2.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_VF.xlsx", "emsebokinspection.owl");
//        owl.importXlsxDataToOwl("emsebok/Data Extraction_GM_PBR.xlsx", "emsebokinspection.owl");

    }
}
