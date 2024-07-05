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
package org.neo4j.gds.applications.algorithms.embeddings;

import org.neo4j.gds.core.model.Model;
import org.neo4j.gds.core.model.ModelCatalog;
import org.neo4j.gds.embeddings.graphsage.GraphSageModelTrainer;
import org.neo4j.gds.embeddings.graphsage.ModelData;
import org.neo4j.gds.embeddings.graphsage.algo.GraphSageTrainConfig;

import java.nio.file.Path;

public class DefaultGraphSageModelRepository implements GraphSageModelRepository {
    private final ModelCatalog modelCatalog;
    private final Path directoryThatShouldBeInsideModelCatalog;

    public DefaultGraphSageModelRepository(ModelCatalog modelCatalog, Path directoryThatShouldBeInsideModelCatalog) {
        this.modelCatalog = modelCatalog;
        this.directoryThatShouldBeInsideModelCatalog = directoryThatShouldBeInsideModelCatalog;
    }

    @Override
    public void store(Model<ModelData, GraphSageTrainConfig, GraphSageModelTrainer.GraphSageTrainMetrics> model) {
        modelCatalog.store(model.creator(), model.name(), directoryThatShouldBeInsideModelCatalog);
    }
}
