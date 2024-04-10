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
package org.neo4j.gds.embeddings.hashgnn;

import org.neo4j.gds.annotation.Parameters;

import java.util.List;
import java.util.Optional;

@Parameters
public record HashGNNParameters(
    int concurrency,
    int iterations,
    int embeddingDensity,
    double neighborInfluence,
    List<String> featureProperties,
    boolean heterogeneous,
    Optional<Integer> outputDimension,
    Optional<BinarizeFeaturesConfig> binarizeFeatures,
    Optional<GenerateFeaturesConfig> generateFeatures,
    Optional<Long> randomSeed
) {
}
