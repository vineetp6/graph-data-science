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
package org.neo4j.gds.procedures.algorithms.centrality;

import org.neo4j.gds.algorithms.centrality.CentralityAlgorithmResult;
import org.neo4j.gds.api.Graph;
import org.neo4j.gds.api.GraphStore;
import org.neo4j.gds.applications.algorithms.machinery.AlgorithmProcessingTimings;
import org.neo4j.gds.applications.algorithms.machinery.ResultBuilder;
import org.neo4j.gds.applications.algorithms.metadata.NodePropertiesWritten;
import org.neo4j.gds.closeness.ClosenessCentralityWriteConfig;

import java.util.Optional;
import java.util.stream.Stream;

class ClosenessCentralityResultBuilderForWriteMode implements ResultBuilder<ClosenessCentralityWriteConfig, CentralityAlgorithmResult, Stream<CentralityWriteResult>, NodePropertiesWritten> {
    private final GenericCentralityResultBuilderForWriteMode genericResultBuilder = new GenericCentralityResultBuilderForWriteMode();

    private final boolean shouldComputeCentralityDistribution;

    ClosenessCentralityResultBuilderForWriteMode(boolean shouldComputeCentralityDistribution) {
        this.shouldComputeCentralityDistribution = shouldComputeCentralityDistribution;
    }

    @Override
    public Stream<CentralityWriteResult> build(
        Graph graph,
        GraphStore graphStore,
        ClosenessCentralityWriteConfig configuration,
        Optional<CentralityAlgorithmResult> result,
        AlgorithmProcessingTimings timings,
        Optional<NodePropertiesWritten> metadata
    ) {
        return Stream.of(
            genericResultBuilder.build(
                graph,
                configuration,
                result,
                timings,
                metadata,
                shouldComputeCentralityDistribution
            )
        );
    }
}
