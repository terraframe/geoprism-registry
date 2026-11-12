/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.io;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks whether the values of each column in an imported dataset are unique.
 * Values are compared by their toString() representation, which matches how
 * the importers derive codes. Blank values are ignored. Once a column is known to contain a duplicate its
 * seen values are discarded, so only columns which are (so far) unique hold
 * their values in memory.
 *
 * To protect the server from very large datasets, the estimated memory held by
 * the tracker is capped. If the cap is exceeded all held values are released
 * and tracking stops: columns already known to be non-unique are still
 * reported, but no further duplicates will be detected.
 */
public class ColumnUniquenessTracker
{
  private static final Logger logger                = LoggerFactory.getLogger(ColumnUniquenessTracker.class);

  /**
   * Default maximum estimated memory, in bytes, held by the tracker (64 MB)
   */
  public static final long    DEFAULT_MAX_BYTES     = 64L * 1024 * 1024;

  /**
   * Rough per-value overhead of a String stored in a HashSet (HashMap node,
   * table slot, String and backing array headers)
   */
  private static final long   PER_VALUE_OVERHEAD    = 80;

  private final long          maxBytes;

  private long                estimatedBytes;

  private boolean             exhausted;

  private Map<String, Set<String>> seen;

  private Set<String>              nonUnique;

  public ColumnUniquenessTracker()
  {
    this(DEFAULT_MAX_BYTES);
  }

  public ColumnUniquenessTracker(long maxBytes)
  {
    this.maxBytes = maxBytes;
    this.estimatedBytes = 0;
    this.exhausted = false;
    this.seen = new HashMap<>();
    this.nonUnique = new TreeSet<>();
  }

  public void add(String column, Object value)
  {
    if (this.exhausted || column == null || value == null || this.nonUnique.contains(column))
    {
      return;
    }

    // Compare the untrimmed string value, which is exactly what the importers
    // use as the code (see ObjectImporter.getCode / GeoObjectImporter.getCode)
    String string = value.toString();

    if (string.trim().length() == 0)
    {
      return;
    }

    Set<String> values = this.seen.computeIfAbsent(column, k -> new HashSet<>());

    if (!values.add(string))
    {
      this.nonUnique.add(column);
      this.estimatedBytes -= values.stream().mapToLong(ColumnUniquenessTracker::estimate).sum();
      this.seen.remove(column);
    }
    else
    {
      this.estimatedBytes += estimate(string);

      if (this.estimatedBytes > this.maxBytes)
      {
        logger.info("Column uniqueness tracking exceeded its memory limit of [" + this.maxBytes + "] bytes. Duplicate detection has stopped for the remainder of the dataset.");

        this.exhausted = true;
        this.seen = new HashMap<>();
        this.estimatedBytes = 0;
      }
    }
  }

  private static long estimate(String value)
  {
    return PER_VALUE_OVERHEAD + ( 2L * value.length() );
  }

  /**
   * @return true if the memory limit was exceeded and tracking has stopped
   */
  public boolean isExhausted()
  {
    return exhausted;
  }

  public Set<String> getNonUniqueColumns()
  {
    return nonUnique;
  }
}
