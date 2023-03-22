/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl.upload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.CloseableReentrantLock;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.etl.ImportError;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ValidationProblem;

public class ImportHistoryProgressScribe implements ImportProgressListenerIF
{
  public static class Range implements Comparable<Range>
  {
    private long start;

    private long end;

    public Range(long value)
    {
      this.start = value;
      this.end = value;
    }

    public Range(long start, long end)
    {
      this.start = start;
      this.end = end;
    }

    @Override
    public int compareTo(Range o)
    {
      return Long.compare(this.start, o.start);
    }

    public boolean contains(long value)
    {
      return this.start <= value && value <= this.end;
    }

    @Override
    public String toString()
    {
      if (start == end)
      {
        return Long.toString(start);
      }

      return Long.toString(start) + " - " + Long.toString(end);
    }

    static SortedSet<Range> consecutiveRanges(SortedSet<Range> a, long entry)
    {
      a.add(new Range(entry));

      return consecutiveRanges(a);
    }

    static SortedSet<Range> consecutiveRanges(SortedSet<Range> set)
    {
      int length = 1;

      ArrayList<Range> a = new ArrayList<Range>(set);

      SortedSet<Range> list = new TreeSet<Range>();

      // If the array is empty,
      // return the list
      if (a.size() == 0)
      {
        return list;
      }

      // Traverse the array from first position
      for (int i = 1; i <= a.size(); i++)
      {

        // Check the difference between the
        // current and the previous elements
        // If the difference doesn't equal to 1
        // just increment the length variable.
        if (i == a.size() || a.get(i).start - a.get(i - 1).end != 1)
        {

          // If the range contains
          // only one element.
          // add it into the list.
          if (length == 1)
          {
            list.add(a.get(i - length));
          }
          else
          {

            // Build the range between the first
            // element of the range and the
            // current previous element as the
            // last range.
            list.add(new Range(a.get(i - length).start, a.get(i - 1).end));
          }

          // After finding the first range
          // initialize the length by 1 to
          // build the next range.
          length = 1;
        }
        else
        {
          length++;
        }
      }

      return list;
    }

    public static SortedSet<Range> parse(String string)
    {
      TreeSet<Range> set = new TreeSet<Range>();

      if (!StringUtils.isEmpty(string))
      {
        JsonArray array = JsonParser.parseString(string).getAsJsonArray();

        for (int i = 0; i < array.size(); i++)
        {
          JsonElement element = array.get(i);

          if (element.isJsonObject())
          {
            JsonObject object = element.getAsJsonObject();
            long start = object.get("start").getAsLong();
            long end = object.get("end").getAsLong();

            set.add(new Range(start, end));
          }
          else
          {
            set.add(new Range(element.getAsLong()));
          }
        }
      }

      return set;
    }

    public static String serialize(SortedSet<Range> set)
    {
      JsonArray array = new JsonArray();

      for (Range range : set)
      {
        if (range.start != range.end)
        {
          JsonObject object = new JsonObject();
          object.addProperty("start", range.start);
          object.addProperty("end", range.end);

          array.add(object);
        }
        else
        {
          array.add(range.start);
        }
      }

      return array.toString();
    }

  }

  private static Logger          logger                    = LoggerFactory.getLogger(ImportHistoryProgressScribe.class);

  public static final Integer    UPDATE_PROGRESS_EVERY_NUM = 10;

  private ImportHistory          history;

  private int                    recordedErrors            = 0;

  private Long                   importedRecords           = Long.valueOf(0);

  private SortedSet<Range>       completedRows             = new TreeSet<>();

  private Set<ValidationProblem> referenceProblems         = new TreeSet<ValidationProblem>();

  private Set<ValidationProblem> rowValidationProblems     = new TreeSet<ValidationProblem>();

  private CloseableReentrantLock lock                      = new CloseableReentrantLock();

  public ImportHistoryProgressScribe(ImportHistory history)
  {
    this.history = history;
    this.importedRecords = history.getImportedRecords();

    this.completedRows = Range.parse(history.getCompletedRowsJson());
  }

  @Override
  public void setWorkTotal(Long workTotal)
  {
    try (CloseableReentrantLock lock = this.lock.open())
    {
      this.history.appLock();
      this.history.setWorkTotal(workTotal);
      this.history.apply();

      logger.info("Starting import with total work size [" + this.history.getWorkTotal() + "] and import stage [" + this.history.getStage().get(0) + "].");
    }
  }

