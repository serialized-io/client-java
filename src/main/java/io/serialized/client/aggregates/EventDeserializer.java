package io.serialized.client.aggregates;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.serialized.client.aggregates.EventBatch.newEvent;

class EventDeserializer extends StdDeserializer<EventBatch.Event> {

  private Map<String, Class> eventTypes;

  private EventDeserializer(Map<String, Class> eventTypes) {
    super((Class) null);
    this.eventTypes = eventTypes;
  }

  static Module module(Map<String, Class> eventTypes) {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(EventBatch.Event.class, new EventDeserializer(eventTypes));
    return module;
  }

  @Override
  public EventBatch.Event deserialize(JsonParser jp, DeserializationContext context) throws IOException {

    JsonNode node = jp.getCodec().readTree(jp);

    String eventId = node.get("eventId").asText();
    String eventType = node.get("eventType").asText();
    EventBatch.EventBuilder eventBuilder = newEvent().eventType(eventType).eventId(UUID.fromString(eventId));

    Optional<Class> matchingClass = eventTypes
        .entrySet()
        .stream()
        .filter(et -> et.getKey().equals(eventType))
        .map(Map.Entry::getValue)
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
