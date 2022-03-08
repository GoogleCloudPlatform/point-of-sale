package com.google.abmedge.dao;

import com.google.abmedge.dto.Item;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called itemRepository
// CRUD refers Create, Read, Update, Delete
public interface ItemRepository extends CrudRepository<Item, UUID> {

}
