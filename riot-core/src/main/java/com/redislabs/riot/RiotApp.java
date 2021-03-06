package com.redislabs.riot;

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.redislabs.riot.redis.AbstractRedisCommand;

import io.lettuce.core.RedisURI;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;
import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.PicocliException;
import picocli.CommandLine.RunFirst;

@Command(usageHelpAutoWidth = true, sortOptions = false, versionProvider = ManifestVersionProvider.class, subcommands = HiddenGenerateCompletion.class, abbreviateSynopsis = true)
public class RiotApp implements Runnable {

    private static final String ROOT_LOGGER = "";

    @Option(names = { "--help" }, usageHelp = true, description = "Show this help message and exit.")
    private boolean helpRequested;
    @Option(names = { "-V", "--version" }, versionHelp = true, description = "Print version information and exit.")
    private boolean versionRequested;
    @Option(names = { "-q", "--quiet" }, description = "Log errors only")
    private boolean quiet;
    @Option(names = { "-w", "--warn" }, description = "Set log level to warn")
    private boolean warn;
    @Option(names = { "-i", "--info" }, description = "Set log level to info")
    private boolean info;
    @Option(names = { "-d", "--debug" }, description = "Log in debug mode (includes normal stacktrace)")
    private boolean debug;
    @Getter
    @ArgGroup(heading = "Redis connection options%n", exclusive = false)
    private RedisConnectionOptions redisConnectionOptions = new RedisConnectionOptions();

    public int execute(String... args) {
	try {
	    CommandLine commandLine = commandLine();
	    ParseResult parseResult = parse(commandLine, args);
	    initializeLogging();
	    return commandLine.getExecutionStrategy().execute(parseResult);
	} catch (PicocliException e) {
	    System.err.println(e.getMessage());
	    return 1;
	}
    }

    private void initializeLogging() {
	InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
	LogManager.getLogManager().reset();
	Logger activeLogger = Logger.getLogger(ROOT_LOGGER);
	ConsoleHandler handler = new ConsoleHandler();
	handler.setLevel(Level.ALL);
	handler.setFormatter(new OneLineLogFormat(debug));
	activeLogger.addHandler(handler);
	Logger.getLogger(ROOT_LOGGER).setLevel(loggingLevel());
    }

    public CommandLine commandLine() {
	CommandLine commandLine = new CommandLine(this);
	registerConverters(commandLine);
	commandLine.setCaseInsensitiveEnumValuesAllowed(true);
	return commandLine;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ParseResult parse(CommandLine commandLine, String[] args) {
	ParseResult parseResult = commandLine.parseArgs(args);
	ParseResult subcommand = parseResult.subcommand();
	if (subcommand != null) {
	    Object command = subcommand.commandSpec().userObject();
	    if (AbstractImportCommand.class.isAssignableFrom(command.getClass())) {
		AbstractImportCommand<?, ?> importCommand = (AbstractImportCommand<?, ?>) command;
		List<ParseResult> parsedRedisCommands = subcommand.subcommands();
		for (ParseResult parsedRedisCommand : parsedRedisCommands) {
		    if (parsedRedisCommand.isUsageHelpRequested()) {
			return parsedRedisCommand;
		    }
		    importCommand.getRedisCommands()
			    .add((AbstractRedisCommand) parsedRedisCommand.commandSpec().userObject());
		}
		commandLine.setExecutionStrategy(new RunFirst());
		return subcommand;
	    }
	}
	return parseResult;
    }

    protected void registerConverters(CommandLine commandLine) {
	commandLine.registerConverter(RedisURI.class, new RedisURIConverter());
    }

    @Override
    public void run() {
	CommandLine.usage(this, System.out);
    }

    private Level loggingLevel() {
	if (debug) {
	    return Level.FINE;
	}
	if (info) {
	    return Level.INFO;
	}
	if (warn) {
	    return Level.WARNING;
	}
	if (quiet) {
	    return Level.OFF;
	}
	return Level.SEVERE;
    }

}
