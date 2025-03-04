/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package schemacrawler.integration.test;


import static org.hamcrest.MatcherAssert.assertThat;
import static schemacrawler.test.utility.ExecutableTestUtility.executableExecution;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.hasSameContentAs;
import static schemacrawler.test.utility.FileHasContent.outputOf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.cwi.monetdb.embedded.env.MonetDBEmbeddedDatabase;
import nl.cwi.monetdb.embedded.env.MonetDBEmbeddedException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.test.utility.BaseAdditionalDatabaseTest;
import schemacrawler.test.utility.DatabaseTestUtility;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.text.schema.SchemaTextOptions;
import schemacrawler.tools.text.schema.SchemaTextOptionsBuilder;

public class MonetDBTest
  extends BaseAdditionalDatabaseTest
{

  private boolean isDatabaseRunning;

  @AfterEach
  public void stopDatabaseServer()
    throws MonetDBEmbeddedException
  {
    if (isDatabaseRunning)
    {
      MonetDBEmbeddedDatabase.stopDatabase();
    }
  }

  @BeforeEach
  public void createDatabase()
  {
    try
    {
      // Set up native libraries, and load JDBC driver
      final Path directoryPath = Files.createTempDirectory("monetdbjavalite");
      MonetDBEmbeddedDatabase.startDatabase(directoryPath.toString());
      if (MonetDBEmbeddedDatabase.isDatabaseRunning())
      {
        MonetDBEmbeddedDatabase.stopDatabase();
      }

      createDataSource("jdbc:monetdb:embedded::memory:", null, null);
      createDatabase("/monetdb.scripts.txt");

      isDatabaseRunning = true;
    }
    catch (final Throwable e)
    {
      LOGGER.log(Level.FINE, e.getMessage(), e);
      // Do not run if MonetDBLite cannot be loaded
      isDatabaseRunning = false;
    }
  }

  @Test
  public void testMonetDBWithConnection()
    throws Exception
  {
    if (!isDatabaseRunning)
    {
      LOGGER.log(Level.INFO, "Did NOT run MonetDB test");
      return;
    }

    final SchemaCrawlerOptions options = DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder
      .builder();
    textOptionsBuilder.noIndexNames().showDatabaseInfo().showJdbcDriverInfo();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable("details");
    executable.setSchemaCrawlerOptions(options);
    executable.setAdditionalConfiguration(SchemaTextOptionsBuilder
      .builder(textOptions).toConfig());

    assertThat(outputOf(executableExecution(getConnection(), executable)),
               hasSameContentAs(classpathResource("testMonetDBWithConnection.txt")));
    LOGGER.log(Level.INFO, "Completed MonetDB test successfully");
  }

}
