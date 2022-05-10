package io.serialized.client.aggregate;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public class BulkEventBatchDto {

  @NotNull
  public UUID aggregateId;

  @Min(0)
  public Integer expectedVersion;

  @Valid
  @NotEmpty
  @Size(min = 1, max = 64)
  public List<EventDto> events;

}
