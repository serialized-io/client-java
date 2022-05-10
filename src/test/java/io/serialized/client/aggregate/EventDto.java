package io.serialized.client.aggregate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

public class EventDto {

  public String eventId;

  @NotBlank
  @Size(min = 1, max = 256)
  public String eventType;

  public LinkedHashMap<String, Object> data;

  @Size(min = 1, max = 65536)
  public String encryptedData;

  public Map<String, Object> metadata;

}
