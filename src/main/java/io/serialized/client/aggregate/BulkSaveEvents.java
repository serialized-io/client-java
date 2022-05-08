package io.serialized.client.aggregate;

import java.util.Collections;
import java.util.List;

public class BulkSaveEvents {

  public List<EventBatch> batches;

  public static BulkSaveEvents newBulkSaveEvents(List<EventBatch> batches) {
    BulkSaveEvents bulkSaveEvents = new BulkSaveEvents();
    bulkSaveEvents.batches = Collections.unmodifiableList(batches);
    return bulkSaveEvents;
  }

}
