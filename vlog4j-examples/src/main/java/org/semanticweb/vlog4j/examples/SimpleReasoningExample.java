package org.semanticweb.vlog4j.examples;

/*-
 * #%L
 * VLog4j Examples
 * %%
 * Copyright (C) 2018 - 2019 VLog4j Developers
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

import java.io.IOException;

import org.semanticweb.vlog4j.core.exceptions.VLog4jException;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.parser.ParsingException;
import org.semanticweb.vlog4j.parser.RuleParser;

/**
 * This example demonstrates the basic usage of VLog4j for rule reasoning. We
 * are using a fixed set of rules and facts defined in Java without any external
 * sources, and we query for some of the results.
 * 
 * @author Markus Kroetzsch
 *
 */
public class SimpleReasoningExample {

	public static void main(final String[] args) throws IOException {

		ExamplesUtils.configureLogging(); // use simple logger for the example

		// Define some facts and rules in VLog's basic syntax:
		String rules = "% --- Some facts --- \n" //
				+ "location(germany,europe). \n" //
				+ "location(uk,europe). \n" //
				+ "location(saxony,germany). \n" //
				+ "location(dresden,saxony). \n" //
				+ "city(dresden). \n" //
				+ "country(germany). \n" //
				+ "country(uk). \n" //
				+ "university(tudresden, germany). \n" //
				+ "university(uoxford, uk) . \n" //
				+ "streetAddress(tudresden, \"Mommsenstraße 9\", \"01069\", \"Dresden\") . \n" //
				+ "zipLocation(\"01069\", dresden) . \n" //
				+ "% --- Standard recursion: locations are transitive --- \n" //
				+ "locatedIn(?X,?Y) :- location(?X,?Y) . \n" //
				+ "locatedIn(?X,?Z) :- locatedIn(?X,?Y), locatedIn(?Y,?Z) . \n" //
				+ "% --- Build address facts using the city constant --- \n" //
				+ "address(?Uni, ?Street, ?ZIP, ?City) :- streetAddress(?Uni, ?Street, ?ZIP, ?CityName), zipLocation(?ZIP, ?City) . \n"
				+ "% --- Value invention: universities have some address --- \n" //
				+ "address(?Uni, !Street, !ZIP, !City), locatedIn(!City, ?Country) :- university(?Uni, ?Country) . \n"
				+ "% --- Negation: organisations in Europe but not in Germany --- \n" //
				+ "inEuropeOutsideGermany(?Org) :- address(?Org, ?S, ?Z, ?City), locatedIn(?City, europe), ~locatedIn(?City, germany) . \n"
				+ "% ---\n";

		System.out.println("Knowledge base used in this example:\n\n" + rules);

		RuleParser ruleParser = new RuleParser();
		try {
			ruleParser.parse(rules);
		} catch (ParsingException e) {
			System.out.println("Failed to parse rules: " + e.getMessage());
			return;
		}

		try (final Reasoner reasoner = Reasoner.getInstance()) {
			reasoner.addFacts(ruleParser.getFacts());
			reasoner.addRules(ruleParser.getRules());

			System.out.print("Loading rules and facts ... ");
			reasoner.load();
			System.out.println("done.");

			System.out.print("Computing all inferences ... ");
			reasoner.reason();
			System.out.println("done.\n");

			/* Execute some queries */
			ExamplesUtils.printOutQueryAnswers("address(?Org, ?Street, ?ZIP, ?City)", reasoner);
			ExamplesUtils.printOutQueryAnswers("locatedIn(?place, europe)", reasoner);
			ExamplesUtils.printOutQueryAnswers("inEuropeOutsideGermany(?Org)", reasoner);

			System.out.println("Done.");
		} catch (VLog4jException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}
