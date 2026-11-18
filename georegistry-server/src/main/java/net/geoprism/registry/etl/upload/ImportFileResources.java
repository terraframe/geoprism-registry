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
package net.geoprism.registry.etl.upload;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

import net.geoprism.registry.etl.CloseableDelegateFile;
import net.geoprism.registry.etl.ImportFileFormatException;

/**
 * Locates the file to import in an uploaded resource, for every import format.
 * An upload is either the file itself, or a zip containing it. Zips may contain
 * folders, and the files macOS adds when zipping (__MACOSX/ and ._*) are
 * ignored.
 */
public class ImportFileResources
{
  public static final String ZIP = "zip";

  private ImportFileResources()
  {
  }

  /**
   * @return true if the resource has the given extension, ignoring case
   */
  public static boolean hasExtension(ApplicationResource res, String extension)
  {
    return extension.equalsIgnoreCase(res.getNameExtension());
  }

  /**
   * Opens the file with the given extension from the resource: either the
   * resource itself, or the first matching file (by path) in a zip.
   *
   * @throws ImportFileFormatException
   *           if the resource is a zip without a matching file, or is neither
   *           a zip nor a file with the extension
   */
  public static CloseableFile getFile(ApplicationResource res, String extension)
  {
    if (hasExtension(res, ZIP))
    {
      return getFileFromZip(res, extension);
    }
    else if (hasExtension(res, extension))
    {
      return getLocalFile(res);
    }

    throw ImportFileFormatException.withDetails("Expected a ." + extension + " or ." + ZIP + " file, but got [" + res.getName() + "].");
  }

  /**
   * A file read in place, which is not deleted when it is closed, with the
   * name of the resource it belongs to. Files in the vault are stored under a
   * generated name without an extension, so the resource's name is the one to
   * show to users.
   */
  public static class ResourceFile extends CloseableFile
  {
    private static final long serialVersionUID = 1L;

    private final String      displayName;

    public ResourceFile(File file, String displayName)
    {
      super(file, false);

      this.displayName = displayName;
    }

    public String getDisplayName()
    {
      return displayName;
    }
  }

  /**
   * @return The name to show users for a file returned by
   *         {@link #getFile(ApplicationResource, String)}: the uploaded file's
   *         name, rather than the vault's name for it
   */
  public static String getDisplayName(File file)
  {
    return file instanceof ResourceFile ? ( (ResourceFile) file ).getDisplayName() : file.getName();
  }

  /**
   * @return The resource's file. A file in a local vault is read in place:
   *         {@link ApplicationResource#openNewFile()} copies the whole file,
   *         which for a large upload costs time and as much disk space again,
   *         and would happen for each stage of an import. Other resources are
   *         copied.
   */
  public static CloseableFile getLocalFile(ApplicationResource res)
  {
    if (res instanceof ApplicationFileResource && !res.isRemote())
    {
      File file = ( (ApplicationFileResource) res ).getUnderlyingFile();

      if (file != null && file.isFile())
      {
        return new ResourceFile(file, res.getName());
      }
    }

    return res.openNewFile();
  }

  /**
   * Extracts a zip resource and returns the first file (by path) with the
   * given extension. Closing the returned file deletes the extracted files.
   *
   * @throws ImportFileFormatException
   *           if the zip does not contain a matching file
   */
  public static CloseableFile getFileFromZip(ApplicationResource res, String extension)
  {
    CloseableFile directory = extractZip(res);

    try
    {
      List<File> files = findFiles(directory, extension);

      if (files.isEmpty())
      {
        throw newMissingFileException(extension);
      }

      return new CloseableDelegateFile(files.get(0), directory);
    }
    catch (RuntimeException e)
    {
      directory.close();

      throw e;
    }
  }

  public static ImportFileFormatException newMissingFileException(String extension)
  {
    return ImportFileFormatException.withDetails("The zip file does not contain a ." + extension + " file.");
  }

