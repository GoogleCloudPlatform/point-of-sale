package com.google.abmedge.inventory.util;

/** A checked exception that is thrown whenever there is a failure from the inventory connector */
public class InventoryStoreConnectorException extends Exception {
  private static final long serialVersionUID = 8828512147713293558L;

  public InventoryStoreConnectorException() {
    super();
  }

  public InventoryStoreConnectorException(String message, Throwable cause) {
    super(message, cause);
  }

  public InventoryStoreConnectorException(String message) {
    super(message);
  }

  public InventoryStoreConnectorException(Throwable cause) {
    super(cause);
  }
}
