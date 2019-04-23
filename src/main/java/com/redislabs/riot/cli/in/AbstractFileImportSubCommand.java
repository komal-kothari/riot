package com.redislabs.riot.cli.in;

import java.io.File;
import java.net.URL;

import com.redislabs.riot.file.FileReaderBuilder;

import picocli.CommandLine.Option;

public abstract class AbstractFileImportSubCommand extends AbstractImportSubCommand {

	@Option(names = { "-f",
			"--file" }, description = "Path to input file. Mutually exclusive with url option.")
	private File file;
	@Option(names = { "-u",
			"--url" }, description = "URL for input file. Mutually exclusive with file option.")
	private URL url;
	@Option(names = "--gzip", description = "Input is gzip compressed.")
	private boolean gzip;

	@Override
	public String getSourceDescription() {
		return "file " + (file == null ? url : file);
	}

	protected FileReaderBuilder builder() {
		FileReaderBuilder builder = new FileReaderBuilder();
		builder.setFile(file);
		builder.setUrl(url);
		builder.setGzip(gzip);
		return builder;
	}

}