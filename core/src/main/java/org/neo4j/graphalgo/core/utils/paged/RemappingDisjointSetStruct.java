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
package org.neo4j.graphalgo.core.utils.paged;


import com.carrotsearch.hppc.LongLongHashMap;
import com.carrotsearch.hppc.LongLongMap;
import com.carrotsearch.hppc.OpenHashContainers;
import org.neo4j.graphalgo.api.HugeWeightMapping;
import org.neo4j.graphalgo.core.utils.mem.MemoryEstimation;
import org.neo4j.graphalgo.core.utils.mem.MemoryEstimations;
import org.neo4j.graphalgo.core.utils.mem.MemoryRange;

import static org.neo4j.graphalgo.core.utils.mem.MemoryUsage.sizeOfDoubleArray;
import static org.neo4j.graphalgo.core.utils.mem.MemoryUsage.sizeOfLongArray;

/**
 * Implements {@link DisjointSetStruct} with support for incremental computation based on a previously computed mapping
 * between node ids and set ids.
 * Note that this does not use <a href=https://en.wikipedia.org/wiki/Disjoint-set_data_structure#by_rank">Union by Rank</a>
 * but prefers the minimum set id instead when merging two sets.
 */
public final class RemappingDisjointSetStruct extends DisjointSetStruct {

    public static final MemoryEstimation MEMORY_ESTIMATION = MemoryEstimations.builder(
            RemappingDisjointSetStruct.class)
            .perNode("parent", HugeLongArray::memoryEstimation)
            .rangePerNode("internalToProvidedIds", nodeCount -> {
                int minBufferSize = OpenHashContainers.emptyBufferSize();
                int maxBufferSize = OpenHashContainers.expectedBufferSize((int) nodeCount);
                if (maxBufferSize < minBufferSize) {
                    minBufferSize = maxBufferSize;
                    maxBufferSize = OpenHashContainers.emptyBufferSize();
                }
                long min = sizeOfLongArray(minBufferSize) + sizeOfDoubleArray(minBufferSize);
                long max = sizeOfLongArray(maxBufferSize) + sizeOfDoubleArray(maxBufferSize);
                return MemoryRange.of(min, max);
            })
            .build();

    private final HugeLongArray parent;
    private final LongLongHashMap internalToProvidedIds;
    private final HugeWeightMapping communityMapping;
    private final long capacity;

    public static MemoryEstimation memoryEstimation() {
        return RemappingDisjointSetStruct.MEMORY_ESTIMATION;
    }

    /**
     * Initialize the struct with the given capacity.
     * Note: the struct must be {@link RemappingDisjointSetStruct#reset()} prior use!
     *
     * @param capacity the capacity (maximum node id)
     */
    public RemappingDisjointSetStruct(long capacity, HugeWeightMapping communityMapping, AllocationTracker tracker) {
        this.parent = HugeLongArray.newArray(capacity, tracker);
        this.internalToProvidedIds = new LongLongHashMap();
        this.communityMapping = communityMapping;
        this.capacity = capacity;
    }

    @Override
    public HugeLongArray parent() {
        return parent;
    }

    /**
     * reset the container
     */
    @Override
    public RemappingDisjointSetStruct reset() {
        final LongLongMap internalMapping = new LongLongHashMap();
        internalToProvidedIds.clear();
        parent.setAll(nodeId -> {
            double communityIdValue = communityMapping.nodeWeight(nodeId, Double.NaN);
            if (!Double.isNaN(communityIdValue)) {
                long communityId = (long) communityIdValue;
                int idIndex = internalMapping.indexOf(communityId);
                if (internalMapping.indexExists(idIndex)) {
                    return internalMapping.indexGet(idIndex);
                }
                internalMapping.indexInsert(idIndex, communityId, nodeId);
                internalToProvidedIds.put(nodeId, communityId);
            }
            return -1L;
        });
        return this;
    }

    /**
     * element (node) count
     *
     * @return the element count
     */
    @Override
    public long capacity() {
        return capacity;
    }

    /**
     * find setId of element p.
     *
     * @param p the element in the set we are looking for
     * @return an id of the set it belongs to
     */
    @Override
    public long find(long p) {
        return findPC(p);
    }

    /**
     * join set of p (Sp) with set of q (Sq) so that {@link RemappingDisjointSetStruct#connected(long, long)}
     * for any pair of (Spi, Sqj) evaluates to true. Some optimizations exists
     * which automatically balance the tree, the "weighted union rule" is used here.
     *
     * @param p an item of Sp
     * @param q an item of Sq
     */
    @Override
    public void union(long p, long q) {
        long pSet = find(p);
        long qSet = find(q);

        if (pSet < qSet) {
            parent.set(qSet, pSet);
        } else if (qSet < pSet) {
            parent.set(pSet, qSet);
        }
    }

    /**
     * find setId of element p without balancing optimization.
     *
     * @param nodeId the element in the set we are looking for
     * @return an id of the set it belongs to
     */
    @Override
    long findNoOpt(final long nodeId) {
        long p = nodeId;
        long np;
        while ((np = parent.get(p)) != -1L) {
            p = np;
        }
        return p;
    }

    @Override
    public long setIdOf(final long nodeId) {
        long setId = findNoOpt(nodeId);
        return internalToProvidedIds.getOrDefault(setId, setId);
    }
}
