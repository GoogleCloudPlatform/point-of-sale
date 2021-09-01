package com.google.abmedge.dto;

import java.util.UUID;

public class PurchaseItem {
  private UUID itemId;
  private long itemCount;

  public UUID getItemId() {
    return itemId;
  }

  public void setItemId(UUID itemId) {
    this.itemId = itemId;
  }

  public long getItemCount() {
    return itemCount;
  }

  public void setItemCount(long itemCount) {
    this.itemCount = itemCount;
  }
}
