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
package org.neo4j.gds.api.properties.nodes;

import org.neo4j.gds.api.nodeproperties.ValueType;
import org.neo4j.gds.collections.ha.HugeObjectArray;
import org.neo4j.gds.core.utils.paged.HugeAtomicBitSet;

import java.util.Optional;

public class BinaryArrayNodePropertyValues implements NodePropertyValues {

    private final HugeObjectArray<HugeAtomicBitSet> binaryEmbeddings;
    private final int embeddingDimension;

    public BinaryArrayNodePropertyValues(
        HugeObjectArray<HugeAtomicBitSet> binaryEmbeddings,
        int embeddingDimension
    ) {
        this.binaryEmbeddings = binaryEmbeddings;
        this.embeddingDimension = embeddingDimension;
    }

    @Override
    public double[] doubleArrayValue(long nodeId) {
        return bitSetToDoubleArray(binaryEmbeddings.get(nodeId), embeddingDimension);
    }

    @Override
    public float[] floatArrayValue(long nodeId) {
        return bitSetToFloatArray(binaryEmbeddings.get(nodeId), embeddingDimension);
    }

    @Override
    public long[] longArrayValue(long nodeId) {
        return bitSetToLongArray(binaryEmbeddings.get(nodeId), embeddingDimension);
    }

    @Override
    public Object getObject(long nodeId) {
        return bitSetToDoubleArray(binaryEmbeddings.get(nodeId), embeddingDimension);
    }

    @Override
    public Optional<Integer> dimension() {
        return Optional.of(embeddingDimension);
    }

    @Override
    public ValueType valueType() {
        return ValueType.DOUBLE_ARRAY;
    }

    @Override
    public long nodeCount() {
        return binaryEmbeddings.size();
    }

    private static double[] bitSetToDoubleArray(HugeAtomicBitSet bitSet, int dimension) {
        var array = new double[dimension];
        bitSet.forEachSetBit(bit -> {
            array[(int) bit] = 1.0;
        });
        return array;
    }

    private static float[] bitSetToFloatArray(HugeAtomicBitSet bitSet, int dimension) {
        var array = new float[dimension];
        bitSet.forEachSetBit(bit -> {
            array[(int) bit] = 1.0f;
        });
        return array;
    }

    private static long[] bitSetToLongArray(HugeAtomicBitSet bitSet, int dimension) {
        var array = new long[dimension];
        bitSet.forEachSetBit(bit -> {
            array[(int) bit] = 1;
        });
        return array;
    }
}
