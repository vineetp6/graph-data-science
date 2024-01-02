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
package org.neo4j.gds.procedures.pathfinding;

import org.neo4j.gds.api.CloseableResourceRegistry;
import org.neo4j.gds.api.GraphName;
import org.neo4j.gds.api.NodeLookup;
import org.neo4j.gds.api.ProcedureReturnColumns;
import org.neo4j.gds.applications.algorithms.pathfinding.PathFindingAlgorithmsFacade;
import org.neo4j.gds.config.AlgoBaseConfig;
import org.neo4j.gds.core.CypherMapWrapper;
import org.neo4j.gds.paths.astar.config.ShortestPathAStarStreamConfig;
import org.neo4j.gds.paths.dijkstra.PathFindingResult;
import org.neo4j.gds.paths.dijkstra.config.AllShortestPathsDijkstraMutateConfig;
import org.neo4j.gds.paths.dijkstra.config.AllShortestPathsDijkstraStreamConfig;
import org.neo4j.gds.paths.dijkstra.config.ShortestPathDijkstraMutateConfig;
import org.neo4j.gds.paths.dijkstra.config.ShortestPathDijkstraStreamConfig;
import org.neo4j.gds.procedures.algorithms.ConfigurationCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This is the top facade on the Neo4j Procedures integration for path finding algorithms.
 * The role it plays is, to be newed up with request scoped dependencies,
 * and to capture the procedure-specific bits of path finding algorithms calls.
 * For example, translating a return column specification into a parameter, a business level concept.
 * This is also where we put result rendering.
 */
public class PathFindingProcedureFacade {
    // request scoped services
    private final CloseableResourceRegistry closeableResourceRegistry;
    private final ConfigurationCreator configurationCreator;
    private final NodeLookup nodeLookup;
    private final ProcedureReturnColumns procedureReturnColumns;

    // delegate
    private final PathFindingAlgorithmsFacade facade;

    public PathFindingProcedureFacade(
        CloseableResourceRegistry closeableResourceRegistry,
        ConfigurationCreator configurationCreator,
        NodeLookup nodeLookup,
        ProcedureReturnColumns procedureReturnColumns,
        PathFindingAlgorithmsFacade facade
    ) {
        this.closeableResourceRegistry = closeableResourceRegistry;
        this.configurationCreator = configurationCreator;
        this.nodeLookup = nodeLookup;
        this.procedureReturnColumns = procedureReturnColumns;

        this.facade = facade;
    }

    public Stream<PathFindingStreamResult> singlePairShortestPathAStarStream(
        String graphName,
        Map<String, Object> configuration
    ) {
        return runStreamAlgorithm(
            graphName,
            configuration,
            ShortestPathAStarStreamConfig::of,
            facade::singlePairShortestPathAStarStream
        );
    }

    public Stream<PathFindingMutateResult> singlePairShortestPathDijkstraMutate(
        String graphName,
        Map<String, Object> configuration
    ) {
        return Stream.of(
            runMutateAlgorithm(
                graphName,
                configuration,
                ShortestPathDijkstraMutateConfig::of,
                facade::singlePairShortestPathDijkstraMutate
            )
        );
    }

    public Stream<PathFindingStreamResult> singlePairShortestPathDijkstraStream(
        String graphName,
        Map<String, Object> configuration
    ) {
        return runStreamAlgorithm(
            graphName,
            configuration,
            ShortestPathDijkstraStreamConfig::of,
            facade::singlePairShortestPathDijkstraStream
        );
    }

    public Stream<PathFindingMutateResult> singleSourceShortestPathDijkstraMutate(
        String graphName,
        Map<String, Object> configuration
    ) {
        return Stream.of(
            runMutateAlgorithm(
                graphName,
                configuration,
                AllShortestPathsDijkstraMutateConfig::of,
                facade::singleSourceShortestPathDijkstraMutate
            )
        );
    }

    public Stream<PathFindingStreamResult> singleSourceShortestPathDijkstraStream(
        String graphNameAsString,
        Map<String, Object> rawConfiguration
    ) {
        return runStreamAlgorithm(
            graphNameAsString,
            rawConfiguration,
            AllShortestPathsDijkstraStreamConfig::of,
            facade::singleSourceShortestPathDijkstraStream
        );
    }

    /**
     * This is kind of a template method: Dijkstra and A* both use the same code structure.
     * It is quite banal:
     * <ol>
     *     <li> configuration parsing
     *     <li> parameter marshalling
     *     <li> delegating to down stream layer to call the thing we are actually interested in
     *     <li> handle resource closure
     * </ol>
     */
    private <CONFIGURATION extends AlgoBaseConfig> Stream<PathFindingStreamResult> runStreamAlgorithm(
        String graphNameAsString,
        Map<String, Object> rawConfiguration,
        Function<CypherMapWrapper, CONFIGURATION> configurationSupplier,
        AlgorithmHandle<CONFIGURATION, PathFindingResult, Stream<PathFindingStreamResult>> algorithm
    ) {
        var graphName = GraphName.parse(graphNameAsString);
        var configuration = configurationCreator.createConfigurationForStream(rawConfiguration, configurationSupplier);
        var resultBuilder = new PathFindingResultBuilderForStreamMode(
            nodeLookup,
            procedureReturnColumns.contains("path")
        );

        var resultStream = algorithm.compute(graphName, configuration, resultBuilder);

        // we need to do this for stream mode
        closeableResourceRegistry.register(resultStream);

        return resultStream;
    }

    /**
     * Contract this with {@link #runStreamAlgorithm}:
     * <ul>
     *     <li>Same input handling
     *     <li>Pick a different result marshaller - this is the big responsibility in this layer
     *     <li>Delegate to compute
     *     <li>No stream closing hook
     * </ul>
     *
     * So very much the same, but I didn't fancy trying to extract reuse today, for readability's sake
     */
    private <CONFIGURATION extends AlgoBaseConfig> PathFindingMutateResult runMutateAlgorithm(
        String graphNameAsString,
        Map<String, Object> rawConfiguration,
        Function<CypherMapWrapper, CONFIGURATION> configurationSupplier,
        AlgorithmHandle<CONFIGURATION, PathFindingResult, PathFindingMutateResult> algorithm
    ) {
        var graphName = GraphName.parse(graphNameAsString);
        var configuration = configurationCreator.createConfiguration(rawConfiguration, configurationSupplier);
        var resultBuilder = new PathFindingResultBuilderForMutateMode(configuration);

        return algorithm.compute(graphName, configuration, resultBuilder);
    }
}
