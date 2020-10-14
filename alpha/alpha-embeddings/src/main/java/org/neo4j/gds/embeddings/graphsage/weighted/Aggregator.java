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
package org.neo4j.gds.embeddings.graphsage.weighted;

import org.neo4j.gds.embeddings.graphsage.ddl4j.Variable;
import org.neo4j.gds.embeddings.graphsage.ddl4j.functions.Weights;
import org.neo4j.gds.embeddings.graphsage.ddl4j.tensor.Matrix;
import org.neo4j.gds.embeddings.graphsage.ddl4j.tensor.Tensor;
import org.neo4j.gds.embeddings.graphsage.subgraph.SubGraph;
import org.neo4j.graphalgo.core.utils.mem.MemoryRange;

import java.util.List;
import java.util.Locale;

import static org.neo4j.graphalgo.utils.StringFormatting.toUpperCaseWithLocale;

public interface Aggregator {
    Variable<Matrix> aggregate(Variable<Matrix> previousLayerRepresentations, int[][] adjacencyMatrix, int[] selfAdjacencyMatrix);

    default Variable<Matrix> aggregate(Variable<Matrix> previousLayerRepresentations, SubGraph subGraph, int[][] adjacencyMatrix, int[] selfAdjacencyMatrix) {
        return aggregate(previousLayerRepresentations, adjacencyMatrix, selfAdjacencyMatrix);
    }

    // TODO: maybe turn this generic?
    List<Weights<? extends Tensor<?>>> weights();

    enum AggregatorType {
        WEIGHTED_MEAN {
            @Override
            public MemoryRange memoryEstimation(
                long minNodeCount,
                long maxNodeCount,
                long minPreviousNodeCount,
                long maxPreviousNodeCount,
                int inputDimension,
                int embeddingDimension
            ) {
                // TODO: Implement memory estimation
                var minBound = 0L;
                var maxBound = 0L;
                return MemoryRange.of(minBound, maxBound);
            }
        },
        WEIGHTED_POOL {
            @Override
            public MemoryRange memoryEstimation(
                long minNodeCount,
                long maxNodeCount,
                long minPreviousNodeCount,
                long maxPreviousNodeCount,
                int inputDimension,
                int embeddingDimension
            ) {
                // TODO: Implement memory estimation
                var minBound = 0L;
                var maxBound = 0L;
                return MemoryRange.of(minBound, maxBound);
            }
        };

        public static AggregatorType of(String activationFunction) {
            return valueOf(toUpperCaseWithLocale(activationFunction));
        }

        public static AggregatorType parse(Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof String) {
                return of(((String) object).toUpperCase(Locale.ENGLISH));
            }
            if (object instanceof AggregatorType) {
                return (AggregatorType) object;
            }
            return null;
        }

        public static String toString(AggregatorType af) {
            return af.toString();
        }

        public abstract MemoryRange memoryEstimation(
            long minNodeCount,
            long maxNodeCount,
            long minPreviousNodeCount,
            long maxPreviousNodeCount,
            int inputDimension,
            int embeddingDimension
        );
    }
}
