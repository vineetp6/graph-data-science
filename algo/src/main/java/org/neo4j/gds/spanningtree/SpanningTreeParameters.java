/*
 * Copyright (c) "Neo4j"
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
package org.neo4j.gds.spanningtree;

import org.neo4j.gds.annotation.Parameters;

import java.util.function.DoubleUnaryOperator;

@Parameters
public class SpanningTreeParameters {
    static SpanningTreeParameters create(DoubleUnaryOperator objective, long sourceNode) {
        return new SpanningTreeParameters(objective, sourceNode);
    }

    private final DoubleUnaryOperator objective;

    private final long sourceNode;

    protected SpanningTreeParameters(DoubleUnaryOperator objective, long sourceNode) {
        this.objective = objective;
        this.sourceNode = sourceNode;
    }

    public long sourceNode() {
        return sourceNode;
    }

    public DoubleUnaryOperator objective() {
        return objective;
    }
}