  /**
   * Extracts a zip resource into a new temporary directory. The returned file
   * is the directory, which is deleted when the file is closed.
   */
  public static CloseableFile extractZip(ApplicationResource res)
  {
    File dir;

    try
    {
      dir = Files.createTempDirectory(res.getBaseName()).toFile();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    CloseableDelegateFile closeable = new CloseableDelegateFile(dir, dir);

    try
    {
      try
      {
        extract(res, dir, StandardCharsets.UTF_8);
      }
      catch (ZipException | IllegalArgumentException e)
      {
        Charset cp437 = getDosCharset();

        if (cp437 == null)
        {
          throw e;
        }

        // Entry names which aren't marked as UTF-8 are read as UTF-8, which is
        // what most tools write. Windows' built in zip tool instead writes
        // them in the DOS code page (CP437), so names with non-ASCII
        // characters (e.g. Données.gpkg) can't be read. Try again, reading
        // unmarked names as CP437. Names marked as UTF-8 are still read as
        // UTF-8.
        FileUtils.cleanDirectory(dir);

        extract(res, dir, cp437);
      }

      return closeable;
    }
    catch (ZipException | EOFException e)
    {
      closeable.close();

      // e.g. a corrupt or truncated zip
      throw ImportFileFormatException.fromCause(e);
    }
    catch (IOException e)
    {
      closeable.close();

      throw new ProgrammingErrorException(e);
    }
    catch (RuntimeException e)
    {
      closeable.close();

      throw e;
    }
  }

  /**
   * @return The DOS code page used by Windows' zip tool, or null if the JVM
   *         doesn't include it
   */
  private static Charset getDosCharset()
  {
    try
    {
      return Charset.forName("IBM437");
    }
    catch (IllegalArgumentException e)
    {
      return null;
    }
  }

  /**
   * Finds the files with the given extension (case insensitive) anywhere under
   * a directory, sorted by their path relative to the directory. Files and
   * folders created by macOS when zipping (__MACOSX/ and ._* resource forks)
   * are ignored.
   */
  public static List<File> findFiles(File directory, String extension)
  {
    String suffix = "." + extension.toLowerCase(Locale.ROOT);
    Path root = directory.toPath();

    try (Stream<Path> paths = Files.walk(root))
    {
      return paths.filter(Files::isRegularFile) //
          .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(suffix)) //
          .filter(path -> !path.getFileName().toString().startsWith("._")) //
          .filter(path -> {
            for (Path segment : root.relativize(path))
            {
              if (segment.toString().equals("__MACOSX"))
              {
                return false;
              }
            }

            return true;
          }) //
          .sorted(Comparator.comparing(path -> root.relativize(path).toString())) //
          .map(Path::toFile) //
          .collect(Collectors.toList());
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  private static void extract(ApplicationResource res, File directory, Charset charset) throws IOException
  {
    // create a buffer to improve copy performance later.
    byte[] buffer = new byte[2048];

    try (InputStream iStream = res.openNewStream(); ZipInputStream zstream = new ZipInputStream(iStream, charset))
    {
      ZipEntry entry;

      while ( ( entry = zstream.getNextEntry() ) != null)
      {
        File file = new File(directory, entry.getName());

        if (!file.getCanonicalPath().startsWith(directory.getCanonicalPath() + File.separator))
        {
          // A malformed (or malicious) entry name, e.g. "../file"
          throw new ZipException("The zip entry [" + entry.getName() + "] is not a valid file name");
        }

        // Zips made by compressing a folder contain directory entries, and
        // entries for files inside folders
        if (entry.isDirectory())
        {
          if (!file.isDirectory() && !file.mkdirs())
          {
            throw new IOException("Unable to create directory: " + entry.getName());
          }

          continue;
        }

        File parent = file.getParentFile();

        if (!parent.isDirectory() && !parent.mkdirs())
        {
          throw new IOException("Unable to create directory: " + parent);
        }

        try (FileOutputStream output = new FileOutputStream(file))
        {
          int len = 0;

          while ( ( len = zstream.read(buffer) ) > 0)
          {
            output.write(buffer, 0, len);
          }
        }
      }
    }
  }
}
