package at.ac.tuwien.importer.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.ParseException;
import org.jbibtex.Value;

import at.ac.tuwien.importer.helper.CommonHelper;
import at.ac.tuwien.importer.helper.ExcelHelper;
import at.ac.tuwien.importer.helper.ExcelModel;
import at.ac.tuwien.importer.helper.ExcelModelEntry;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Hello world!
 * 
 */
public class Importer {
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
        importConcepts();
        CommonHelper.writeFile(model, owlTarget);
        System.out.println("Successfully importing '" + excelSource + "' Model into '" + owlTarget);
    }

    public void importXlsxDataToOwl(String excelSource, String owlTarget) {
        init(excelSource, owlTarget);
        importData();
        CommonHelper.writeFile(model, owlTarget);
        System.out.println("Successfully importing '" + excelSource + "' Model & Data into '" + owlTarget);
    }

    private void init(String excelSource, String owlTarget) {
        model = CommonHelper.readOrCreateFile(owlTarget);
        workbook = ExcelHelper.readFile(excelSource);
        storeModelMap();
    }

    private void importConcepts() {
        XSSFSheet sheet = workbook.getSheet(CommonHelper.CONCEPT_SHEET);
        if (sheet == null)
            return;

        OntClass concept = model.createClass(model.getNsPrefixURI("") + "Concept");
        OntProperty propID = model.createDatatypeProperty(getDefaultNS() + "conceptName", true);
        propID.setDomain(concept);
        propID.setRange(XSD.xstring);

        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue; // header

            String conceptName = row.getCell(1).getStringCellValue();
            String synonims = row.getCell(5).getStringCellValue();

            if (conceptName.trim().isEmpty())
                continue;
            Individual conceptInstance =
                model.createIndividual(getDefaultNS() + "CON_" + transformToKey(conceptName), concept);
            conceptInstance.addProperty(propID, conceptName);
            String[] conceptSynonims = synonims.split(";");
            for (String s : conceptSynonims) {
                if (s.trim().isEmpty())
                    continue;
                Property p = model.getProperty(model.getNsPrefixURI("owl"), "sameAs");
                Individual synonymInstance =
                    model.createIndividual(getDefaultNS() + "CON_" + transformToKey(s), concept);
                conceptInstance.addProperty(p, synonymInstance);
                synonymInstance.addProperty(propID, s);
            }
        }
    }

    private void importModel() {
        for (String sheetName : modelMapper.keySet()) {
            ExcelModel sheetModel = modelMapper.get(sheetName);
            OntClass sheetClass = model.createClass(model.getNsPrefixURI("") + sheetModel.getOntoConcept());
            for (Integer sheetColumnNumber : sheetModel.keySet()) {
                ExcelModelEntry modelEntry = sheetModel.get(sheetColumnNumber);

                OntProperty property;
                Resource classRange = XSD.xstring;

                if (modelEntry.getKey().equalsIgnoreCase("fk")) {
                    property = model.createObjectProperty(model.getNsPrefixURI("") + modelEntry.getOntoProperty());
                    classRange = model.createClass(model.getNsPrefixURI("") + modelEntry.getDatatype());
                    if (!modelEntry.getAdditionalProperties().isEmpty()) {
                        createAddPropResource(classRange, modelEntry.getAdditionalProperties());
                    }

                    property.setRange(classRange);
                    property.setDomain(sheetClass);
                } else {
                    property = model.createDatatypeProperty(model.getNsPrefixURI("") + modelEntry.getOntoProperty());
                    if (modelEntry.getKey().equalsIgnoreCase("pk")) {
                        property.convertToFunctionalProperty();
                    }

                    property.setRange(classRange);
                    property.setDomain(sheetClass);
                }
            }
        }
    }

    private void createAddPropResource(Resource ontClass, List<String> additionalProps) {
        Iterator<String> propsIterator = additionalProps.iterator();
        while (propsIterator.hasNext()) {
            String[] props = propsIterator.next().split("=");
            OntProperty prop = model.createDatatypeProperty(getDefaultNS() + props[0]);
            prop.setDomain(ontClass);
        }
    }

    // private void createAddPropInstances(Individual indiv, List<String> additionalProps) {
    // Iterator<String> propsIterator = additionalProps.iterator();
    // while (propsIterator.hasNext()) {
    // String[] props = propsIterator.next().split("=");
    // OntProperty prop = model.createDatatypeProperty(getDefaultNS() + props[0]);
    // prop.setDomain(ontClass);
    // }
    // }

    private void importData() {
        for (XSSFSheet xssfSheet : workbook) {
            if (xssfSheet.getSheetName().equalsIgnoreCase(CommonHelper.MAPPING_SHEET))
                continue;
            processSheet(xssfSheet);
        }
    }

    private void processSheet(XSSFSheet sheet) {
        ExcelModel map = modelMapper.get(sheet.getSheetName());
        if (map == null)
            return;
        for (Row row : sheet) {
            Cell primaryKeyCell = row.getCell(map.getPrimaryKey());
            if (primaryKeyCell == null)
                continue;
            if (row.getCell(map.getPrimaryKey()).getStringCellValue().isEmpty() || (row.getRowNum() == 0))
                continue;

            // TODO: to be separated into processRow()
            OntClass concept = model.getOntClass(getDefaultNS() + map.getOntoConcept()); // ontoconcept
            String PK = transformToKey(row.getCell(map.getPrimaryKey()).getStringCellValue());

            Individual instance = model.createIndividual(getDefaultNS() + PK, concept);

            for (int i : map.keySet()) {
                ExcelModelEntry mapEntry = map.get(i);
                Cell cell = row.getCell(mapEntry.getColumnNumber());
                if (cell == null)
                    continue;
                List<Statement> listStmt = processCell(instance, mapEntry, cell);
                if (listStmt.isEmpty())
                    continue;
                model.add(listStmt);
            }
        }
    }

    private List<Statement> processCell(Individual instance, ExcelModelEntry mapEntry, Cell cell) {
        List<Statement> listStatement = new ArrayList<Statement>();
        OntProperty instanceProperty = model.getOntProperty(getDefaultNS() + mapEntry.getOntoProperty());
        cell.setCellType(Cell.CELL_TYPE_STRING);

        String instancePropertyValues = cell.getStringCellValue().trim();
        RDFNode instancePropertyObject = null;

        if (mapEntry.getKey().equalsIgnoreCase("bib")) {
            instancePropertyObject = ResourceFactory.createPlainLiteral(instancePropertyValues);
            createBibTexModelEntry(instance, instancePropertyValues);
        } else {
            String[] values = instancePropertyValues.split(";");
            for (int i = 0; i < values.length; i++) {
                String instancePropertyValue = values[i];
                if (instancePropertyValue.equalsIgnoreCase("BLANK"))
                    continue;

                if (mapEntry.getKey().equalsIgnoreCase("fk")) {
                    instancePropertyValue = transformToKey(instancePropertyValue);
                    OntResource domain = instanceProperty.getRange();
                    instancePropertyObject = model.createIndividual(getDefaultNS() + instancePropertyValue, domain);
                    if (!mapEntry.getAdditionalProperties().isEmpty()) {
                        // TODO: HARDCODE
                        OntProperty bokTopicID = model.createDatatypeProperty(getDefaultNS() + "bokTopicID", true);
                        bokTopicID.setDomain(domain);
                        bokTopicID.setRange(XSD.xstring);
                        listStatement.add(ResourceFactory.createStatement(instancePropertyObject.asResource(),
                                bokTopicID, ResourceFactory.createPlainLiteral(values[i].trim())));
                        Iterator<String> propsIterator = mapEntry.getAdditionalProperties().iterator();
                        while (propsIterator.hasNext()) {
                            String[] props = propsIterator.next().split("=");
                            OntProperty prop = model.createDatatypeProperty(getDefaultNS() + props[0], false);
                            listStatement.add(ResourceFactory.createStatement(instancePropertyObject.asResource(),
                                    prop, ResourceFactory.createPlainLiteral(props[1].trim())));
                        }
                    }
                } else {
                    instancePropertyObject = ResourceFactory.createPlainLiteral(instancePropertyValue.trim());
                }

                listStatement
                        .add(ResourceFactory.createStatement(instance, instanceProperty, instancePropertyObject));
            }
        }

        return listStatement;
    }

    private void createBibTexModelEntry(Individual instance, String bibTexString) {
        String bib_ = "bib_";
        try {
            BibTeXDatabase bdb = CommonHelper.parseBibTeX(bibTexString);
            Map<Key, BibTeXEntry> map = bdb.getEntries();
            Iterator<Key> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                Key key = iter.next();

                BibTeXEntry bib = map.get(key);
                Map<Key, Value> m = bib.getFields();
                Iterator<Key> it = m.keySet().iterator();

                while (it.hasNext()) {

                    Key k = it.next();

                    DatatypeProperty property = model.createDatatypeProperty(model.getNsPrefixURI("") + bib_ + k);
                    property.setDomain(instance.getOntClass(true));
                    property.setRange(XSD.xstring);

                    Value v = m.get(k);
                    RDFNode node = ResourceFactory.createPlainLiteral(v.toUserString().trim());
                    model.add(instance, property, node);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void storeModelMap() {
        XSSFSheet sheet = workbook.getSheet(CommonHelper.MAPPING_SHEET);
        if (sheet == null)
            return;

        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue; // header

            String sheetName = row.getCell(0).getStringCellValue();
            String concept = row.getCell(1).getStringCellValue();

            ExcelModel map;
            if (modelMapper.containsKey(sheetName)) {
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
            if (key.equalsIgnoreCase("pk"))
                map.setPrimaryKey(num);
            int card = (int) row.getCell(7).getNumericCellValue();
            List<String> allowedVals = Arrays.asList(row.getCell(8).getStringCellValue().split(";"));
            List<String> addProps = Arrays.asList(row.getCell(9).getStringCellValue().split(";"));

            ExcelModelEntry entry =
                new ExcelModelEntry(excelColumn, ontoProperty, key, datatype, num, card, allowedVals, addProps);
            map.put(num, entry);
        }
    }

    private String getDefaultNS() {
        return model.getNsPrefixURI("");
    }

    private String transformToKey(String entry) {
        entry = entry.trim();
        entry = entry.replace('(', '_');
        entry = entry.replace(')', '_');
        entry = entry.replace(' ', '_');
        entry = entry.replace(',', '_');
        entry = entry.replace(':', '_');
        entry = entry.replace('-', '_');
        entry = entry.toUpperCase();
        return entry;
    }
}
