package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * This class is a REST controller for managing Item entities.
 * It provides endpoints for CRUD operations and processing items asynchronously.
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * This method retrieves all items from the repository.
     *
     * @return List of all items
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    /**
     * This method creates a new item.
     *
     * @param item Item to create
     * @param result BindingResult to handle validation errors
     * @return ResponseEntity with the created item and HTTP status
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    /**
     * This method retrieves an item by its ID.
     *
     * @param id ID of the item to retrieve
     * @return ResponseEntity with the item and HTTP status
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        try {
            return itemService.findById(id)
                    .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method updates an existing item.
     *
     * @param id ID of the item to update
     * @param item Item with updated data
     * @return ResponseEntity with the updated item and HTTP status
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        try {
            Optional<Item> existingItem = itemService.findById(id);
            if (existingItem.isPresent()) {
                item.setId(id);
                return new ResponseEntity<>(itemService.save(item), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method deletes an item by its ID.
     *
     * @param id ID of the item to delete
     * @return ResponseEntity with HTTP status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        try {
            Optional<Item> item = itemService.findById(id);
            if (item.isPresent()) {
                itemService.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method processes items asynchronously.
     *
     * @return List of processed items
     */
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        try {
            return new ResponseEntity<>(itemService.processItemsAsync(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
