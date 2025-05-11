package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * This class is responsible for handling business logic related to Item entities.
 * It interacts with the ItemRepository to perform CRUD operations and process items asynchronously.
 */
@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EmailValidator emailValidator;

    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * This method retrieves all items from the repository.
     *
     * @return List of all items
     */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * This method retrieves an item by its ID.
     *
     * @param id ID of the item to retrieve
     * @return Optional containing the item if found, otherwise empty
     */
    public Optional<Item> findById(Long id) {
        try {
            return itemRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving item with ID " + id, e);
        }
    }

    /**
     * This method saves an item to the repository.
     *
     * @param item Item to save
     * @return Saved item
     */
    public Item save(Item item) {
        if (item.getEmail() != null && !emailValidator.isValid(item.getEmail())) {
            throw new IllegalArgumentException("Invalid email address");
        }

        if (item.getId() == null && item.getStatus() == null || item.getStatus().isEmpty()) {
            item.setStatus("New");
        }

        return itemRepository.save(item);
    }

    /**
     * This method deletes an item by its ID.
     *
     * @param id ID of the item to delete
     */
    public void deleteById(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new RuntimeException("Item with ID " + id + " not found");
        }
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public List<Item> processItemsAsync() {
        List<Item> items = itemRepository.findAll();
        List<CompletableFuture<Item>> futures = new ArrayList<>();

        for (Item item : items) {
            CompletableFuture<Item> future = processItemAsync(item.getId());
            futures.add(future);
        }

        // Wait for all futures to complete and collect results
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get(); // Wait for all tasks to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error processing items", e);
        }

        // Collect results from futures
        List<Item> processedItems = new ArrayList<>();
        for (CompletableFuture<Item> future : futures) {
            try {
                processedItems.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Error retrieving processed item", e);
            }
        }

        return processedItems;
    }

    private CompletableFuture<Item> processItemAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                Optional<Item> optItem = itemRepository.findById(id);
                if (optItem.isPresent()) {
                    Item item = optItem.get();
                    item.setStatus("Processed");
                    return itemRepository.save(item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task was interrupted", e);
            }
            return null;
        }, executor);
    }
}

