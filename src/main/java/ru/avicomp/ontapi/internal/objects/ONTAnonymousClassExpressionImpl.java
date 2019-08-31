/*
 * This file is part of the ONT API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2019, Avicomp Services, AO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package ru.avicomp.ontapi.internal.objects;

import org.apache.jena.graph.BlankNodeId;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.NNF;
import ru.avicomp.ontapi.DataFactory;
import ru.avicomp.ontapi.OntApiException;
import ru.avicomp.ontapi.internal.InternalObjectFactory;
import ru.avicomp.ontapi.internal.ONTObject;
import ru.avicomp.ontapi.jena.model.*;
import ru.avicomp.ontapi.jena.utils.OntModels;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An abstraction for any anonymous class expressions
 * (i.e. for all class expressions with except of {@code OWLClass}es).
 * <p>
 * Created by @ssz on 10.08.2019.
 *
 * @param <ONT> any subtype of {@link OntCE}
 * @param <OWL> any subtype of {@link OWLAnonymousClassExpression}
 * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLAnonymousClassExpressionImpl
 * @see ONTClassImpl
 * @see ru.avicomp.ontapi.internal.ReadHelper#calcClassExpression(OntCE, InternalObjectFactory, Set)
 * @see OntCE
 * @see OntClass
 * @since 1.4.3
 */
