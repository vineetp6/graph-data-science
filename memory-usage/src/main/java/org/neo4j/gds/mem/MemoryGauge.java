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
package org.neo4j.gds.mem;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An oracle we ask for information about system memory.
 * We inject an AtomicLong that is populated elsewhere, and read from that.
 */
public class MemoryGauge {
    private final AtomicLong availableMemory;

    public MemoryGauge(AtomicLong availableMemory) {
        this.availableMemory = availableMemory;
    }

    // Start with `synchronized` and improve if needed.
    public synchronized long tryToReserveMemory(long bytesToReserve) {
        var available = availableMemory.get();
        if (bytesToReserve > available) {
            throw new MemoryReservationExceededException(bytesToReserve, available);
        }
        return availableMemory.addAndGet(-bytesToReserve);
    }
}
