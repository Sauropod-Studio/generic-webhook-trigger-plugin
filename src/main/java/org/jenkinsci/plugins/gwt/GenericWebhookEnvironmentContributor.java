package org.jenkinsci.plugins.gwt;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

@Extension
public class GenericWebhookEnvironmentContributor extends EnvironmentContributor {
  private static final String CONTRIBUTING_VARIABLES = "Contributing variables:";

  @SuppressWarnings("unchecked")
  @Override
  public void buildEnvironmentFor(
      @SuppressWarnings("rawtypes") @Nonnull Run r,
      @Nonnull EnvVars envs,
      @Nonnull TaskListener listener)
      throws IOException, InterruptedException {
    boolean shouldLog = shouldLog(r);
    GenericCause cause = (GenericCause) r.getCause(GenericCause.class);
    if (cause != null) {
      if (shouldLog) {
        listener
            .getLogger()
            .println(
                GenericWebhookEnvironmentContributor.class.getSimpleName()
                    + " Received:\n\n"
                    + cause.getPostContent()
                    + "\n\n");
      }
      Map<String, String> resolvedVariables = cause.getResolvedVariables();
      if (shouldLog) {
        listener.getLogger().println(CONTRIBUTING_VARIABLES + "\n");
      }
      for (String variable : resolvedVariables.keySet()) {
        String resolved = cause.getResolvedVariables().get(variable);
        if (shouldLog) {
          listener.getLogger().println("    " + variable + " = " + resolved);
        }
        envs.override(variable, resolved);
      }
      if (shouldLog) {
        listener.getLogger().println("\n");
      }
    }
  }

  private boolean shouldLog(@SuppressWarnings("rawtypes") Run r) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(r.getLogFile()))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.contains(CONTRIBUTING_VARIABLES)) {
          return false;
        }
      }
    }
    return true;
  }
}
