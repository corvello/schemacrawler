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
package schemacrawler.tools.integration.serialize;


import static sf.util.Utility.isBlank;

import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.executable.BaseCommandProvider;
import schemacrawler.tools.executable.CommandDescription;
import schemacrawler.tools.executable.SchemaCrawlerCommand;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.OutputOptions;

public class SerializationCommandProvider
  extends BaseCommandProvider
{

  private static final String DESCRIPTION_HEADER = "Create an offline catalog snapshot";

  public SerializationCommandProvider()
  {
    super(new CommandDescription(SerializationCommand.COMMAND,
                                 DESCRIPTION_HEADER));
  }

  @Override
  public SchemaCrawlerCommand newSchemaCrawlerCommand(final String command)
  {
    return new SerializationCommand();
  }

  @Override
  public boolean supportsSchemaCrawlerCommand(final String command,
                                              final SchemaCrawlerOptions schemaCrawlerOptions,
                                              final OutputOptions outputOptions)
  {
    if (outputOptions == null)
    {
      return false;
    }
    final String format = outputOptions.getOutputFormatValue();
    if (isBlank(format))
    {
      return false;
    }
    final boolean supportsSchemaCrawlerCommand =
      supportsCommand(command) && SerializationFormat.isSupportedFormat(format);
    return supportsSchemaCrawlerCommand;
  }

  @Override
  public PluginCommand getCommandLineCommand()
  {
    final PluginCommand pluginCommand = new PluginCommand(SerializationCommand.COMMAND,
                                                          "** "
                                                          + DESCRIPTION_HEADER,
                                                          "For more information, see https://www.schemacrawler.com/serialize.html %n");

    return pluginCommand;
  }

}
