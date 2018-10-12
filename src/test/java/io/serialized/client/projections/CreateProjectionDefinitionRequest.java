package io.serialized.client.projections;

import java.util.ArrayList;
import java.util.List;

public class CreateProjectionDefinitionRequest {

  public String projectionName;
  public String feedName;
  public boolean aggregated;
  public String idField;
  public List<ProjectionHandler> handlers;

  public static class ProjectionHandler {

    public String eventType;
    public List<Function> functions = new ArrayList<>();

    public static class Function {

      public String function;
      public String targetSelector;
      public String targetFilter;
      public String eventSelector;
      public String eventFilter;
      public String rawData;

    }

  }

}
