package org.semanticweb.vlog4j.parser;

/*-
 * #%L
 * vlog4j-syntax
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

import org.semanticweb.vlog4j.core.exceptions.PrefixDeclarationException;
import org.semanticweb.vlog4j.core.model.api.PrefixDeclarationRegistry;
import org.semanticweb.vlog4j.core.model.implementation.AbstractPrefixDeclarationRegistry;

/**
 * Implementation of {@link PrefixDeclarationRegistry} that is used when parsing
 * data from a single source. In this case, attempts to re-declare prefixes or
 * the base IRI will lead to errors.
 *
 * @author Markus Kroetzsch
 *
 */
final public class LocalPrefixDeclarationRegistry extends AbstractPrefixDeclarationRegistry {

	/**
	 * Fallback IRI to use as base IRI if none is set.
	 */
	private String fallbackIri;

	public LocalPrefixDeclarationRegistry() {
		this(PrefixDeclarationRegistry.EMPTY_BASE); // empty string encodes: "no base" (use relative IRIs)
	}

	/**
	 *
	 */
	public LocalPrefixDeclarationRegistry(String fallbackIri) {
		super();
		this.fallbackIri = fallbackIri;
		this.baseUri = null;
	}

	/**
	 * Returns the relevant base namespace. Returns the fallback IRI if no base
	 * namespace has been set yet.
	 *
	 * @return string of an absolute base IRI
	 */
	@Override
	public String getBaseIri() {
		if (this.baseUri == null) {
			this.baseUri = this.fallbackIri;
		}
		return baseUri.toString();
	}

	@Override
	public void setPrefixIri(String prefixName, String prefixIri) throws PrefixDeclarationException {
		if (prefixes.containsKey(prefixName)) {
			throw new PrefixDeclarationException("Prefix \"" + prefixName + "\" is already defined as <"
					+ prefixes.get(prefixName) + ">. It cannot be redefined to mean <" + prefixIri + ">.");
		}

		prefixes.put(prefixName, prefixIri);
	}

	/**
	 * Sets the base namespace to the given value. This should only be done once,
	 * and not after the base namespace was assumed to be an implicit default value.
	 *
	 * @param baseUri the new base namespace
	 * @throws PrefixDeclarationException if base was already defined
	 */

	@Override
	public void setBaseIri(String baseUri) throws PrefixDeclarationException {
		if (this.baseUri != null)
			throw new PrefixDeclarationException(
					"Base is already defined as <" + this.baseUri + "> and cannot be re-defined as " + baseUri);
		this.baseUri = baseUri;
	}
}