  @Override
  public void setCompletedRow(Long newRowNum)
  {
    try (CloseableReentrantLock lock = this.lock.open())
    {
      this.completedRows = Range.consecutiveRanges(this.completedRows, newRowNum);

      // System.out.println(newRowNum);

      // System.out.println(StringUtils.join(completedRows, ", "));

      if (newRowNum % UPDATE_PROGRESS_EVERY_NUM == 0)
      {
        this.history.appLock();
        this.history.setWorkProgress(this.completedRows.last().end);
        this.history.setCompletedRowsJson(Range.serialize(this.completedRows));
        this.history.apply();
      }
    }
  }

  @Override
  public void setImportedRecords(Long newImportedRecords)
  {
    try (CloseableReentrantLock lock = this.lock.open())
    {

      if (newImportedRecords % UPDATE_PROGRESS_EVERY_NUM == 0)
      {
        this.history.appLock();
        this.history.setImportedRecords(newImportedRecords);
        this.history.apply();
      }

      this.importedRecords = newImportedRecords;
    }
  }

  @Override
  public Long getWorkTotal()
  {
    return this.history.getWorkTotal();
  }

  @Override
  public Long getRowNumber()
  {
    if (this.completedRows.size() > 0)
    {
      return this.completedRows.last().end;
    }

    return Long.valueOf(-1L);
  }

  @Override
  public Long getWorkProgress()
  {
    return this.history.getWorkProgress();
  }

  @Override
  public Long getImportedRecordProgress()
  {
    return this.history.getImportedRecords();
  }

  @Override
  public Long getImportedRecords()
  {
    return this.importedRecords;
  }

  @Override
  public void recordError(Throwable ex, String objectJson, String objectType, long rowNum)
  {
    ImportError error = new ImportError();
    error.setHistory(this.history);
    error.setErrorJson(JobHistory.exceptionToJson(ex).toString());
    error.setObjectJson(objectJson);
    error.setObjectType(objectType);
    error.setRowIndex(rowNum);
    error.apply();

    this.history.appLock();
    this.history.setErrorCount(this.history.getErrorCount() + 1);
    this.history.apply();

    this.recordedErrors++;
  }

  public int getRecordedErrorCount()
  {
    return this.recordedErrors;
  }

  @Override
  public boolean hasValidationProblems()
  {
    return this.rowValidationProblems.size() > 0 || this.referenceProblems.size() > 0;
  }

  @Override
  public void addReferenceProblem(ValidationProblem problem)
  {
    try (CloseableReentrantLock lock = this.lock.open())
    {

      Iterator<ValidationProblem> it = this.referenceProblems.iterator();

      while (it.hasNext())
      {
        ValidationProblem vp = it.next();

        if (vp.getKey().equals(problem.getKey()))
        {
          vp.addAffectedRowNumber(Long.parseLong(problem.getAffectedRows()));
          return;
        }
      }

      this.referenceProblems.add(problem);
    }
  }

  @Override
  public void addRowValidationProblem(ValidationProblem problem)
  {
    try (CloseableReentrantLock lock = this.lock.open())
    {

      Iterator<ValidationProblem> it = this.rowValidationProblems.iterator();

      while (it.hasNext())
      {
        ValidationProblem vp = it.next();

        if (vp.getKey().equals(problem.getKey()))
        {
          vp.addAffectedRowNumber(Long.parseLong(problem.getAffectedRows()));
          return;
        }
      }

      this.rowValidationProblems.add(problem);
    }
  }

  @Override
  public void applyValidationProblems()
  {
    try (CloseableReentrantLock lock = this.lock.open())
    {

      for (ValidationProblem problem : this.referenceProblems)
      {
        problem.setHistory(this.history);
        problem.apply();
      }
      for (ValidationProblem problem : this.rowValidationProblems)
      {
        problem.setHistory(this.history);
        problem.apply();
      }
    }
  }

  @Override
  public void finalizeImport()
  {
    try (CloseableReentrantLock lock = this.lock.open())
    {
      this.history.appLock();
      this.history.setImportedRecords(this.importedRecords);
      this.history.setWorkProgress(this.completedRows.last().end);
      this.history.setCompletedRowsJson(Range.serialize(this.completedRows));
      this.history.apply();
    }
  }

  @Override
  public void incrementImportedRecords()
  {
    try (CloseableReentrantLock lock = this.lock.open())
    {

      this.importedRecords += 1;

      if (this.importedRecords % UPDATE_PROGRESS_EVERY_NUM == 0)
      {
        this.history.appLock();
        this.history.setImportedRecords(this.importedRecords);
        this.history.apply();
      }
    }
  }

  @Override
  public boolean isComplete(final Long rowNumber)
  {
    try (CloseableReentrantLock lock = this.lock.open())
    {
      return this.completedRows.stream().map(range -> range.contains(rowNumber.longValue())).reduce((a, b) -> a || b).orElse(false);
    }
  }

}