// Copyright 2019 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.skyframe.actiongraph.v2;

import com.google.devtools.build.lib.analysis.AnalysisProtosV2.Action;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2.ActionGraphContainer;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2.Artifact;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2.AspectDescriptor;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2.Configuration;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2.DepSetOfFiles;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2.PathFragment;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2.RuleClass;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2.Target;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.io.PrintStream;

/** Manages the various streamed output channels of aquery. */
public class StreamedOutputHandler {
  /** Defines the types of proto output this class can handle. */
  public enum OutputType {
    BINARY("proto"),
    TEXT("textproto"),
    JSON("jsonproto");

    private final String formatName;

    OutputType(String formatName) {
      this.formatName = formatName;
    }

    public String formatName() {
      return formatName;
    }
  }

  private final OutputType outputType;
  private final CodedOutputStream outputStream;
  private final PrintStream printStream;
  private final JsonFormat.Printer jsonPrinter = JsonFormat.printer();

  public StreamedOutputHandler(
      OutputType outputType, CodedOutputStream outputStream, PrintStream printStream) {
    this.outputType = outputType;
    this.outputStream = outputStream;
    this.printStream = printStream;
  }

  public void printArtifact(Artifact message) throws IOException {
    printMessage(message, ActionGraphContainer.ARTIFACTS_FIELD_NUMBER, "artifacts");
  }

  public void printAction(Action message) throws IOException {
    printMessage(message, ActionGraphContainer.ACTIONS_FIELD_NUMBER, "actions");
  }

  public void printTarget(Target message) throws IOException {
    printMessage(message, ActionGraphContainer.TARGETS_FIELD_NUMBER, "targets");
  }

  public void printDepSetOfFiles(DepSetOfFiles message) throws IOException {
    printMessage(message, ActionGraphContainer.DEP_SET_OF_FILES_FIELD_NUMBER, "dep_set_of_files");
  }

  public void printConfiguration(Configuration message) throws IOException {
    printMessage(message, ActionGraphContainer.CONFIGURATION_FIELD_NUMBER, "configuration");
  }

  public void printAspectDescriptor(AspectDescriptor message) throws IOException {
    printMessage(
        message, ActionGraphContainer.ASPECT_DESCRIPTORS_FIELD_NUMBER, "aspect_descriptors");
  }

  public void printRuleClass(RuleClass message) throws IOException {
    printMessage(message, ActionGraphContainer.RULE_CLASSES_FIELD_NUMBER, "rule_classes");
  }

  public void printPathFragment(PathFragment message) throws IOException {
    printMessage(message, ActionGraphContainer.PATH_FRAGMENTS_FIELD_NUMBER, "path_fragments");
  }

  /**
   * Prints the Message to the appropriate output channel.
   *
   * @param message The message to be printed.
   */
  private void printMessage(Message message, int fieldNumber, String messageLabel)
      throws IOException {
    switch (outputType) {
      case BINARY:
        outputStream.writeMessage(fieldNumber, message);
        break;
      case TEXT:
        printStream.print(messageLabel + " {\n" + message + "}\n");
        break;
      case JSON:
        jsonPrinter.appendTo(message, printStream);
        printStream.println();
        break;
    }
  }

  /** Called at the end of the query process. */
  public void close() throws IOException {
    switch (outputType) {
      case BINARY:
        outputStream.flush();
        break;
      case TEXT:
      case JSON:
        printStream.flush();
        printStream.close();
        break;
    }
  }
}
