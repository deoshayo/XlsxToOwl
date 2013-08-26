package at.ac.tuwien.importer.helper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class OwlHelper {

	public static final String MAPPING_SHEET = "Ontology Mapping";
	public static String DEFAULT_NS = "http://www.cdl.ifs.tuwien.ac.at/xlsx2onto?#"; 
	
	public static OntModel readFile(String URL) {
		OntModel model = ModelFactory.createOntologyModel();
		try {
			InputStream in = FileManager.get().open(URL);
			if(in==null) throw new IllegalArgumentException("File: '"+URL+"' not found");
			model.read(in, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}
	
	public static OntModel readOrCreateFile(String URL) {
		OntModel model = ModelFactory.createOntologyModel();
		try {
			InputStream in = FileManager.get().open(URL);
			if(in!=null) model.read(in, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (model.getNsPrefixURI("") == null) {
				model.setNsPrefix("", DEFAULT_NS.replace("?", ""));
//				model.setNsPrefix("", DEFAULT_NS.replace("?", Long.toString(Calendar.getInstance().getTimeInMillis())));
			}
		}
		return model;
	}
	
	public static OntModel create() {
		OntModel model = ModelFactory.createOntologyModel();
		return model;
	}
	
	public static void writeFile(OntModel model, String URL) {
		
		try {
			FileOutputStream fileOut = new FileOutputStream(URL);
			model.write(fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
