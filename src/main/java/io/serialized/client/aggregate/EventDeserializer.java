package io.serialized.client.aggregate;

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

import static io.serialized.client.aggregate.Event.newEvent;

class EventDeserializer extends StdDeserializer<Event> {

  private Map<String, Class> eventTypes;

  private EventDeserializer(Map<String, Class> eventTypes) {
    super((Class) null);
    this.eventTypes = eventTypes;
  }

  static Module module(Map<String, Class> eventTypes) {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Event.class, new EventDeserializer(eventTypes));
    return module;
  }

  @Override
  public Event deserialize(JsonParser jp, DeserializationContext context) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    String eventId = node.get("eventId").asText();
    String eventType = node.get("eventType").asText();

    Optional<Class> matchingClass = eventTypes
        .entrySet()
        .stream()
        .filter(et -> et.getKey().equals(eventType))
        .map(Map.Entry::getValue)
        .findFirst();

    JsonNode data = node.get("data");
    if (matchingClass.isPresent()) {
      Event.TypedBuilder eventBuilder = newEvent(matchingClass.get()).eventId(UUID.fromString(eventId));
      eventBuilder.data(jp.getCodec().treeToValue(data, matchingClass.get()));
      return eventBuilder.build();
    } else {
      Event.RawBuilder eventBuilder = newEvent(eventType).eventId(UUID.fromString(eventId));
      eventBuilder.data(jp.getCodec().treeToValue(data, Map.class));
      return eventBuilder.build();
    }
  }

}
