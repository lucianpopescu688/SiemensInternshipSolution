package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InternshipApplicationTests {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemController itemController;

    private Item testItem1;
    private Item testItem2;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();

        testItem1 = new Item();
        testItem1.setName("Test Item 1");
        testItem1.setDescription("Test Description 1");
        testItem1.setStatus("New");
        testItem1.setEmail("test1@example.com");
        itemRepository.save(testItem1);

        testItem2 = new Item();
        testItem2.setName("Test Item 2");
        testItem2.setDescription("Test Description 2");
        testItem2.setStatus("New");
        testItem2.setEmail("test2@example.com");
        itemRepository.save(testItem2);
    }

    @Test
    void testItemEntity() {
        Item item = new Item(null, "Sample Item", "Sample Description", "New", "sample@example.com");

        assertNull(item.getId());
        assertEquals("Sample Item", item.getName());
        assertEquals("Sample Description", item.getDescription());
        assertEquals("New", item.getStatus());
        assertEquals("sample@example.com", item.getEmail());

        item.setName("Updated Item");
        assertEquals("Updated Item", item.getName());

        item.setDescription("Updated Description");
        assertEquals("Updated Description", item.getDescription());

        item.setStatus("Processing");
        assertEquals("Processing", item.getStatus());

        item.setEmail("updated@example.com");
        assertEquals("updated@example.com", item.getEmail());

        Item emptyItem = new Item();
        assertNull(emptyItem.getId());
        assertNull(emptyItem.getName());
        assertNull(emptyItem.getDescription());
        assertNull(emptyItem.getStatus());
        assertNull(emptyItem.getEmail());
    }

    @Test
    void testItemRepository() {
        List<Item> allItems = itemRepository.findAll();
        assertEquals(2, allItems.size());

        Optional<Item> foundItem = itemRepository.findById(testItem1.getId());
        assertTrue(foundItem.isPresent());
        assertEquals("Test Item 1", foundItem.get().getName());

        List<Long> ids = itemRepository.findAllIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains(testItem1.getId()));
        assertTrue(ids.contains(testItem2.getId()));

        Item newItem = new Item();
        newItem.setName("New Item");
        newItem.setDescription("New Description");
        newItem.setEmail("new@example.com");
        Item savedItem = itemRepository.save(newItem);
        assertNotNull(savedItem.getId());
        assertEquals("New Item", savedItem.getName());

        itemRepository.deleteById(savedItem.getId());
        Optional<Item> deletedItem = itemRepository.findById(savedItem.getId());
        assertFalse(deletedItem.isPresent());
    }

    // Service Tests
    @Test
    void testFindAll() {
        List<Item> items = itemService.findAll();
        assertEquals(2, items.size());
    }

    @Test
    void testFindById() {
        Optional<Item> foundItem = itemService.findById(testItem1.getId());
        assertTrue(foundItem.isPresent());
        assertEquals("Test Item 1", foundItem.get().getName());

        Optional<Item> notFoundItem = itemService.findById(999L);
        assertFalse(notFoundItem.isPresent());
    }

    @Test
    void testSave() {
        // Test creating new item
        Item newItem = new Item();
        newItem.setName("Service New Item");
        newItem.setDescription("Service New Description");
        newItem.setEmail("service.new@example.com");

        Item savedItem = itemService.save(newItem);
        assertNotNull(savedItem.getId());
        assertEquals("New", savedItem.getStatus());

        savedItem.setName("Updated Service Item");
        savedItem.setStatus("Processing");
        Item updatedItem = itemService.save(savedItem);
        assertEquals("Updated Service Item", updatedItem.getName());
        assertEquals("Processing", updatedItem.getStatus());
    }

    @Test
    void testDeleteById() {
        itemService.deleteById(testItem1.getId());
        Optional<Item> deletedItem = itemRepository.findById(testItem1.getId());
        assertFalse(deletedItem.isPresent());

        assertThrows(RuntimeException.class, () -> {
            itemService.deleteById(999L);
        });
    }

    @Test
    void testGetAllItems() {
        ResponseEntity<List<Item>> response = itemController.getAllItems();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetItemById() {
        ResponseEntity<Item> response = itemController.getItemById(testItem1.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Item 1", response.getBody().getName());

        ResponseEntity<Item> notFoundResponse = itemController.getItemById(999L);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
    }

    @Test
    void testCreateItem() {
        Item newItem = new Item();
        newItem.setName("Controller New Item");
        newItem.setDescription("Controller New Description");
        newItem.setStatus("New");
        newItem.setEmail("controller.new@example.com");

        ResponseEntity<Item> response = itemController.createItem(newItem, new BeanPropertyBindingResult(newItem, "item"));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Controller New Item", response.getBody().getName());
        assertEquals("New", response.getBody().getStatus());

        Optional<Item> savedItem = itemRepository.findById(response.getBody().getId());
        assertTrue(savedItem.isPresent());
        assertEquals("Controller New Item", savedItem.get().getName());
    }


    @Test
    void testUpdateItem() {
        // Create updated item
        Item updatedItem = new Item();
        updatedItem.setName("Updated Controller Item");
        updatedItem.setDescription("Updated Controller Description");
        updatedItem.setStatus("Updated");
        updatedItem.setEmail("updated.controller@example.com");

        ResponseEntity<Item> response = itemController.updateItem(testItem1.getId(), updatedItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Controller Item", response.getBody().getName());
        assertEquals("Updated", response.getBody().getStatus());

        Optional<Item> savedItem = itemRepository.findById(testItem1.getId());
        assertTrue(savedItem.isPresent());
        assertEquals("Updated Controller Item", savedItem.get().getName());
    }

    @Test
    void testUpdateNonExistingItem() {
        Item updatedItem = new Item();
        updatedItem.setName("Non-existing Item");

        ResponseEntity<Item> response = itemController.updateItem(999L, updatedItem);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteItem() {
        ResponseEntity<Void> response = itemController.deleteItem(testItem1.getId());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        Optional<Item> deletedItem = itemRepository.findById(testItem1.getId());
        assertFalse(deletedItem.isPresent());
    }

    @Test
    void testDeleteNonExistingItem() {
        ResponseEntity<Void> response = itemController.deleteItem(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
