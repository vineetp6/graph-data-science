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
package org.neo4j.graphalgo.beta.pregel.cc;

import org.neo4j.graphalgo.api.nodeproperties.ValueType;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.PregelComputation;
import org.neo4j.graphalgo.beta.pregel.PregelConfig;
import org.neo4j.graphalgo.beta.pregel.PregelContext;
import org.neo4j.graphalgo.beta.pregel.annotation.GDSMode;
import org.neo4j.graphalgo.beta.pregel.annotation.PregelProcedure;

import java.util.Queue;

@PregelProcedure(
    name = "gds.pregel.test",
    description = "Test computation description",
    modes = {GDSMode.STREAM, GDSMode.WRITE, GDSMode.MUTATE, GDSMode.STATS}
)
public class Computation implements PregelComputation<PregelConfig> {

    @Override
    public Pregel.NodeSchema nodeSchema() {
        return null;
    }

    @Override
    public void compute(PregelContext.ComputeContext<PregelConfig> context, final long nodeId, Queue<Double> messages) {

    }
}
