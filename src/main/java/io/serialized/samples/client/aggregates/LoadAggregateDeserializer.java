package io.serialized.samples.client.aggregates;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.List;

public class LoadAggregateDeserializer extends StdDeserializer<LoadAggregateResponse> {

  public LoadAggregateDeserializer() {
    this(null);
  }

  protected LoadAggregateDeserializer(Class<?> vc) {
    super(vc);
  }

  public static Module module() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(LoadAggregateResponse.class, new LoadAggregateDeserializer());
    return module;
  }

  @Override
  public LoadAggregateResponse deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {

    JsonNode node = jp.getCodec().readTree(jp);
    
    if (jp.getCurrentToken() != JsonToken.START_ARRAY) {
      JsonNode events1 = node.get("events");

      List events = jp.getCodec().treeToValue(events1, List.class);

      System.out.println("events = " + events);
    }

    return new LoadAggregateResponse();
  }
}
