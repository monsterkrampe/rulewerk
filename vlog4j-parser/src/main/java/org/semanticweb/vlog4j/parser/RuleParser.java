package org.semanticweb.vlog4j.parser;

/*-
 * #%L
 * vlog4j-parser
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.semanticweb.vlog4j.core.exceptions.PrefixDeclarationException;
import org.semanticweb.vlog4j.core.model.api.DataSource;
import org.semanticweb.vlog4j.core.model.api.DataSourceDeclaration;
import org.semanticweb.vlog4j.core.model.api.Fact;
import org.semanticweb.vlog4j.core.model.api.Literal;
import org.semanticweb.vlog4j.core.model.api.PositiveLiteral;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.reasoner.KnowledgeBase;
import org.semanticweb.vlog4j.parser.javacc.JavaCCParser;
import org.semanticweb.vlog4j.parser.javacc.JavaCCParserBase.FormulaContext;
import org.semanticweb.vlog4j.parser.javacc.ParseException;
import org.semanticweb.vlog4j.parser.javacc.TokenMgrError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to statically access VLog parsing functionality.
 *
 * @author Markus Kroetzsch
 *
 */
public class RuleParser {

	private static Logger LOGGER = LoggerFactory.getLogger(RuleParser.class);

	public static void parseInto(final KnowledgeBase knowledgeBase, final InputStream stream, final String encoding,
			final ParserConfiguration parserConfiguration) throws ParsingException {
		final JavaCCParser parser = new JavaCCParser(stream, encoding);
		parser.setKnowledgeBase(knowledgeBase);
		parser.setParserConfiguration(parserConfiguration);
		doParse(parser);
	}

	public static void parseInto(final KnowledgeBase knowledgeBase, final InputStream stream,
			final ParserConfiguration parserConfiguration) throws ParsingException {
		parseInto(knowledgeBase, stream, "UTF-8", parserConfiguration);
	}

	public static void parseInto(final KnowledgeBase knowledgeBase, final String input,
			final ParserConfiguration parserConfiguration) throws ParsingException {
		final InputStream inputStream = new ByteArrayInputStream(input.getBytes());
		parseInto(knowledgeBase, inputStream, "UTF-8", parserConfiguration);
	}

	public static void parseInto(final KnowledgeBase knowledgeBase, final InputStream stream, final String encoding)
			throws ParsingException {
		final JavaCCParser javaCcParser = new JavaCCParser(stream, encoding);
		javaCcParser.setKnowledgeBase(knowledgeBase);
		doParse(javaCcParser);
	}

	public static void parseInto(final KnowledgeBase knowledgeBase, final InputStream stream) throws ParsingException {
		parseInto(knowledgeBase, stream, "UTF-8");
	}

	public static void parseInto(final KnowledgeBase knowledgeBase, final String input) throws ParsingException {
		final InputStream inputStream = new ByteArrayInputStream(input.getBytes());
		parseInto(knowledgeBase, inputStream, "UTF-8");
	}

	public static KnowledgeBase parse(final InputStream stream, final String encoding,
			final ParserConfiguration parserConfiguration) throws ParsingException {
		JavaCCParser parser = new JavaCCParser(stream, encoding);
		parser.setParserConfiguration(parserConfiguration);
		return doParse(parser);
	}

	public static KnowledgeBase parse(final InputStream stream, final ParserConfiguration parserConfiguration)
			throws ParsingException {
		return parse(stream, "UTF-8", parserConfiguration);
	}

	public static KnowledgeBase parse(final String input, final ParserConfiguration parserConfiguration)
			throws ParsingException {
		final InputStream inputStream = new ByteArrayInputStream(input.getBytes());
		return parse(inputStream, "UTF-8", parserConfiguration);
	}

	public static KnowledgeBase parse(final InputStream stream, final String encoding) throws ParsingException {
		return doParse(new JavaCCParser(stream, encoding));
	}

	public static KnowledgeBase parse(final InputStream stream) throws ParsingException {
		return parse(stream, "UTF-8");
	}

	public static KnowledgeBase parse(final String input) throws ParsingException {
		final InputStream inputStream = new ByteArrayInputStream(input.getBytes());
		return parse(inputStream, "UTF-8");
	}

	/**
	 * Interface for a method parsing a fragment of the supported syntax.
	 *
	 * This is needed to specify the exceptions thrown by the parse method.
	 */
	@FunctionalInterface
	interface SyntaxFragmentParser<T> {
		T parse(final JavaCCParser parser)
				throws ParsingException, ParseException, PrefixDeclarationException, TokenMgrError;
	}

