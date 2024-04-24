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
package org.neo4j.gds.procedures.similarity;

import org.neo4j.gds.algorithms.similarity.SimilarityAlgorithmsEstimateBusinessFacade;
import org.neo4j.gds.algorithms.similarity.SimilarityAlgorithmsWriteBusinessFacade;
import org.neo4j.gds.api.ProcedureReturnColumns;
import org.neo4j.gds.applications.algorithms.machinery.MemoryEstimateResult;
import org.neo4j.gds.procedures.algorithms.configuration.ConfigurationCreator;
import org.neo4j.gds.procedures.algorithms.similarity.SimilarityWriteResult;
import org.neo4j.gds.similarity.filterednodesim.FilteredNodeSimilarityWriteConfig;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @deprecated use {@link org.neo4j.gds.procedures.algorithms.similarity.SimilarityProcedureFacade} instead
 */
@Deprecated
public class SimilarityProcedureFacade {
    private final ConfigurationCreator configurationCreator;
    private final ProcedureReturnColumns procedureReturnColumns;
    private final SimilarityAlgorithmsEstimateBusinessFacade estimateBusinessFacade;
    private final SimilarityAlgorithmsWriteBusinessFacade writeBusinessFacade;

    /**
     * @deprecated this sits here temporarily
     */
    @Deprecated
    private final org.neo4j.gds.procedures.algorithms.similarity.SimilarityProcedureFacade theOtherFacade;

    public SimilarityProcedureFacade(
        ConfigurationCreator configurationCreator,
        ProcedureReturnColumns procedureReturnColumns,
        SimilarityAlgorithmsEstimateBusinessFacade estimateBusinessFacade,
        SimilarityAlgorithmsWriteBusinessFacade writeBusinessFacade,
        org.neo4j.gds.procedures.algorithms.similarity.SimilarityProcedureFacade theOtherFacade
    ) {
        this.configurationCreator = configurationCreator;
        this.procedureReturnColumns = procedureReturnColumns;
        this.estimateBusinessFacade = estimateBusinessFacade;
        this.writeBusinessFacade = writeBusinessFacade;

        this.theOtherFacade = theOtherFacade;
    }

    //filtered

    public Stream<SimilarityWriteResult> filteredNodeSimilarityWrite(
        String graphName,
        Map<String, Object> configuration
    ) {
        var writeConfig = configurationCreator.createConfiguration(configuration, FilteredNodeSimilarityWriteConfig::of);

        var computationResult = writeBusinessFacade.filteredNodeSimilarity(
            graphName,
            writeConfig,
            procedureReturnColumns.contains("similarityDistribution")
        );

        return Stream.of(NodeSimilarityComputationResultTransformer.toWriteResult(computationResult));
    }

    public Stream<MemoryEstimateResult> filteredNodeSimilarityEstimateWrite(
        Object graphNameOrConfiguration,
        Map<String, Object> algoConfiguration
    ) {
        var config = configurationCreator.createConfiguration(algoConfiguration, FilteredNodeSimilarityWriteConfig::of);
        return Stream.of(estimateBusinessFacade.nodeSimilarity(graphNameOrConfiguration, config));
    }

    /**
     * @deprecated short term hack while migrating
     */
    @Deprecated
    public org.neo4j.gds.procedures.algorithms.similarity.SimilarityProcedureFacade theOtherFacade() {
        return theOtherFacade;
    }
}
