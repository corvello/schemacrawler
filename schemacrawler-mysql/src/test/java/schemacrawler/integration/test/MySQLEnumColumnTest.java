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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;
import static schemacrawler.utility.SchemaCrawlerUtility.getCatalog;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.server.mysql.MySQLUtility;
import schemacrawler.test.utility.BaseAdditionalDatabaseTest;
import schemacrawler.test.utility.HeavyDatabaseBuildCondition;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(HeavyDatabaseBuildCondition.class)
public class MySQLEnumColumnTest
  extends BaseAdditionalDatabaseTest
{

  @Container
  private MySQLContainer dbContainer = new MySQLContainer<>()
    .withCommand("mysqld", "--lower_case_table_names=1")
    .withUsername("schemacrawler");;

  @BeforeEach
  public void createDatabase()
    throws SQLException, SchemaCrawlerException
  {
    createDataSource(dbContainer.getJdbcUrl(),
                     dbContainer.getUsername(),
                     dbContainer.getPassword());
  }

  @Test
  public void columnWithEnum()
    throws Exception
  {
    try (final Connection connection = getConnection();
      final Statement stmt = connection.createStatement();)
    {
      stmt.execute(
        "CREATE TABLE shirts (name VARCHAR(40), size ENUM('small', 'medium', 'large'))");
      connection.commit();
    }

    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = SchemaCrawlerOptionsBuilder
      .builder().withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
    final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
      .toOptions();

    final Catalog catalog = getCatalog(getConnection(), schemaCrawlerOptions);
    final Schema schema = catalog.lookupSchema("test").orElse(null);
    assertThat(schema, notNullValue());
    final Table table = catalog.lookupTable(schema, "shirts").orElse(null);
    assertThat(table, notNullValue());
    final Column column = table.lookupColumn("size").orElse(null);
    assertThat(column, notNullValue());
    final List<String> enumValues = MySQLUtility.getEnumValues(column);
    assertThat(enumValues, containsInAnyOrder("small", "medium", "large"));
  }

}
