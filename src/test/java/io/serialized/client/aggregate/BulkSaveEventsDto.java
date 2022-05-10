package io.serialized.client.aggregate;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class BulkSaveEventsDto {

  @Valid
  @NotNull
  @NotEmpty
  public List<BulkEventBatchDto> batches;

}
