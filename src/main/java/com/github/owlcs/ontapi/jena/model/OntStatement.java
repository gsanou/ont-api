/*
 * This file is part of the ONT API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2019, The University of Manchester, owl.cs group.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.github.owlcs.ontapi.jena.model;

import com.github.owlcs.ontapi.jena.OntJenaException;
import com.github.owlcs.ontapi.jena.utils.Models;
import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Ontology RDF {@link Statement Statement}.
 * <p>
 * This is <b>not</b> a {@link org.apache.jena.rdf.model.Resource}.
 * This is an extended {@link Statement Jena Model Statement} with possibility to add, delete and find annotations
 * in the same form of {@code OntStatement} or {@link OntAnnotation Ontology Annotation} resources.
 * <p>
 * Created by @szuev on 13.11.2016.
 *
 * @see OntAnnotation
 * @see Statement
 * @see <a href='https://www.w3.org/TR/owl2-mapping-to-rdf/#Translation_of_Annotations'>2.2 Translation of Annotations</a>
 */
public interface OntStatement extends Statement {

    /**
     * Gets the {@link OntModel Ontology RDF Model} this {@link OntStatement Statement} was created in.
     *
     * @return {@link OntModel}
     */
    @Override
    OntModel getModel();

    /**
     * Annotates the statement with the given {@link OntAnnotationProperty annotation property} and {@link RDFNode RDF Node} value
     * and returns a newly added annotation assertion statement.
     * <p>
     * In the special case of a main statement (i.e. if this statement is a result of {@link OntObject#getMainStatement()})
     * the returned {@code OntStatement} has the same subject as this statement
     * and it is called a plain annotation assertion
     * (in this case the method is equivalent to the {@link OntObject#addAnnotation(OntAnnotationProperty, RDFNode)} method).
     * Otherwise the returned statement is a part of a fresh or existing {@link OntAnnotation bulk annotation resource}
     * and its subject is a blank node.
     *
     * @param property {@link OntAnnotationProperty} named annotation property, not {@code null}
     * @param value    {@link RDFNode} uri-resource, literal or anonymous individual, not {@code null}
     * @return a <b>new</b> {@link OntStatement Ont-Statement} for newly added annotation
     * @throws OntJenaException in case input is incorrect
     * @see #annotate(OntAnnotationProperty, RDFNode)
     * @see OntAnnotation#addAnnotation(OntAnnotationProperty, RDFNode)
     * @see OntObject#addAnnotation(OntAnnotationProperty, RDFNode)
     * @see OntObject#getMainStatement()
     */
    OntStatement addAnnotation(OntAnnotationProperty property, RDFNode value);

    /**
     * Lists all annotations related to this statement.
     * The returned stream consists of annotation assertions listed from the top-level bulk annotations
     * plus plain annotation assertions in the special case of main statement.
     *
     * @return Stream (unordered) of {@link OntStatement annotation assertion statement}s
     * with {@link OntAnnotationProperty annotation property} as predicates, can be empty
     * @see #asAnnotationResource()
     */
    Stream<OntStatement> annotations();

    /**
     * Deletes the child annotation if present.
     * Does nothing if no assertion found.
     * Throws an exception if specified annotation has it is own annotations.
     * If this statement is not root and corresponding {@link OntAnnotation} resource has no assertions any more,
     * it deletes the whole OntAnnotation resource also.
     *
     * @param property {@link OntAnnotationProperty} named annotation property, not {@code null}
     * @param value    {@link RDFNode} uri-resource, literal or anonymous individual, not {@code null}
     * @return <b>this</b> statement instance to allow cascading calls
     * @throws OntJenaException in case input is incorrect or deleted annotation has it is own annotations
     * @see #deleteAnnotation(OntAnnotationProperty)
     * @see #clearAnnotations()
     */
    OntStatement deleteAnnotation(OntAnnotationProperty property, RDFNode value) throws OntJenaException;

    /**
     * Returns the stream of the annotation objects attached to this statement.
     * E.g. for the statement {@code s A t} the annotation object looks like
     * <pre>{@code
     * _:b0 a owl:Axiom .
     * _:b0 Aj tj .
     * _:b0 owl:annotatedSource s .
     * _:b0 owl:annotatedProperty A .
     * _:b0 owl:annotatedTarget t .
     * }</pre>
     * Technically, although it usually does not make sense,
     * it is allowed that a statement may have several such b-nodes.
     *
     * @return Stream (unordered) of {@link OntAnnotation} resources
     * @see #asAnnotationResource() to get first annotation-object
     * @see #getAnnotationList() to get all annotation-objects in a fixed order
     */
    Stream<OntAnnotation> annotationResources();