@SuppressWarnings("WeakerAccess")
public abstract class ONTAnonymousClassExpressionImpl<ONT extends OntCE, OWL extends OWLAnonymousClassExpression>
        extends ONTExpressionImpl<ONT>
        implements OWLAnonymousClassExpression, ONTObject<OWL> {

    protected ONTAnonymousClassExpressionImpl(BlankNodeId n, Supplier<OntGraphModel> m) {
        super(n, m);
    }

    /**
     * Wraps the given {@link OntCE} as {@link OWLAnonymousClassExpression} and {@link ONTObject}.
     *
     * @param ce    {@link OntCE}, not {@code null}, must be anonymous
     * @param model a provider of non-null {@link OntGraphModel}, not {@code null}
     * @return {@link ONTAnonymousClassExpressionImpl} instance
     */
    @SuppressWarnings("unchecked")
    public static ONTAnonymousClassExpressionImpl create(OntCE ce, Supplier<OntGraphModel> model) {
        Class<? extends OntCE> type = OntModels.getOntType(ce);
        BlankNodeId id = ce.asNode().getBlankNodeId();
        ONTAnonymousClassExpressionImpl res = create(id, type, model);
        // since we have already type information
        // we can forcibly load the cache to reduce graph traversal operations
        // (otherwise this type information will be collected again on demand, which means double-work):
        res.content.put(res, res.collectContent(ce, res.getObjectFactory()));
        return res;
    }

    /**
     * Creates a class expression with given b-node {@code id} and {@code type}.
     * An underlying graph must contain a valid class expression structure for the specified {@code id}.
     *
     * @param id    {@link BlankNodeId}, not {@code null}
     * @param type  {@code Class}, not {@code null}
     * @param model a provider of non-null {@link OntGraphModel}, not {@code null}
     * @return {@link ONTAnonymousClassExpressionImpl} instance
     */
    public static ONTAnonymousClassExpressionImpl create(BlankNodeId id,
                                                         Class<? extends OntCE> type,
                                                         Supplier<OntGraphModel> model) {
        if (OntCE.ObjectSomeValuesFrom.class == type) {
            return new OSVF(id, model);
        }
        if (OntCE.DataSomeValuesFrom.class == type) {
            return new DSVF(id, model);
        }
        if (OntCE.ObjectAllValuesFrom.class == type) {
            return new OAVF(id, model);
        }
        if (OntCE.DataAllValuesFrom.class == type) {
            return new DAVF(id, model);
        }
        if (OntCE.ObjectHasValue.class == type) {
            return new OHV(id, model);
        }
        if (OntCE.DataHasValue.class == type) {
            return new DHV(id, model);
        }
        if (OntCE.HasSelf.class == type) {
            return new OHS(id, model);
        }
        if (OntCE.ObjectCardinality.class == type) {
            return new OEC(id, model);
        }
        if (OntCE.DataCardinality.class == type) {
            return new DEC(id, model);
        }
        if (OntCE.ObjectMinCardinality.class == type) {
            return new OMIC(id, model);
        }
        if (OntCE.DataMinCardinality.class == type) {
            return new DMIC(id, model);
        }
        if (OntCE.ObjectMaxCardinality.class == type) {
            return new OMAC(id, model);
        }
        if (OntCE.DataMaxCardinality.class == type) {
            return new DMAC(id, model);
        }
        if (OntCE.UnionOf.class == type) {
            return new UF(id, model);
        }
        if (OntCE.IntersectionOf.class == type) {
            return new IF(id, model);
        }
        if (OntCE.OneOf.class == type) {
            return new OF(id, model);
        }
        if (OntCE.ComplementOf.class == type) {
            return new CF(id, model);
        }
        if (OntCE.NaryDataSomeValuesFrom.class == type) {
            return new NDSVF(id, model);
        }
        if (OntCE.NaryDataAllValuesFrom.class == type) {
            return new NDAVF(id, model);
        }
        throw new OntApiException.IllegalState();
    }

    @SuppressWarnings("unchecked")
    @Override
    public OWL getOWLObject() {
        return (OWL) this;
    }

    @Override
    public boolean isOWLThing() {
        return false;
    }

    @Override
    public boolean isOWLNothing() {
        return false;
    }

    @Override
    public boolean isClassExpression() {
        return true;
    }

    @Override
    public OWLClassExpression getNNF() {
        return accept(getNNFClassVisitor());
    }

    @Override
    public OWLClassExpression getComplementNNF() {
        return getObjectComplementOf().accept(getNNFClassVisitor());
    }

    protected OWLClassExpressionVisitorEx<OWLClassExpression> getNNFClassVisitor() {
        return new NNF(getDataFactory()).getClassVisitor();
    }

    @Override
    public OWLObjectComplementOf getObjectComplementOf() {
        return getDataFactory().getOWLObjectComplementOf(this);
    }

    @Override
    public Set<OWLClassExpression> asConjunctSet() {
        return createSet(this);
    }

    @Override
    public Stream<OWLClassExpression> conjunctSet() {
        return Stream.of(this);
    }

    @Override
    public boolean containsConjunct(@Nullable OWLClassExpression ce) {
        return equals(ce);
    }

    @Override
    public Set<OWLClassExpression> asDisjunctSet() {
        return createSet(this);
    }

    @Override
    public Stream<OWLClassExpression> disjunctSet() {
        return Stream.of(this);
    }

    @Override
    public boolean canContainAnnotationProperties() {
        return false;
    }

    @Override
    public boolean containsEntityInSignature(@Nullable OWLEntity entity) {
        if (entity == null || entity.isOWLAnnotationProperty()) return false;
        return super.containsEntityInSignature(entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<ONTObject<? extends OWLObject>> objects() {
        List res = Arrays.asList(getContent());
        return (Stream<ONTObject<? extends OWLObject>>) res.stream();
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectSomeValuesFromImpl
     * @see OntCE.ObjectSomeValuesFrom
     */
    public static class OSVF
            extends WithClassAndObjectProperty<OntCE.ObjectSomeValuesFrom, OWLObjectSomeValuesFrom>
            implements OWLObjectSomeValuesFrom {

        public OSVF(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.ObjectSomeValuesFrom asRDFNode() {
            return as(OntCE.ObjectSomeValuesFrom.class);
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectAllValuesFromImpl
     * @see OntCE.ObjectAllValuesFrom
     */
    public static class OAVF
            extends WithClassAndObjectProperty<OntCE.ObjectAllValuesFrom, OWLObjectAllValuesFrom>
            implements OWLObjectAllValuesFrom {

        public OAVF(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.ObjectAllValuesFrom asRDFNode() {
            return as(OntCE.ObjectAllValuesFrom.class);
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLDataSomeValuesFromImpl
     * @see OntCE.DataSomeValuesFrom
     */
    public static class DSVF
            extends WithDataRangeAndDataProperty<OntCE.DataSomeValuesFrom, OWLDataSomeValuesFrom>
            implements OWLDataSomeValuesFrom {

        public DSVF(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.DataSomeValuesFrom asRDFNode() {
            return as(OntCE.DataSomeValuesFrom.class);
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLDataAllValuesFromImpl
     * @see OntCE.DataAllValuesFrom
     */
    public static class DAVF
            extends WithDataRangeAndDataProperty<OntCE.DataAllValuesFrom, OWLDataAllValuesFrom>
            implements OWLDataAllValuesFrom {

        public DAVF(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.DataAllValuesFrom asRDFNode() {
            return as(OntCE.DataAllValuesFrom.class);
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLDataSomeValuesFromImpl
     * @see OntCE.NaryDataSomeValuesFrom
     */
    public static class NDSVF
            extends WithDataProperty<OntCE.NaryDataSomeValuesFrom, OWLDataSomeValuesFrom>
            implements OWLDataSomeValuesFrom {

        public NDSVF(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.NaryDataSomeValuesFrom asRDFNode() {
            return as(OntCE.NaryDataSomeValuesFrom.class);
        }

        @Override
        public OWLDataRange getFiller() {
            return getONTDataRange().getOWLObject();
        }

        @SuppressWarnings("unchecked")
        public ONTObject<? extends OWLDataRange> getONTDataRange() {
            // [property, filler]
            return (ONTObject<? extends OWLDataRange>) getContent()[1];
        }

        @Override
        protected Object[] collectContent(OntCE.NaryDataSomeValuesFrom ce, InternalObjectFactory of) {
            // [property, filler]
            return new Object[]{of.getProperty(ce.getProperty()), of.getDatatype(ce.getValue())};
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLDataAllValuesFromImpl
     * @see OntCE.NaryDataAllValuesFrom
     */
    public static class NDAVF
            extends WithDataProperty<OntCE.NaryDataAllValuesFrom, OWLDataAllValuesFrom>
            implements OWLDataAllValuesFrom {

        public NDAVF(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.NaryDataAllValuesFrom asRDFNode() {
            return as(OntCE.NaryDataAllValuesFrom.class);
        }

        @Override
        public OWLDataRange getFiller() {
            return getONTClassExpression().getOWLObject();
        }

        @SuppressWarnings("unchecked")
        public ONTObject<? extends OWLDataRange> getONTClassExpression() {
            // [property, filler]
            return (ONTObject<? extends OWLDataRange>) getContent()[1];
        }

        @Override
        protected Object[] collectContent(OntCE.NaryDataAllValuesFrom ce, InternalObjectFactory of) {
            // [property, filler]
            return new Object[]{of.getProperty(ce.getProperty()), of.getDatatype(ce.getValue())};
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectHasValueImpl
     * @see OntCE.ObjectHasValue
     */
    public static class OHV
            extends WithObjectProperty<OntCE.ObjectHasValue, OWLObjectHasValue>
            implements OWLObjectHasValue {

        protected OHV(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.ObjectHasValue asRDFNode() {
            return as(OntCE.ObjectHasValue.class);
        }

        @Override
        public OWLObjectSomeValuesFrom asSomeValuesFrom() {
            DataFactory df = getDataFactory();
            return df.getOWLObjectSomeValuesFrom(getProperty(), df.getOWLObjectOneOf(getFiller()));
        }

        @Override
        public OWLIndividual getFiller() {
            return getONTIndividual().getOWLObject();
        }

        @SuppressWarnings("unchecked")
        public ONTObject<? extends OWLIndividual> getONTIndividual() {
            // [property, filler]
            return (ONTObject<? extends OWLIndividual>) getContent()[1];
        }

        @Override
        protected Object[] collectContent(OntCE.ObjectHasValue ce, InternalObjectFactory of) {
            // [property, filler]
            return new Object[]{of.getProperty(ce.getProperty()), of.getIndividual(ce.getValue())};
        }

        @Override
        public boolean containsEntityInSignature(OWLEntity entity) {
            if (entity == null) return false;
            if (entity.isOWLObjectProperty())
                return getNamedProperty().equals(entity);
            OWLIndividual i;
            if (entity.isIndividual() && (i = getFiller()).isOWLNamedIndividual()) {
                return entity.equals(i);
            }
            return false;
        }

        @Override
        public Set<OWLEntity> getSignatureSet() {
            Set<OWLEntity> res = createSortedSet();
            res.add(getNamedProperty());
            OWLIndividual i = getFiller();
            if (i.isOWLNamedIndividual()) res.add(i.asOWLNamedIndividual());
            return res;
        }

        @Override
        public Set<OWLObjectProperty> getObjectPropertySet() {
            return createSet(getNamedProperty());
        }

        protected OWLObjectProperty getNamedProperty() {
            return getProperty().getNamedProperty();
        }

        @Override
        public Set<OWLNamedIndividual> getNamedIndividualSet() {
            OWLIndividual i = getFiller();
            return i.isOWLNamedIndividual() ? createSet(i.asOWLNamedIndividual()) : createSet();
        }

        @Override
        public Set<OWLAnonymousIndividual> getAnonymousIndividualSet() {
            OWLIndividual i = getFiller();
            return i.isOWLNamedIndividual() ? createSet() : createSet(i.asOWLAnonymousIndividual());
        }

        @Override
        public Set<OWLClassExpression> getClassExpressionSet() {
            return createSet(this);
        }

        @Override
        public boolean canContainDataProperties() {
            return false;
        }

        @Override
        public boolean canContainNamedClasses() {
            return false;
        }

        @Override
        public boolean canContainDatatypes() {
            return false;
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLDataHasValueImpl
     * @see OntCE.DataHasValue
     */
    public static class DHV
            extends WithDataProperty<OntCE.DataHasValue, OWLDataHasValue>
            implements OWLDataHasValue {

        protected DHV(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.DataHasValue asRDFNode() {
            return as(OntCE.DataHasValue.class);
        }

        @Override
        public OWLDataSomeValuesFrom asSomeValuesFrom() {
            DataFactory df = getDataFactory();
            return df.getOWLDataSomeValuesFrom(getProperty(), df.getOWLDataOneOf(getFiller()));
        }

        @Override
        public OWLLiteral getFiller() {
            return getONTLiteral().getOWLObject();
        }

        @SuppressWarnings("unchecked")
        public ONTObject<OWLLiteral> getONTLiteral() {
            // [property, filler]
            return (ONTObject<OWLLiteral>) getContent()[1];
        }

        @Override
        protected Object[] collectContent(OntCE.DataHasValue ce, InternalObjectFactory of) {
            // [property, filler]
            return new Object[]{of.getProperty(ce.getProperty()), of.getLiteral(ce.getValue())};
        }
    }

    /**
     * An {@code ObjectHasSelf} implementation.
     * It does not contain boolean datatype in signature -
     * see <a href='https://github.com/owlcs/owlapi/issues/783'>owlcs/owlapi##783</a>.
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectHasSelfImpl
     * @see OntCE.HasSelf
     */
    public static class OHS
            extends WithObjectProperty<OntCE.HasSelf, OWLObjectHasSelf>
            implements OWLObjectHasSelf {

        protected OHS(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.HasSelf asRDFNode() {
            return as(OntCE.HasSelf.class);
        }

        @Override
        public boolean containsEntityInSignature(OWLEntity entity) {
            if (entity == null || !entity.isOWLObjectProperty()) return false;
            return getNamedProperty().equals(entity);
        }

        @Override
        public Set<OWLEntity> getSignatureSet() {
            return createSet(getNamedProperty());
        }

        @Override
        public Set<OWLObjectProperty> getObjectPropertySet() {
            return createSet(getNamedProperty());
        }

        protected OWLObjectProperty getNamedProperty() {
            return getProperty().getNamedProperty();
        }

        @Override
        public boolean canContainNamedIndividuals() {
            return false;
        }

        @Override
        public boolean canContainAnonymousIndividuals() {
            return false;
        }

        @Override
        public Set<OWLClassExpression> getClassExpressionSet() {
            return createSet(this);
        }

        @Override
        public boolean canContainDataProperties() {
            return false;
        }

        @Override
        public boolean canContainNamedClasses() {
            return false;
        }

        @Override
        public boolean canContainDatatypes() {
            return false;
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectExactCardinalityImpl
     * @see OntCE.ObjectCardinality
     */
    public static class OEC
            extends WithClassAndObjectPropertyAndCardinality<OntCE.ObjectCardinality, OWLObjectExactCardinality>
            implements OWLObjectExactCardinality {

        public OEC(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.ObjectCardinality asRDFNode() {
            return as(OntCE.ObjectCardinality.class);
        }

        @Override
        public OWLObjectIntersectionOf asIntersectionOfMinMax() {
            DataFactory df = getDataFactory();
            OWLObjectPropertyExpression p = getProperty();
            OWLClassExpression c = getFiller();
            int q = getCardinality();
            return df.getOWLObjectIntersectionOf(df.getOWLObjectMinCardinality(q, p, c),
                    df.getOWLObjectMaxCardinality(q, p, c));
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLDataExactCardinalityImpl
     * @see OntCE.DataCardinality
     */
    public static class DEC
            extends WithDataRangeAndDataPropertyAndCardinality<OntCE.DataCardinality, OWLDataExactCardinality>
            implements OWLDataExactCardinality {

        public DEC(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.DataCardinality asRDFNode() {
            return as(OntCE.DataCardinality.class);
        }

        @Override
        public OWLObjectIntersectionOf asIntersectionOfMinMax() {
            DataFactory df = getDataFactory();
            OWLDataProperty p = getProperty();
            OWLDataRange c = getFiller();
            int q = getCardinality();
            return df.getOWLObjectIntersectionOf(df.getOWLDataMinCardinality(q, p, c),
                    df.getOWLDataMaxCardinality(q, p, c));
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectMinCardinalityImpl
     * @see OntCE.ObjectMinCardinality
     */
    public static class OMIC
            extends WithClassAndObjectPropertyAndCardinality<OntCE.ObjectMinCardinality, OWLObjectMinCardinality>
            implements OWLObjectMinCardinality {

        public OMIC(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.ObjectMinCardinality asRDFNode() {
            return as(OntCE.ObjectMinCardinality.class);
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLDataMinCardinalityImpl
     * @see OntCE.DataMinCardinality
     */
    public static class DMIC
            extends WithDataRangeAndDataPropertyAndCardinality<OntCE.DataMinCardinality, OWLDataMinCardinality>
            implements OWLDataMinCardinality {

        public DMIC(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.DataMinCardinality asRDFNode() {
            return as(OntCE.DataMinCardinality.class);
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectMaxCardinalityImpl
     * @see OntCE.ObjectMaxCardinality
     */
    public static class OMAC
            extends WithClassAndObjectPropertyAndCardinality<OntCE.ObjectMaxCardinality, OWLObjectMaxCardinality>
            implements OWLObjectMaxCardinality {

        public OMAC(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.ObjectMaxCardinality asRDFNode() {
            return as(OntCE.ObjectMaxCardinality.class);
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLDataMaxCardinalityImpl
     * @see OntCE.DataMaxCardinality
     */
    public static class DMAC
            extends WithDataRangeAndDataPropertyAndCardinality<OntCE.DataMaxCardinality, OWLDataMaxCardinality>
            implements OWLDataMaxCardinality {

        public DMAC(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.DataMaxCardinality asRDFNode() {
            return as(OntCE.DataMaxCardinality.class);
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectUnionOfImpl
     * @see OntCE.UnionOf
     */
    public static class UF
            extends WithClassMembers<OntCE.UnionOf, OWLObjectUnionOf>
            implements OWLObjectUnionOf {

        public UF(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.UnionOf asRDFNode() {
            return as(OntCE.UnionOf.class);
        }

        @Override
        public Set<OWLClassExpression> asDisjunctSet() {
            Set<OWLClassExpression> res = createSortedSet();
            getONTMembers().forEach(x -> res.addAll(x.getOWLObject().asDisjunctSet()));
            return res;
        }

        @Override
        public Stream<OWLClassExpression> disjunctSet() {
            return asDisjunctSet().stream();
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectIntersectionOfImpl
     * @see OntCE.IntersectionOf
     */
    public static class IF
            extends WithClassMembers<OntCE.IntersectionOf, OWLObjectIntersectionOf>
            implements OWLObjectIntersectionOf {

        public IF(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.IntersectionOf asRDFNode() {
            return as(OntCE.IntersectionOf.class);
        }

        @Override
        public Set<OWLClassExpression> asConjunctSet() {
            Set<OWLClassExpression> res = createSortedSet();
            getONTMembers().forEach(x -> res.addAll(x.getOWLObject().asConjunctSet()));
            return res;
        }

        @Override
        public Stream<OWLClassExpression> conjunctSet() {
            return asConjunctSet().stream();
        }

        @Override
        public boolean containsConjunct(@Nullable OWLClassExpression ce) {
            return asConjunctSet().contains(ce);
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectOneOfImpl
     * @see OntCE.OneOf
     */
    public static class OF
            extends WithMembers<OntIndividual, OntCE.OneOf, OWLIndividual, OWLObjectOneOf>
            implements OWLObjectOneOf {

        public OF(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        protected ONTObject<? extends OWLIndividual> map(OntIndividual i, InternalObjectFactory of) {
            return of.getIndividual(i);
        }

        @Override
        public OntCE.OneOf asRDFNode() {
            return as(OntCE.OneOf.class);
        }

        @Override
        public Stream<OWLIndividual> individuals() {
            return operands();
        }

        @Override
        public OWLClassExpression asObjectUnionOf() {
            Collection<ONTObject<? extends OWLIndividual>> values = getONTMembers();
            if (values.size() == 1) {
                return this;
            }
            DataFactory df = getDataFactory();
            return df.getOWLObjectUnionOf(values.stream().map(x -> df.getOWLObjectOneOf(x.getOWLObject())));
        }

        @Override
        public boolean containsEntityInSignature(OWLEntity entity) {
            if (entity == null || !entity.isIndividual()) return false;
            return namedIndividuals().anyMatch(entity::equals);
        }

        @Override
        public Set<OWLEntity> getSignatureSet() {
            return namedIndividuals().collect(Collectors.toCollection(this::createSortedSet));
        }

        @Override
        public Set<OWLNamedIndividual> getNamedIndividualSet() {
            return namedIndividuals().collect(Collectors.toCollection(this::createSortedSet));
        }

        @Override
        public Set<OWLAnonymousIndividual> getAnonymousIndividualSet() {
            return objectIndividuals()
                    .map(x -> x.getOWLObject().isOWLNamedIndividual() ? null :
                            x.getOWLObject().asOWLAnonymousIndividual())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(this::createSortedSet));
        }

        protected Stream<OWLNamedIndividual> namedIndividuals() {
            return objectIndividuals()
                    .map(x -> x.getOWLObject().isOWLNamedIndividual() ?
                            x.getOWLObject().asOWLNamedIndividual() : null)
                    .filter(Objects::nonNull);
        }

        @SuppressWarnings("unchecked")
        protected Stream<ONTObject<? extends OWLIndividual>> objectIndividuals() {
            List res = Arrays.asList(getContent());
            return (Stream<ONTObject<? extends OWLIndividual>>) res.stream();
        }

        @Override
        public Set<OWLClassExpression> getClassExpressionSet() {
            return createSet(this);
        }

        @Override
        public boolean canContainDataProperties() {
            return false;
        }

        @Override
        public boolean canContainObjectProperties() {
            return false;
        }

        @Override
        public boolean canContainNamedClasses() {
            return false;
        }

        @Override
        public boolean canContainDatatypes() {
            return false;
        }
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.objects.ce.OWLObjectComplementOfImpl
     * @see OntCE.ComplementOf
     */
    public static class CF
            extends ONTAnonymousClassExpressionImpl<OntCE.ComplementOf, OWLObjectComplementOf>
            implements OWLObjectComplementOf {

        public CF(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        public OntCE.ComplementOf asRDFNode() {
            return as(OntCE.ComplementOf.class);
        }

        @Override
        public OWLClassExpression getOperand() {
            return getONTClassExpression().getOWLObject();
        }

        @SuppressWarnings("unchecked")
        protected ONTObject<? extends OWLClassExpression> getONTClassExpression() {
            return (ONTObject<? extends OWLClassExpression>) getContent()[0];
        }

        @Override
        protected Object[] collectContent(OntCE.ComplementOf ce, InternalObjectFactory of) {
            return new Object[]{of.getClass(ce.getValue())};
        }

        @Override
        public boolean isClassExpressionLiteral() {
            return asRDFNode().getValue().isURIResource();
        }
    }

    protected abstract static class WithClassMembers<ONT extends OntCE.ComponentsCE<OntCE>,
            OWL extends OWLAnonymousClassExpression>
            extends WithMembers<OntCE, ONT, OWLClassExpression, OWL> {

        protected WithClassMembers(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        @Override
        protected ONTObject<? extends OWLClassExpression> map(OntCE i, InternalObjectFactory of) {
            return of.getClass(i);
        }
    }

    protected abstract static class WithMembers<ONT_M extends OntObject,
            ONT_C extends OntCE.ComponentsCE<ONT_M>,
            OWL_M extends OWLObject,
            OWL_C extends OWLAnonymousClassExpression>
            extends ONTAnonymousClassExpressionImpl<ONT_C, OWL_C>
            implements HasOperands<OWL_M>, HasComponents {

        protected WithMembers(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        protected abstract ONTObject<? extends OWL_M> map(ONT_M i, InternalObjectFactory of);

        @Override
        public Stream<OWL_M> operands() {
            return getOperandsAsList().stream();
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<OWL_M> getOperandsAsList() {
            List res = getONTMembers();
            return (List<OWL_M>) res;
        }

        @SuppressWarnings("unchecked")
        public List<ONTObject<? extends OWL_M>> getONTMembers() {
            List res = Arrays.asList(getContent());
            return (List<ONTObject<? extends OWL_M>>) res;
        }

        @Override
        protected Object[] collectContent(ONT_C ce, InternalObjectFactory of) {
            // OWL-API requires distinct and sorted Stream's and _List's_
            Set<ONTObject<? extends OWL_M>> res = createSortedSet(Comparator.comparing(ONTObject::getOWLObject));
            listONTMembers(ce).forEachRemaining(e -> res.add(map(e, of)));
            return res.toArray();
        }

        protected Iterator<ONT_M> listONTMembers(ONT_C ce) {
            return OntModels.listMembers(ce.getList());
        }
    }

    protected abstract static class WithDataRangeAndDataPropertyAndCardinality<ONT extends OntCE.CardinalityRestrictionCE<OntDR, OntNDP>,
            OWL extends OWLRestriction>
            extends WithDataRangeAndDataProperty<ONT, OWL> {

        protected WithDataRangeAndDataPropertyAndCardinality(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        public int getCardinality() {
            // [property, cardinality, filler]
            return (int) getContent()[1];
        }

        @Override
        public Stream<ONTObject<? extends OWLObject>> objects() {
            return Stream.of(getONTDataProperty(), getONTDataRange());
        }

        @Override
        protected Object[] collectContent(ONT ce, InternalObjectFactory of) {
            // [property, cardinality, filler]
            return new Object[]{of.getProperty(ce.getProperty()), ce.getCardinality(), of.getDatatype(ce.getValue())};
        }
    }

    protected abstract static class WithDataRangeAndDataProperty<ONT extends OntCE.ComponentRestrictionCE<OntDR, OntNDP>,
            OWL extends OWLRestriction>
            extends WithDataProperty<ONT, OWL> {

        protected WithDataRangeAndDataProperty(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        public OWLDataRange getFiller() {
            return getONTDataRange().getOWLObject();
        }

        @SuppressWarnings("unchecked")
        public ONTObject<? extends OWLDataRange> getONTDataRange() {
            // [property, cardinality, filler] or [property, filler]
            Object[] array = getContent();
            return (ONTObject<? extends OWLDataRange>) array[array.length - 1];
        }

        @Override
        protected Object[] collectContent(ONT ce, InternalObjectFactory of) {
            // [property, cardinality, filler] or [property, filler]
            return new Object[]{of.getProperty(ce.getProperty()), of.getDatatype(ce.getValue())};
        }
    }

    /**
     * An abstraction for simplification code,
     * that describes {@code owl:Restriction} containing datatype property.
     * Its signature also contains {@link OWLDatatype datatype} (explicitly or implicitly).
     * It cannot contain nested class expressions.
     *
     * @param <ONT> - subtype of {@link OntCE.RestrictionCE}
     * @param <OWL> - subtype of {@link OWLRestriction}
     */
    protected abstract static class WithDataProperty<ONT extends OntCE.RestrictionCE<OntNDP>,
            OWL extends OWLRestriction>
            extends ONTAnonymousClassExpressionImpl<ONT, OWL> {

        protected WithDataProperty(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        public OWLDataProperty getProperty() {
            return getONTDataProperty().getOWLObject();
        }

        @SuppressWarnings("unchecked")
        public ONTObject<OWLDataProperty> getONTDataProperty() {
            // [property, cardinality, filler] or [property, filler] or [property]
            return (ONTObject<OWLDataProperty>) getContent()[0];
        }

        @Override
        protected Object[] collectContent(ONT ce, InternalObjectFactory of) {
            // [property, cardinality, filler] or [property, filler] or [property]
            return new Object[]{of.getProperty(ce.getProperty())};
        }

        @Override
        public boolean containsEntityInSignature(OWLEntity entity) {
            if (entity == null || !entity.isOWLDatatype() && !entity.isOWLDataProperty()) return false;
            return super.containsEntityInSignature(entity);
        }

        @Override
        public boolean canContainNamedClasses() {
            return false;
        }

        @Override
        public boolean canContainNamedIndividuals() {
            return false;
        }

        @Override
        public boolean canContainObjectProperties() {
            return false;
        }

        @Override
        public Set<OWLClassExpression> getClassExpressionSet() {
            return createSet(this);
        }

        @Override
        public boolean canContainAnonymousIndividuals() {
            return false;
        }
    }

    protected abstract static class WithClassAndObjectPropertyAndCardinality<ONT extends OntCE.CardinalityRestrictionCE<OntCE, OntOPE>,
            OWL extends OWLRestriction>
            extends WithClassAndObjectProperty<ONT, OWL> {
        protected WithClassAndObjectPropertyAndCardinality(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        public int getCardinality() {
            // [property, cardinality, filler]
            return (int) getContent()[1];
        }

        @Override
        public Stream<ONTObject<? extends OWLObject>> objects() {
            return Stream.of(getONTObjectPropertyExpression(), getONTClassExpression());
        }

        @Override
        protected Object[] collectContent(ONT ce, InternalObjectFactory of) {
            // [property, cardinality, filler]
            return new Object[]{of.getProperty(ce.getProperty()), ce.getCardinality(), of.getClass(ce.getValue())};
        }
    }

    protected abstract static class WithClassAndObjectProperty<ONT extends OntCE.ComponentRestrictionCE<OntCE, OntOPE>,
            OWL extends OWLRestriction>
            extends WithObjectProperty<ONT, OWL> {

        protected WithClassAndObjectProperty(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        public OWLClassExpression getFiller() {
            return getONTClassExpression().getOWLObject();
        }

        @SuppressWarnings("unchecked")
        public ONTObject<? extends OWLClassExpression> getONTClassExpression() {
            // [property, cardinality, filler] or [property, filler]
            Object[] array = getContent();
            return (ONTObject<? extends OWLClassExpression>) array[array.length - 1];
        }

        @Override
        protected Object[] collectContent(ONT ce, InternalObjectFactory of) {
            return new Object[]{of.getProperty(ce.getProperty()), of.getClass(ce.getValue())};
        }
    }

    protected abstract static class WithObjectProperty<ONT extends OntCE.RestrictionCE<OntOPE>,
            OWL extends OWLRestriction>
            extends ONTAnonymousClassExpressionImpl<ONT, OWL> {

        protected WithObjectProperty(BlankNodeId n, Supplier<OntGraphModel> m) {
            super(n, m);
        }

        public OWLObjectPropertyExpression getProperty() {
            return getONTObjectPropertyExpression().getOWLObject();
        }

        @SuppressWarnings("unchecked")
        public ONTObject<? extends OWLObjectPropertyExpression> getONTObjectPropertyExpression() {
            // [property, cardinality, filler] or [property, filler] or [property]
            return (ONTObject<? extends OWLObjectPropertyExpression>) getContent()[0];
        }

        @Override
        protected Object[] collectContent(ONT ce, InternalObjectFactory of) {
            // [property, cardinality, filler] or [property, filler] or [property]
            return new Object[]{of.getProperty(ce.getProperty())};
        }
    }
}
