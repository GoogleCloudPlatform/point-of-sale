// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.abmedge.inventory;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.PrimaryKey;
import com.google.cloud.spring.data.spanner.core.mapping.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * An instance of the {@link Item} class is a representation of an item as it will be stored in the
 * inventory. This class describes the information about a specific item that is available and
 * provides a utility method to get a deep copy of it.
 */
@Table(name = Item.ITEMS_TABLE)
public class Item implements Serializable {

  public static final String ITEMS_TABLE = "items";
  public static final String LABELS_TABLE = "labels";

  @Column(name="item_id")
  @PrimaryKey
  private UUID id;

  private String name;
  private String type;
  private BigDecimal price;
  private String imageUrl;
  private long quantity;

  // @Interleaved
  private List<String> labels;

  // @Version
  private Long version;

  public Item() {
    this.id = UUID.randomUUID();
    this.labels = new ArrayList<>();
  }

  public static Item from(Item item) {
    Item copyItem = new Item();
    copyItem.id = item.id;
    copyItem.type = item.type;
    copyItem.name = item.name;
    copyItem.price = item.price.setScale(9, RoundingMode.HALF_EVEN);
    copyItem.imageUrl = item.imageUrl;
    copyItem.quantity = item.quantity;
    copyItem.getLabels().addAll(item.getLabels());
    return copyItem;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public long getQuantity() {
    return quantity;
  }

  public void setQuantity(long quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Item)) {
      return false;
    }
    Item item = (Item) o;
    return id.equals(item.id)
        && name.equals(item.name)
        && type.equals(item.type)
        && price.equals(item.price);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, price);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Item.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("name='" + name + "'")
        .add("type='" + type + "'")
        .add("price=" + price)
        .add("imageUrl='" + imageUrl + "'")
        .add("quantity=" + quantity)
        .add("labels=" + labels)
        .toString();
  }
}
