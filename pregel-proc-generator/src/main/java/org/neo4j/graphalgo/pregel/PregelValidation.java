/*
 * Copyright (c) 2017-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.pregel;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import org.neo4j.graphalgo.annotation.ValueClass;
import org.neo4j.graphalgo.beta.pregel.PregelComputation;
import org.neo4j.graphalgo.beta.pregel.annotation.Pregel;
import org.neo4j.graphalgo.beta.pregel.annotation.Procedure;
import org.neo4j.procedure.Description;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.neo4j.graphalgo.utils.StringFormatting.formatWithLocale;

final class PregelValidation {

    private static final String PREGEL_ANNOTATION_VALUE = "value";
    private static final String PREGEL_ANNOTATION_CONFIG_CLASS = "configClass";

    private final Messager messager;
    private final Types typeUtils;
    private final Elements elementUtils;

    // Represents the PregelComputation interface
    private final TypeMirror pregelComputation;

    PregelValidation(Messager messager, Elements elementUtils, Types typeUtils) {
        this.messager = messager;
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.pregelComputation = elementUtils.getTypeElement(PregelComputation.class.getName()).asType();
    }

    Optional<Spec> validate(Element pregelElement) {
        var pregelAnnotationMirror = MoreElements.getAnnotationMirror(pregelElement, Pregel.class).get();
        var maybeConfigName = getAnnotationValueFromAlternatives(
            pregelAnnotationMirror,
            PREGEL_ANNOTATION_VALUE,
            PREGEL_ANNOTATION_CONFIG_CLASS
        );
        var maybeProcedure = Optional.ofNullable(pregelElement.getAnnotation(Procedure.class));

        if (
            !isClass(pregelElement) ||
            !isPregelComputation(pregelElement) ||
            !hasDistinctConfig(maybeConfigName, pregelElement, pregelAnnotationMirror) ||
            !hasProcedureAnnotation(maybeProcedure, pregelElement, pregelAnnotationMirror)
            // TODO: validate that config has a factory method with correct signature
        ) {
            return Optional.empty();
        }

        var computationName = pregelElement.getSimpleName().toString();
        var rootPackage = elementUtils.getPackageOf(pregelElement).getQualifiedName().toString();
        var maybeDescription = Optional.ofNullable(MoreElements
            .getAnnotationMirror(pregelElement, Description.class)
            .orNull());

        return Optional.of(ImmutableSpec.of(
            pregelElement,
            computationName,
            rootPackage,
            maybeConfigName.get(),
            maybeProcedure.get().value(),
            maybeDescription
        ));
    }

    private boolean isClass(Element pregelElement) {
        boolean isClass = pregelElement.getKind() == ElementKind.CLASS;
        if (!isClass) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "The annotated configuration must be a class.",
                pregelElement
            );
        }
        return isClass;
    }

    private boolean isPregelComputation(Element pregelElement) {
        var pregelTypeElement = MoreElements.asType(pregelElement);
        // TODO: this check needs to bubble up the inheritance tree
        var isPregelComputation = pregelTypeElement
            .getInterfaces()
            .stream()
            .anyMatch(tm -> typeUtils.isSameType(tm, pregelComputation));

        if (!isPregelComputation) {
            messager.printMessage(Diagnostic.Kind.ERROR, formatWithLocale(
                "Class must inherit %s",
                MoreTypes.asTypeElement(pregelComputation).getSimpleName()
            ), pregelTypeElement);
        }

        return isPregelComputation;
    }

    private boolean hasDistinctConfig(
        Optional<AnnotationValue> maybeConfigName,
        Element pregelElement,
        AnnotationMirror pregelAnnotationMirror
    ) {
        if (maybeConfigName.isEmpty()) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Only one of `value` or `configClass` may be set.",
                pregelElement,
                pregelAnnotationMirror
            );
            return false;
        }
        return true;
    }

    private boolean hasProcedureAnnotation(
        Optional<Procedure> maybeProcedure,
        Element pregelElement,
        AnnotationMirror pregelAnnotationMirror
    ) {
        return maybeProcedure.map(x -> true).orElseGet(() -> {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Procedure annotation must be present.",
                pregelElement,
                pregelAnnotationMirror
            );
            return false;
        });
    }

    private Optional<AnnotationValue> getAnnotationValueFromAlternatives(
        AnnotationMirror annotationMirror,
        String elementName1,
        String elementName2
    ) {
        var declaredValues = annotationMirror.getElementValues().entrySet().stream()
            .filter(entry -> entry.getKey().getSimpleName().contentEquals(elementName1) || entry
                .getKey()
                .getSimpleName()
                .contentEquals(elementName2))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());

        // get default value for elementName1
        if (declaredValues.isEmpty()) {
            for (var method : ElementFilter.methodsIn(annotationMirror
                .getAnnotationType()
                .asElement()
                .getEnclosedElements())) {
                if (method.getSimpleName().contentEquals(elementName1)) {
                    return Optional.ofNullable(method.getDefaultValue());
                }
            }
        }

        return declaredValues.size() == 1
            ? Optional.of(declaredValues.get(0))
            : Optional.empty();
    }

    @ValueClass
    interface Spec {
        Element element();

        String computationName();

        String rootPackage();

        AnnotationValue configName();

        String procedureName();

        Optional<AnnotationMirror> description();

        default ClassName className() {
            return ClassName.get(rootPackage(), computationName());
        }
    }

}
