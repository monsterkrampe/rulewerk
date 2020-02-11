package org.semanticweb.vlog4j.syntax.parser;

/*-
 * #%L
 * VLog4j Parser
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Literal;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.parser.ConfigurableLiteralHandler;
import org.semanticweb.vlog4j.parser.ParserConfiguration;
import org.semanticweb.vlog4j.parser.ParsingException;
import org.semanticweb.vlog4j.parser.RuleParser;
import org.semanticweb.vlog4j.parser.javacc.JavaCCParserBase.ConfigurableLiteralDelimiter;
import org.semanticweb.vlog4j.parser.javacc.SubParserFactory;

public class RuleParserConfigurableLiteralTest {
	public static final Constant pipeConstant = Expressions.makeAbstractConstant("testPipe");
	public static final Constant hashConstant = Expressions.makeAbstractConstant("testHash");
	public static final Constant parenConstant = Expressions.makeAbstractConstant("testParen");
	public static final Constant braceConstant = Expressions.makeAbstractConstant("testBrace");
	public static final Constant bracketConstant = Expressions.makeAbstractConstant("testBracket");

	public static final ConfigurableLiteralHandler pipeHandler = getMockLiteralHandler(
			ConfigurableLiteralDelimiter.PIPE, pipeConstant);
	public static final ConfigurableLiteralHandler hashHandler = getMockLiteralHandler(
			ConfigurableLiteralDelimiter.HASH, hashConstant);
	public static final ConfigurableLiteralHandler parenHandler = getMockLiteralHandler(
			ConfigurableLiteralDelimiter.PAREN, parenConstant);
	public static final ConfigurableLiteralHandler braceHandler = getMockLiteralHandler(
			ConfigurableLiteralDelimiter.BRACE, braceConstant);
	public static final ConfigurableLiteralHandler bracketHandler = getMockLiteralHandler(
			ConfigurableLiteralDelimiter.BRACKET, bracketConstant);

	@Test(expected = ParsingException.class)
	public void parseLiteral_unregisteredCustomLiteral_throws() throws ParsingException {
		RuleParser.parseLiteral("p(|test|)");
	}

	@Test
	public void registerLiteral_succeeds() throws ParsingException {
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PIPE, pipeHandler);
		assertTrue("Configurable Literal Handler has been registered",
				parserConfiguration.isConfigurableLiteralRegistered(ConfigurableLiteralDelimiter.PIPE));
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerLiteral_duplicateHandler_throws() throws ParsingException, IllegalArgumentException {
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PIPE, pipeHandler)
				.registerLiteral(ConfigurableLiteralDelimiter.PIPE, hashHandler);
	}

	@Test
	public void parseLiteral_customPipeLiteral_succeeds() throws ParsingException {
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PIPE, pipeHandler);
		Literal result = RuleParser.parseLiteral("p(|test|)", parserConfiguration);
		assertEquals(pipeConstant, result.getConstants().toArray()[0]);
	}

	@Test
	public void parseLiteral_customHashLiteral_succeeds() throws ParsingException {
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.HASH, hashHandler);
		Literal result = RuleParser.parseLiteral("p(#test#)", parserConfiguration);
		assertEquals(hashConstant, result.getConstants().toArray()[0]);
	}

	@Test
	public void parseLiteral_customParenLiteral_succeeds() throws ParsingException {
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PAREN, parenHandler);
		Literal result = RuleParser.parseLiteral("p((test))", parserConfiguration);
		assertEquals(parenConstant, result.getConstants().toArray()[0]);
	}

	@Test
	public void parseLiteral_customBraceLiteral_succeeds() throws ParsingException {
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.BRACE, braceHandler);
		Literal result = RuleParser.parseLiteral("p({test})", parserConfiguration);
		assertEquals(braceConstant, result.getConstants().toArray()[0]);
	}

	@Test
	public void parseLiteral_customBracketLiteral_succeeds() throws ParsingException {
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.BRACKET, bracketHandler);
		Literal result = RuleParser.parseLiteral("p([test])", parserConfiguration);
		assertEquals(bracketConstant, result.getConstants().toArray()[0]);
	}

	@Test
	public void parseLiteral_mixedLiterals_succeeds() throws ParsingException {
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PIPE, pipeHandler)
				.registerLiteral(ConfigurableLiteralDelimiter.HASH, hashHandler)
				.registerLiteral(ConfigurableLiteralDelimiter.BRACKET, bracketHandler)
				.registerLiteral(ConfigurableLiteralDelimiter.BRACE, braceHandler);
		Literal result = RuleParser.parseLiteral("p(||, #test#, [], {})", parserConfiguration);
		List<Constant> constants = result.getConstants().collect(Collectors.toList());
		List<Constant> expected = new ArrayList<>(
				Arrays.asList(pipeConstant, hashConstant, bracketConstant, braceConstant));
		assertEquals(expected, constants);
	}

	@Test
	public void parseLiteral_nontrivialPipeLiteral_succeeds() throws ParsingException {
		String label = "this is a test, do not worry.";
		String input = "p(|" + label + "|)";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PIPE, reversingHandler);
		Literal result = RuleParser.parseLiteral(input, parserConfiguration);
		assertEquals(makeReversedConstant(label), result.getConstants().toArray()[0]);
	}

	@Test
	public void parseLiteral_nestedParenLiterals_succeeds() throws ParsingException {
		String label = "(((this is a test, do not worry.)))";
		String input = "p((" + label + "))";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PAREN, reversingHandler);
		Literal result = RuleParser.parseLiteral(input, parserConfiguration);
		assertEquals(makeReversedConstant(label), result.getConstants().toArray()[0]);
	}

	@Test
	public void parseLiteral_multipleParenLiterals_succeeds() throws ParsingException {
		String input = "p((test), (tset))";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PAREN, reversingHandler);
		Literal result = RuleParser.parseLiteral(input, parserConfiguration);
		List<Constant> constants = result.getConstants().collect(Collectors.toList());
		List<Constant> expected = new ArrayList<>(
				Arrays.asList(makeReversedConstant("test"), makeReversedConstant("tset")));
		assertEquals(expected, constants);
	}

	@Test
	public void parseLiteral_multipleNestedParenLiterals_succeeds() throws ParsingException {
		String input = "p(((test)), ((tset), (tst)))";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PAREN, reversingHandler);
		Literal result = RuleParser.parseLiteral(input, parserConfiguration);
		List<Constant> constants = result.getConstants().collect(Collectors.toList());
		List<Constant> expected = new ArrayList<>(
				Arrays.asList(makeReversedConstant("(test)"), makeReversedConstant("(tset), (tst)")));
		assertEquals(expected, constants);
	}

	@Test(expected = ParsingException.class)
	public void parseLiteral_mismatchedNestedParenLiteral_throws() throws ParsingException {
		String input = "p((test ())";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PAREN, reversingHandler);
		RuleParser.parseLiteral(input, parserConfiguration);
	}

	@Test
	public void parseLiteral_nestedBraceLiteral_succeeds() throws ParsingException {
		String label = "{{{this is a test, do not worry.}}}";
		String input = "p({" + label + "})";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.BRACE, reversingHandler);
		Literal result = RuleParser.parseLiteral(input, parserConfiguration);
		assertEquals(makeReversedConstant(label), result.getConstants().toArray()[0]);
	}

	@Test
	public void parseLiteral_multipleBraceLiterals_succeeds() throws ParsingException {
		String input = "p({test}, {tset})";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.BRACE, reversingHandler);
		Literal result = RuleParser.parseLiteral(input, parserConfiguration);
		List<Constant> constants = result.getConstants().collect(Collectors.toList());
		List<Constant> expected = new ArrayList<>(
				Arrays.asList(makeReversedConstant("test"), makeReversedConstant("tset")));
		assertEquals(expected, constants);
	}

	@Test
	public void parseLiteral_multipleNestedBraceLiterals_succeeds() throws ParsingException {
		String input = "p({{test}}, {{tset}})";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.BRACE, reversingHandler);
		Literal result = RuleParser.parseLiteral(input, parserConfiguration);
		List<Constant> constants = result.getConstants().collect(Collectors.toList());
		List<Constant> expected = new ArrayList<>(
				Arrays.asList(makeReversedConstant("{test}"), makeReversedConstant("{tset}")));
		assertEquals(expected, constants);
	}

	@Test
	public void parseLiteral_nestedBracketLiteral_succeeds() throws ParsingException {
		String label = "[[[this is a test, do not worry.]]]";
		String input = "p([" + label + "])";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.BRACKET, reversingHandler);
		Literal result = RuleParser.parseLiteral(input, parserConfiguration);
		assertEquals(makeReversedConstant(label), result.getConstants().toArray()[0]);
	}

	@Test
	public void parseLiteral_multipleBracketLiterals_succeeds() throws ParsingException {
		String input = "p([test], [tset])";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.BRACKET, reversingHandler);
		Literal result = RuleParser.parseLiteral(input, parserConfiguration);
		List<Constant> constants = result.getConstants().collect(Collectors.toList());
		List<Constant> expected = new ArrayList<>(
				Arrays.asList(makeReversedConstant("test"), makeReversedConstant("tset")));
		assertEquals(expected, constants);
	}

	@Test
	public void parseLiteral_multipleNestedBracketLiterals_succeeds() throws ParsingException {
		String input = "p([[test]], [[tset], [tst]])";
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.BRACKET, reversingHandler);
		Literal result = RuleParser.parseLiteral(input, parserConfiguration);
		List<Constant> constants = result.getConstants().collect(Collectors.toList());
		List<Constant> expected = new ArrayList<>(
				Arrays.asList(makeReversedConstant("[test]"), makeReversedConstant("[tset], [tst]")));
		assertEquals(expected, constants);
	}

	@Test
	public void parseLiteral_mixedAndNestedLiterals_succeeds() throws ParsingException {
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.registerLiteral(ConfigurableLiteralDelimiter.PIPE, pipeHandler)
				.registerLiteral(ConfigurableLiteralDelimiter.HASH, hashHandler)
				.registerLiteral(ConfigurableLiteralDelimiter.PAREN, parenHandler)
				.registerLiteral(ConfigurableLiteralDelimiter.BRACE, braceHandler)
				.registerLiteral(ConfigurableLiteralDelimiter.BRACKET, bracketHandler);
		Literal result = RuleParser.parseLiteral("p(|{}|, #test#, [|test, #test#, test|], ([], {}, [{[{}]}]))",
				parserConfiguration);
		List<Constant> constants = result.getConstants().collect(Collectors.toList());
		List<Constant> expected = new ArrayList<>(
				Arrays.asList(pipeConstant, hashConstant, bracketConstant, parenConstant));
		assertEquals(expected, constants);
	}

	static Constant makeReversedConstant(String name) {
		StringBuilder builder = new StringBuilder(name);
		return Expressions.makeAbstractConstant(builder.reverse().toString());
	}

	static ConfigurableLiteralHandler reversingHandler = (String syntacticForm,
			SubParserFactory subParserFactory) -> makeReversedConstant(syntacticForm);

	static ConfigurableLiteralHandler getMockLiteralHandler(ConfigurableLiteralDelimiter delimiter, Constant constant) {
		ConfigurableLiteralHandler handler = mock(ConfigurableLiteralHandler.class);
		try {
			doReturn(constant).when(handler).parseLiteral(ArgumentMatchers.anyString(),
					ArgumentMatchers.<SubParserFactory>any());
		} catch (ParsingException e) {
			// ignore it, since the mock will not throw
		}
		return handler;
	}

}
