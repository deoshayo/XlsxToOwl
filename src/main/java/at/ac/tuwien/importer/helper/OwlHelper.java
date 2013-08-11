package at.ac.tuwien.importer.helper;

import java.io.InputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class OwlHelper {
	
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
	
	public static String getDefaultNS(OntModel model) {
		return model.getNsPrefixURI("");
	}

}
