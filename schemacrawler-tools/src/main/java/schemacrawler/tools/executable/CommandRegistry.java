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

package schemacrawler.tools.executable;


import static java.util.Comparator.naturalOrder;

import java.util.*;
import java.util.logging.Level;

import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerRuntimeException;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.OutputOptions;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;

/**
 * Command registry for mapping command to executable.
 *
 * @author Sualeh Fatehi
 */
public final class CommandRegistry
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger.getLogger(
    CommandRegistry.class.getName());

  public static CommandRegistry getCommandRegistry()
    throws SchemaCrawlerException
  {
    if (commandRegistrySingleton == null)
    {
      commandRegistrySingleton = new CommandRegistry();
    }
    return commandRegistrySingleton;
  }

  private static List<CommandProvider> loadCommandRegistry()
    throws SchemaCrawlerException
  {

    final List<CommandProvider> commandProviders = new ArrayList<>();

    commandProviders.add(new SchemaTextCommandProvider());
    commandProviders.add(new OperationCommandProvider());

    try
    {
      final ServiceLoader<CommandProvider> serviceLoader = ServiceLoader.load(
        CommandProvider.class);
      for (final CommandProvider commandProvider : serviceLoader)
      {
        LOGGER.log(Level.CONFIG,
                   new StringFormat("Loading command %s, provided by %s",
                                    commandProvider.getSupportedCommands(),
                                    commandProvider.getClass().getName()));
        commandProviders.add(commandProvider);
      }
    }
    catch (final Exception e)
    {
      throw new SchemaCrawlerException(
        "Could not load extended command registry",
        e);
    }

    return commandProviders;
  }
  private static CommandRegistry commandRegistrySingleton;

  private final List<CommandProvider> commandRegistry;

  private CommandRegistry()
    throws SchemaCrawlerException
  {
    commandRegistry = loadCommandRegistry();
  }

  public Collection<PluginCommand> getCommandLineCommands()
  {
    final Collection<PluginCommand> commandLineCommands = new HashSet<>();
    for (final CommandProvider commandProvider : commandRegistry)
    {
      commandLineCommands.add(commandProvider.getCommandLineCommand());
    }
    return commandLineCommands;
  }

  public Collection<CommandDescription> getSupportedCommands()
  {
    final Collection<CommandDescription> supportedCommandDescriptions = new HashSet<>();
    for (final CommandProvider commandProvider : commandRegistry)
    {
      supportedCommandDescriptions.addAll(commandProvider.getSupportedCommands());
    }

    final List<CommandDescription> supportedCommandsOrdered = new ArrayList<>(
      supportedCommandDescriptions);
    supportedCommandsOrdered.sort(naturalOrder());
    return supportedCommandsOrdered;
  }

  SchemaCrawlerCommand configureNewCommand(final String command,
                                           final SchemaCrawlerOptions schemaCrawlerOptions,
                                           final OutputOptions outputOptions)
    throws SchemaCrawlerException
  {
    CommandProvider executableCommandProvider = null;
    for (final CommandProvider commandProvider : commandRegistry)
    {
      if (commandProvider.supportsSchemaCrawlerCommand(command,
                                                       schemaCrawlerOptions,
                                                       outputOptions))
      {
        executableCommandProvider = commandProvider;
        break;
      }
    }
    if (executableCommandProvider == null)
    {
      executableCommandProvider = new OperationCommandProvider();
    }

    final SchemaCrawlerCommand scCommand;
    try
    {
      scCommand = executableCommandProvider.newSchemaCrawlerCommand(command);
      scCommand.setSchemaCrawlerOptions(schemaCrawlerOptions);
      scCommand.setOutputOptions(outputOptions);
    }
    catch (final Throwable e)
    {
      // Mainly catch NoClassDefFoundError, which is a Throwable, for
      // missing third-party jars
      LOGGER.log(Level.CONFIG, e.getMessage(), e);
      throw new SchemaCrawlerRuntimeException(String.format(
        "Cannot run command <%s>",
        command));
    }

    return scCommand;
  }

}
