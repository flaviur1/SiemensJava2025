package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        // the errors were backwards, so I switched them
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK); // put OK instead of CREATED
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // put NOT_FOUND instead of ACCEPTED
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);// put OK instead of CONFLICT
    }

    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        try {
            List<Item> items = itemService.processItemsAsync().get();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/getprocessed")
    public ResponseEntity<List<Item>> getProcessedItems() {
        return new ResponseEntity<>(itemService.getProcessedItems(), HttpStatus.OK);
    }

    @GetMapping("/processed/nr")
    public ResponseEntity<AtomicInteger> getNrOfProcessedItems() {
        return new ResponseEntity<>(itemService.getProcessedCount(), HttpStatus.OK);
    }
}