	/**
	 * Parse a syntax fragment.
	 *
	 * @param input               Input string.
	 * @param parserAction        Parsing method for the {@code T}.
	 * @param syntaxFragmentType  Description of the type {@code T} being parsed.
	 * @param parserConfiguration {@link ParserConfiguration} instance, or null.
	 *
	 * @throws ParsingException when an error during parsing occurs.
	 * @return an appropriate instance of {@code T}
	 */
	static <T> T parseSyntaxFragment(final String input, SyntaxFragmentParser<T> parserAction,
			final String syntaxFragmentType, final ParserConfiguration parserConfiguration) throws ParsingException {
		final InputStream inputStream = new ByteArrayInputStream(input.getBytes());
		final JavaCCParser localParser = new JavaCCParser(inputStream, "UTF-8");

		if (parserConfiguration != null) {
			localParser.setParserConfiguration(parserConfiguration);
		}

		try {
			T result = parserAction.parse(localParser);
			localParser.ensureEndOfInput();
			return result;
		} catch (ParseException | PrefixDeclarationException | TokenMgrError e) {
			LOGGER.error("Exception while parsing " + syntaxFragmentType + ": {}!", input);
			throw new ParsingException("Exception while parsing " + syntaxFragmentType, e);
		}
	}

	public static Rule parseRule(final String input, final ParserConfiguration parserConfiguration)
			throws ParsingException {
		return parseSyntaxFragment(input, JavaCCParser::rule, "rule", parserConfiguration);
	}

	public static Rule parseRule(final String input) throws ParsingException {
		return parseRule(input, null);
	}

	public static Literal parseLiteral(final String input, final ParserConfiguration parserConfiguration)
			throws ParsingException {
		return parseSyntaxFragment(input, parser -> parser.literal(FormulaContext.HEAD), "literal",
				parserConfiguration);
	}

	public static Literal parseLiteral(final String input) throws ParsingException {
		return parseLiteral(input, null);
	}

	public static PositiveLiteral parsePositiveLiteral(final String input,
			final ParserConfiguration parserConfiguration) throws ParsingException {
		return parseSyntaxFragment(input, parser -> parser.positiveLiteral(FormulaContext.HEAD), "positivel literal",
				parserConfiguration);
	}

	public static PositiveLiteral parsePositiveLiteral(final String input) throws ParsingException {
		return parsePositiveLiteral(input, null);
	}

	public static Fact parseFact(final String input, final ParserConfiguration parserConfiguration)
			throws ParsingException {
		return parseSyntaxFragment(input, parser -> parser.fact(FormulaContext.HEAD), "fact", parserConfiguration);
	}

	public static Fact parseFact(final String input) throws ParsingException {
		return parseFact(input, null);
	}

	public static Term parseTerm(final String input, final FormulaContext context,
			final ParserConfiguration parserConfiguration) throws ParsingException {
		return parseSyntaxFragment(input, parser -> parser.term(context), "term", parserConfiguration);
	}

	public static Term parseTerm(final String input, final ParserConfiguration parserConfiguration)
			throws ParsingException {
		return parseTerm(input, FormulaContext.HEAD, parserConfiguration);
	}

	public static Term parseTerm(final String input, final FormulaContext context) throws ParsingException {
		return parseTerm(input, context, null);
	}

	public static Term parseTerm(final String input) throws ParsingException {
		return parseTerm(input, (ParserConfiguration) null);
	}

	public static DataSource parseDataSourceDeclaration(final String input, ParserConfiguration parserConfiguration)
			throws ParsingException {
		return parseSyntaxFragment(input, RuleParser::parseAndExtractDatasourceDeclaration, "data source declaration",
				parserConfiguration);
	}

	public static DataSource parseDataSourceDeclaration(final String input) throws ParsingException {
		return parseDataSourceDeclaration(input, null);
	}

	static KnowledgeBase doParse(final JavaCCParser parser) throws ParsingException {
		try {
			parser.parse();
		} catch (ParseException | PrefixDeclarationException | TokenMgrError e) {
			LOGGER.error("Exception while parsing Knowledge Base!", e);
			throw new ParsingException("Exception while parsing Knowledge Base.", e);
		}
		return parser.getKnowledgeBase();
	}

	protected static DataSource parseAndExtractDatasourceDeclaration(final JavaCCParser parser)
			throws ParsingException, ParseException, PrefixDeclarationException {
		parser.source();

		final List<DataSourceDeclaration> dataSourceDeclarations = parser.getKnowledgeBase()
				.getDataSourceDeclarations();

		if (dataSourceDeclarations.size() != 1) {
			throw new ParsingException(
					"Unexpected number of data source declarations: " + dataSourceDeclarations.size());
		}

		return dataSourceDeclarations.get(0).getDataSource();
	}

}
