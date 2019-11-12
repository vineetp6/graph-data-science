/*
 * Copyright (c) 2017-2019 "Neo4j,"
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

package org.neo4j.graphalgo.core.write;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.PropertyMapping;
import org.neo4j.graphalgo.TestDatabaseCreator;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.core.GraphLoader;
import org.neo4j.graphalgo.core.loading.HugeGraphFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import static org.neo4j.graphalgo.TestGraph.Builder.fromGdl;
import static org.neo4j.graphalgo.TestSupport.assertGraphEquals;

class RelationshipExporterTest {

    private static final String DB_CYPHER =
        "CREATE" +
        "  (a {id: 0})" +
        ", (b {id: 1})";

    private GraphDatabaseAPI db;


    @BeforeEach
    void setup() {
        db = TestDatabaseCreator.createTestDatabase();
        db.execute(DB_CYPHER);
    }

    @AfterEach
    void shutdown() {
        db.shutdown();
    }

    @Test
    void exportRelationships() {
        // create graph to export
        GraphDatabaseAPI fromDb = TestDatabaseCreator.createTestDatabase();
        fromDb.execute(
            "CREATE" +
            "  (a {id: 0})" +
            ", (b {id: 1})" +
            ", (a)-[:FOOBAR {weight: 4.2}]->(b)");
        Graph fromGraph = new GraphLoader(fromDb)
            .withAnyLabel()
            .withRelationshipType("FOOBAR")
            .withRelationshipProperties(PropertyMapping.of("weight", 1.0))
            .load(HugeGraphFactory.class);

        // export into new database
        RelationshipExporter build = RelationshipExporter.of(db, fromGraph).build();
        build.write("FOOBAR", "weight", 0.0, Direction.OUTGOING);

        // validate
        Graph actual = new GraphLoader(db)
            .withAnyLabel()
            .withAnyRelationshipType()
            .withRelationshipProperties(PropertyMapping.of("weight", 1.0))
            .load(HugeGraphFactory.class);

        assertGraphEquals(fromGdl("(a)-[{w: 4.2}]->(b)"), actual);
    }

}