package org.semanticweb.vlog4j.parser.directives;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.semanticweb.vlog4j.core.model.api.PrefixDeclarations;
import org.semanticweb.vlog4j.core.reasoner.KnowledgeBase;
import org.semanticweb.vlog4j.parser.DirectiveArgument;
import org.semanticweb.vlog4j.parser.DirectiveHandler;
import org.semanticweb.vlog4j.parser.ParserConfiguration;
import org.semanticweb.vlog4j.parser.ParsingException;
import org.semanticweb.vlog4j.parser.RuleParser;
import org.semanticweb.vlog4j.parser.javacc.SubParserFactory;

/**
 * Handler for parsing {@code @import-relative} statements.
 *
 * @author Maximilian Marx
 */
public class ImportFileRelativeDirectiveHandler implements DirectiveHandler<KnowledgeBase> {
	@Override
	public KnowledgeBase handleDirective(List<DirectiveArgument> arguments, final SubParserFactory subParserFactory)
			throws ParsingException {
		DirectiveHandler.validateNumberOfArguments(arguments, 1);
		PrefixDeclarations prefixDeclarations = getPrefixDeclarations(subParserFactory);
		File file = DirectiveHandler.validateFilenameArgument(arguments.get(0), "rules file");
		KnowledgeBase knowledgeBase = getKnowledgeBase(subParserFactory);
		ParserConfiguration parserConfiguration = getParserConfiguration(subParserFactory);

		try {
			return knowledgeBase.importRulesFile(file, (InputStream stream, KnowledgeBase kb) -> {
				try {
					RuleParser.parseInto(kb, stream, parserConfiguration, prefixDeclarations.getBase());
				} catch (ParsingException e) {
					throw new RuntimeException(e);
				}
				return kb;
			});
		} catch (RuntimeException | IOException e) {
			throw new ParsingException("Failed while trying to import rules file \"" + file.getName() + "\"", e);
		}
	}
}
