package com.google.abmedge.inventory.dto;

import java.util.List;

public class Inventory {

  private List<Item> items;

  public List<Item> getItems() {
    return items;
  }

  public void setItems(List<Item> items) {
    this.items = items;
  }
}
