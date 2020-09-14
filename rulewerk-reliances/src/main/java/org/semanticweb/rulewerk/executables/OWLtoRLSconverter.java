package org.semanticweb.rulewerk.executables;

/*-
 * #%L
 * Rulewerk Reliances
 * %%
 * Copyright (C) 2018 - 2020 Rulewerk Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.rulewerk.core.model.api.Fact;
import org.semanticweb.rulewerk.core.model.api.Rule;
import org.semanticweb.rulewerk.owlapi.OwlToRulesConverter;

public class OWLtoRLSconverter {

	static String manchesterOriginalPath = "/home/lgonzale/ontologies/mowlcorp/data/original";
	static String oxfordOriginalPath = "/home/lgonzale/ontologies/oxford-ontology-library/data/original";
	static String manchesterRLSPath = "/home/lgonzale/ontologies/mowlcorp/data/rls";
	static String oxfordRLSPath = "/home/lgonzale/ontologies/oxford-ontology-library/data/rls";

	static private void saveRLS(String outputPath, String ontologyName, Set<Rule> rules, Set<Fact> facts)
			throws IOException {
		String content = "";
		for (Rule rule : rules) {
			content += rule + "\n";
		}

		content += "\n";
		for (Fact fact : facts) {
			content += fact + "\n";
		}

		FileWriter myWriter = new FileWriter(outputPath + "/" + ontologyName + ".rls");
		myWriter.write(content);
		myWriter.close();
	}

	static private void transform(String inputPath, String outputPath, String ontologyName) throws IOException {
		Path inputOntologyPath = Paths.get(manchesterOriginalPath, ontologyName);

//		System.out.println(inputOntologyPath);

		/* inputOntology is loaded using OWL API */
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();

		try {
			OWLOntology inputOntology = ontologyManager
					.loadOntologyFromOntologyDocument(new File(inputOntologyPath.toString()));

			OwlToRulesConverter owlToRulesConverter = new OwlToRulesConverter();
			owlToRulesConverter.addOntology(inputOntology);

			saveRLS(outputPath, ontologyName, owlToRulesConverter.getRules(), owlToRulesConverter.getFacts());
		} catch (OWLOntologyCreationException e) {
			System.out.println("OWLOntologyCreationException with ontology: " + ontologyName);
		} catch (Exception e) {
			System.out.println("Exception with ontology: " + ontologyName);
		}

	}

	static public void main(String args[]) throws IOException {

		File manchesterOriginalDir = new File(manchesterOriginalPath);
		File oxfordOriginalDir = new File(oxfordOriginalPath);

		String manchesterOriginalContent[] = manchesterOriginalDir.list();
		String oxfordOriginalContent[] = oxfordOriginalDir.list();

		for (String filename : manchesterOriginalContent) {
			transform(manchesterOriginalPath, manchesterRLSPath, filename);
		}

		for (String filename : oxfordOriginalContent) {
			transform(oxfordOriginalPath, oxfordRLSPath, filename);
		}
	}
}