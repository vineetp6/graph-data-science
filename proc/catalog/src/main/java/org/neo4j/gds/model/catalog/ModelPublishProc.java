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
package org.neo4j.gds.model.catalog;

import org.neo4j.gds.BaseProc;
import org.neo4j.gds.core.model.Model;
import org.neo4j.gds.core.model.ModelCatalog;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.io.IOException;
import java.util.stream.Stream;

import static org.neo4j.procedure.Mode.READ;

public class ModelPublishProc extends BaseProc {
    private static final String DESCRIPTION = "Make a trained model accessible by all users";

    @Procedure(name = "gds.alpha.model.publish", mode = READ)
    @Description(DESCRIPTION)
    public Stream<ModelCatalogProc.ModelResult> publish(@Name(value = "modelName") String modelName) throws IOException {
        Model<?, ?> publish = ModelCatalog.publish(username(), modelName);
        return Stream.of(new ModelCatalogProc.ModelResult(publish));
    }
}
