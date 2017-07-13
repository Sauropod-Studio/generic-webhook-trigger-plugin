package org.jenkinsci.plugins.gwt.resolvers;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.jenkinsci.plugins.gwt.FlattenerUtils.filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.gwt.GenericHeaderVariable;

import com.google.common.base.Optional;

public class RequestHeaderResolver {
  public RequestHeaderResolver() {}

  public Map<String, String> getRequestHeaders(
      List<GenericHeaderVariable> configuredGenericHeaderVariables,
      Map<String, Enumeration<String>> incomingHeaders) {
    Map<String, String> found = new HashMap<>();
    for (String headerName : incomingHeaders.keySet()) {
      Optional<GenericHeaderVariable> configuredVariable =
          findConfiguredVariable(configuredGenericHeaderVariables, headerName);
      if (!configuredVariable.isPresent()) {
        continue;
      }
      Enumeration<String> headerEnumeration = incomingHeaders.get(headerName);
      int i = 0;
      while (headerEnumeration.hasMoreElements()) {
        String headerValue = headerEnumeration.nextElement();
        String regexpFilter = configuredVariable.get().getRegexpFilter();
        String filteredValue = filter(headerValue, regexpFilter);
        found.put(headerName + "_" + i, filteredValue);
        i++;
      }
    }
    return found;
  }

  private Optional<GenericHeaderVariable> findConfiguredVariable(
      List<GenericHeaderVariable> configuredGenericHeaderVariables, String headerName) {
    for (GenericHeaderVariable ghv : configuredGenericHeaderVariables) {
      if (ghv.getKey().equals(headerName)) {
        return of(ghv);
      }
    }
    return absent();
  }
}
