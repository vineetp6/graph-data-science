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
package org.neo4j.gds.ml.core.features;

import org.neo4j.gds.core.utils.paged.HugeObjectArray;

public class HugeObjectArrayFeatureConsumer implements FeatureConsumer {
    private final HugeObjectArray<double[]> features;

    public HugeObjectArrayFeatureConsumer(HugeObjectArray<double[]> features) {
        this.features = features;
    }

    @Override
    public void acceptScalar(long nodeOffset, int offset, double value) {
        features.get(nodeOffset)[offset] = value;
    }

    @Override
    public void acceptArray(long nodeOffset, int offset, double[] values) {
        System.arraycopy(values, 0, features.get(nodeOffset), offset, values.length);
    }
}
