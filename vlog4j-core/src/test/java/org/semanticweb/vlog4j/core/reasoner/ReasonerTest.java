package org.semanticweb.vlog4j.core.reasoner;

/*
 * #%L
 * VLog4j Core Components
 * %%
 * Copyright (C) 2018 VLog4j Developers
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.QueryResult;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.impl.Expressions;
import org.semanticweb.vlog4j.core.reasoner.QueryResultIterator;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.ReasonerImpl;
import org.semanticweb.vlog4j.core.reasoner.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import junit.framework.TestCase;
import karmaresearch.vlog.AlreadyStartedException;
import karmaresearch.vlog.EDBConfigurationException;
import karmaresearch.vlog.NotStartedException;

public class ReasonerTest extends TestCase {

	public void testSimpleInference() throws AlreadyStartedException, EDBConfigurationException,
			IOException, NotStartedException, ReasonerStateException, EdbIdbSeparationException {
		final String constantNameC = "c";
		final String constantNameD = "d";

		final Constant constantC = Expressions.makeConstant(constantNameC);
		final Constant constantD = Expressions.makeConstant(constantNameD);
		final Variable x = Expressions.makeVariable("x");
		final Atom factAc = Expressions.makeAtom("A", constantC);
		final Atom factAd = Expressions.makeAtom("A", constantD);
		final Atom atomAx = Expressions.makeAtom("A", x);
		final Atom atomBx = Expressions.makeAtom("B", x);
		final Atom atomCx = Expressions.makeAtom("C", x);
		final Rule ruleBxAx = Expressions.makeRule(atomBx, atomAx);
		final Rule ruleCxBx = Expressions.makeRule(atomCx, atomBx);

		final Reasoner reasoner = new ReasonerImpl();
		reasoner.addFacts(factAc, factAd);
		reasoner.addRules(ruleBxAx, ruleCxBx);
		reasoner.load();

		final QueryResultIterator cxQueryResultEnumBeforeReasoning = reasoner.answerQuery(atomCx);
		assertFalse(cxQueryResultEnumBeforeReasoning.hasNext());

		reasoner.reason();

		final QueryResultIterator cxQueryResultEnumAfterReasoning = reasoner.answerQuery(atomCx);
		final Set<QueryResult> actualResults = gatherQueryResults(cxQueryResultEnumAfterReasoning);

		final QueryResult queryResultC = Expressions.makeQueryResult(constantC);
		final QueryResult queryResultD = Expressions.makeQueryResult(constantD);
		final Set<QueryResult> expectedResults = new HashSet<>(Arrays.asList(queryResultC, queryResultD));
		assertEquals(expectedResults, actualResults);

		reasoner.dispose();
	}

	private static Set<QueryResult> gatherQueryResults(QueryResultIterator queryResultIterator) {
		final Set<QueryResult> results = new HashSet<>();
		queryResultIterator.forEachRemaining(results::add);
		queryResultIterator.dispose();
		return results;
	}

}