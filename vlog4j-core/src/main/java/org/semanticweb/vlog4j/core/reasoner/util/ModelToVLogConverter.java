package org.semanticweb.vlog4j.core.reasoner.util;

import java.util.Collection;

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

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Conjunction;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.reasoner.RuleRewriteStrategy;

/**
 * Utility class with static methods for converting from
 * {@code org.semanticweb.vlog4j.core.model} model objects to
 * {@code karmaresearch.vlog} model objects
 * 
 * @author Irina Dragoste
 *
 */
public final class ModelToVLogConverter {

	public static final String PREDICATE_ARITY_SUFFIX_SEPARATOR = "-";

	private ModelToVLogConverter() {
	}

	public static karmaresearch.vlog.Term toVLogTerm(final Term term) {
		final TermToVLogConverter termToVLogConverter = new TermToVLogConverter();
		return term.accept(termToVLogConverter);
	}

	public static karmaresearch.vlog.Term[] toVLogTermArray(final List<Term> terms) {
		final karmaresearch.vlog.Term[] vLogTerms = new karmaresearch.vlog.Term[terms.size()];
		int i = 0;
		for (final Term term : terms) {
			vLogTerms[i] = toVLogTerm(term);
			i++;
		}
		return vLogTerms;
	}

	public static String[][] toVLogFactTuples(final Collection<Atom> facts) {
		final String[][] tuples = new String[facts.size()][];
		int i = 0;
		for (final Atom atom : facts) {
			final String[] vLogFactTuple = ModelToVLogConverter.toVLogFactTuple(atom);
			tuples[i] = vLogFactTuple;
			i++;
		}
		return tuples;
	}

	private static String[] toVLogFactTuple(final Atom fact) {
		final List<Term> terms = fact.getTerms();
		final String[] vLogFactTuple = new String[terms.size()];
		int i = 0;
		for (final Term term : terms) {
			final String vLogTupleTerm = term.getName();
			vLogFactTuple[i] = vLogTupleTerm;
			i++;
		}
		return vLogFactTuple;
	}

	/**
	 * Internal String representation that uniquely identifies a {@link Predicate}.
	 * 
	 * @param predicate
	 *            a {@link Predicate}
	 * @return String representation corresponding to given predicate name and
	 *         arity.
	 */
	public static String toVLogPredicate(Predicate predicate) {
		final String vLogPredicate = predicate.getName() + PREDICATE_ARITY_SUFFIX_SEPARATOR + predicate.getArity();
		return vLogPredicate;
	}

	public static karmaresearch.vlog.Atom toVLogAtom(final Atom atom) {
		final karmaresearch.vlog.Term[] vLogTerms = toVLogTermArray(atom.getTerms());
		final String vLogPredicate = toVLogPredicate(atom.getPredicate());
		final karmaresearch.vlog.Atom vLogAtom = new karmaresearch.vlog.Atom(vLogPredicate, vLogTerms);
		return vLogAtom;
	}

	private static karmaresearch.vlog.Atom[] toVLogAtomArray(final Conjunction conjunction) {
		final karmaresearch.vlog.Atom[] vLogAtoms = new karmaresearch.vlog.Atom[conjunction.getAtoms().size()];
		int i = 0;
		for (final Atom atom : conjunction.getAtoms()) {
			vLogAtoms[i] = toVLogAtom(atom);
			i++;
		}
		return vLogAtoms;
	}

	private static karmaresearch.vlog.Rule toVLogRule(final Rule rule) {
		final karmaresearch.vlog.Atom[] vLogHead = toVLogAtomArray(rule.getHead());
		final karmaresearch.vlog.Atom[] vLogBody = toVLogAtomArray(rule.getBody());
		return new karmaresearch.vlog.Rule(vLogHead, vLogBody);
	}

	public static karmaresearch.vlog.Rule[] toVLogRuleArray(final List<Rule> rules) {
		final karmaresearch.vlog.Rule[] vLogRules = new karmaresearch.vlog.Rule[rules.size()];
		int i = 0;
		for (final Rule rule : rules) {
			vLogRules[i] = toVLogRule(rule);
			i++;
		}
		return vLogRules;
	}

	public static karmaresearch.vlog.VLog.RuleRewriteStrategy toVLogRuleRewriteStrategy(
			final RuleRewriteStrategy ruleRewriteStrategy) {
		Validate.notNull(ruleRewriteStrategy);
		switch (ruleRewriteStrategy) {
		case SPLIT_HEAD_PIECES:
			return karmaresearch.vlog.VLog.RuleRewriteStrategy.AGGRESSIVE;
		case NONE:
		default:
			return karmaresearch.vlog.VLog.RuleRewriteStrategy.NONE;
		}
	}
}
