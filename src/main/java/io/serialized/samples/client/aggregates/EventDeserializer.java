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
import java.util.*;

import static io.serialized.samples.client.aggregates.EventBatch.newEvent;

public class EventDeserializer extends StdDeserializer<EventBatch.Event> {

  private Set<Class> eventTypes;

  public EventDeserializer(Set<Class> eventTypes) {
    super((Class) null);
    this.eventTypes = eventTypes;
  }

  public static Module module(Set<Class> eventTypes) {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(EventBatch.Event.class, new EventDeserializer(eventTypes));
    return module;
  }

  @Override
  public EventBatch.Event deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {

    JsonNode node = jp.getCodec().readTree(jp);

    String eventId = node.get("eventId").asText();
    String eventType = node.get("eventType").asText();
    EventBatch.EventBuilder eventBuilder = newEvent().eventType(eventType).eventId(UUID.fromString(eventId));


    Optional<Class> matchingClass = eventTypes.stream().filter(et -> et.getSimpleName().equals(eventType))
        .findFirst();

    JsonNode data = node.get("data");
    if (matchingClass.isPresent()) {
      eventBuilder.data(jp.getCodec().treeToValue(data, matchingClass.get()));
    } else {
      eventBuilder.data(jp.getCodec().treeToValue(data, Map.class));
    }

    return eventBuilder.build();
  }
}