    /**
     * An accessor method to return the subject of the statements in form of {@link OntObject Ontology Object}.
     *
     * @return {@link OntObject}
     * @see Statement#getSubject()
     */
    @Override
    OntObject getSubject();

    /**
     * Answers {@code true} iff this statement is in the base graph.
     * The method is equivalent to the expression {@code this.getModel().getBaseGraph().contains(this.asTriple())}.
     *
     * @return {@code true} if it is local statement
     * @see OntResource#isLocal()
     */
    default boolean isLocal() {
        return getModel().getBaseGraph().contains(asTriple());
    }

    /**
     * Returns the annotation objects attached to this statement
     * in the form of {@link List} with a fixed order.
     *
     * @return {@link List} of {@link OntAnnotation Ontology Annotation}s
     * @see #annotationResources()
     * @since 1.3.0
     */
    default List<OntAnnotation> getAnnotationList() {
        return annotationResources().sorted(Models.RDF_NODE_COMPARATOR).collect(Collectors.toList());
    }

    /**
     * Returns the primary annotation object (resource) which is related to this statement.
     * It is assumed that this method always returns the same result if no changes in graph is made,
     * even after graph reloading.
     *
     * @return {@code Optional} around of {@link OntAnnotation}, can be empty
     * @see #getAnnotationList()
     */
    default Optional<OntAnnotation> asAnnotationResource() {
        List<OntAnnotation> res = this.getAnnotationList();
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    /**
     * Lists all annotations by the property.
     *
     * @param property {@link OntAnnotationProperty} the property
     * @return Stream of {@link OntStatement}s
     */
    default Stream<OntStatement> annotations(OntAnnotationProperty property) {
        return annotations().filter(s -> Objects.equals(property, s.getPredicate()));
    }

    /**
     * Deletes all (sub-)annotations with the given predicate-property.
     *
     * @param property {@link OntAnnotationProperty}
     * @return <b>this</b> statement to allow cascading calls
     * @see #clearAnnotations()
     * @see #deleteAnnotation(OntAnnotationProperty, RDFNode)
     */
    default OntStatement deleteAnnotation(OntAnnotationProperty property) {
        annotations(property).map(Statement::getObject)
                .collect(Collectors.toSet())
                .forEach(v -> deleteAnnotation(property, v));
        return this;
    }

    /**
     * Answers {@code true} iff this statement is a declaration: {@code @any rdf:type @any}.
     *
     * @return {@code true} if the predicate is {@code rdf:type}
     */
    default boolean isDeclaration() {
        return RDF.type.equals(getPredicate());
    }

    /**
     * Answers {@code true} iff this statement is a part of {@link OntAnnotation Bulk Annotation Ontology Object}.
     * This means that it is one of the following:
     * <pre>{@code
     * _:x rdf:type              owl:Annotation .
     * _:x p                     v .
     * _:x owl:annotatedSource   this .
     * _:x owl:annotatedProperty x .
     * _:x owl:annotatedTarget   y .
     * }</pre>
     *
     * @return {@code true} if it is a part of bulk annotation object
     * @see OntAnnotation
     */
    default boolean belongsToAnnotation() {
        return getSubject().canAs(OntAnnotation.class);
    }

    /**
     * Answers {@code true} iff this is an annotation assertion.
     * Annotation assertion is a statement {@code s A t}, where
     * {@code s} is an IRI or anonymous individual,
     * {@code t} is an IRI, anonymous individual, or literal,
     * and {@code A} is an annotation property.
     *
     * @return {@code true} if the predicate is {@link OntAnnotationProperty}
     */
    default boolean isAnnotationAssertion() {
        return getPredicate().canAs(OntAnnotationProperty.class);
    }

    /**
     * Removes all sub-annotations including their children.
     *
     * @return <b>this</b> statement to allow cascading calls
     * @see OntStatement#deleteAnnotation(OntAnnotationProperty, RDFNode)
     * @see OntObject#clearAnnotations()
     */
    default OntStatement clearAnnotations() {
        annotations()
                .peek(OntStatement::clearAnnotations)
                .collect(Collectors.toSet())
                .forEach(a -> deleteAnnotation(a.getPredicate().as(OntAnnotationProperty.class), a.getObject()));
        return this;
    }

    /**
     * Answers {@code true} iff this statement has any annotations attached (either plain or bulk).
     *
     * @return {@code true} if it is annotated
     */
    default boolean hasAnnotations() {
        try (Stream<OntStatement> annotations = annotations()) {
            return annotations.findFirst().isPresent();
        }
    }

    /**
     * Creates and returns a textual no-lang sub-annotation-assertion.
     *
     * @param predicate {@link OntAnnotationProperty}, not {@code null}
     * @param text      String, the text message, not {@code null}
     * @return {@link OntStatement}, <b>new</b> instance
     * @see OntObject#addAnnotation(OntAnnotationProperty, String, String)
     */
    default OntStatement addAnnotation(OntAnnotationProperty predicate, String text) {
        return addAnnotation(predicate, text, null);
    }

    /**
     * Creates and returns a textual language-tagged sub-annotation-assertion.
     *
     * @param predicate {@link OntAnnotationProperty}, not {@code null}
     * @param text      String, the text message, not {@code null}
     * @param lang      String, language, optional
     * @return {@link OntStatement}, <b>new</b> instance
     * @see OntObject#addAnnotation(OntAnnotationProperty, String, String)
     */
    default OntStatement addAnnotation(OntAnnotationProperty predicate, String text, String lang) {
        return addAnnotation(predicate, getModel().createLiteral(text, lang));
    }

    /**
     * Annotates the statement with the given predicate and textual message.
     *
     * @param property {@link OntAnnotationProperty} named annotation property, not {@code null}
     * @param text     String, the text message, not {@code null}
     * @return <b>this</b> {@code OntStatement} to allow cascading calls
     * @see OntStatement#addAnnotation(OntAnnotationProperty, String)
     * @see OntModel#getRDFSComment()
     * @see OntModel#getRDFSLabel()
     * @since 1.4.0
     */
    default OntStatement annotate(OntAnnotationProperty property, String text) {
        return annotate(property, text, null);
    }

    /**
     * Annotates the statement with the given predicate and language-tagged textual message.
     *
     * @param property {@link OntAnnotationProperty} named annotation property, not {@code null}
     * @param text     String, the text message, not {@code null}
     * @param lang     String, language, optional
     * @return <b>this</b> {@code OntStatement} to allow cascading calls
     * @see OntStatement#addAnnotation(OntAnnotationProperty, String, String)
     * @see OntModel#getRDFSComment()
     * @see OntModel#getRDFSLabel()
     * @since 1.4.0
     */
    default OntStatement annotate(OntAnnotationProperty property, String text, String lang) {
        return annotate(property, getModel().createLiteral(text, lang));
    }

    /**
     * Annotates the statement with the given predicate and value.
     * The method differs from {@link #addAnnotation(OntAnnotationProperty, RDFNode)} only in return object.
     *
     * @param property {@link OntAnnotationProperty} named annotation property, not {@code null}
     * @param value    {@link RDFNode} uri-resource, literal or anonymous individual, not {@code null}
     * @return <b>this</b> {@code OntStatement} to allow cascading calls
     * @see OntStatement#addAnnotation(OntAnnotationProperty, RDFNode)
     * @see OntModel#getRDFSComment()
     * @see OntModel#getRDFSLabel()
     * @since 1.4.0
     */
    default OntStatement annotate(OntAnnotationProperty property, RDFNode value) {
        addAnnotation(property, value);
        return this;
    }

    /**
     * Answers a typed subject of the statement.
     *
     * @param type Class type
     * @param <S>  subtype of {@link Resource}
     * @return {@link Resource} instance
     * @throws org.apache.jena.enhanced.UnsupportedPolymorphismException if the subject node
     *                                                                   and the given type are incompatible
     * @see #getSubject()
     * @since 1.3.0
     */
    default <S extends Resource> S getSubject(Class<S> type) {
        return getSubject().as(type);
    }

    /**
     * Answers a typed object of the statement.
     *
     * @param type Class type
     * @param <O>  subtype of {@link RDFNode}
     * @return {@link RDFNode} instance
     * @throws org.apache.jena.enhanced.UnsupportedPolymorphismException if the object node
     *                                                                   and the given type are incompatible
     * @see #getObject()
     * @since 1.4.0
     */
    default <O extends RDFNode> O getObject(Class<O> type) {
        return getObject().as(type);
    }
}
