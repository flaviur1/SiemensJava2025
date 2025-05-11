package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ErrorManager;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    // I put the @Getter annotation at processedItems and processedCount so I can create methods in the controller
    // to check if they are created correctly

    @Getter
    private List<Item> processedItems = Collections.synchronizedList(new ArrayList<>());

    // we make the int AtomicInteger so it is threadSafe
    @Getter
    private AtomicInteger processedCount = new AtomicInteger(0);


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
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
     * <p>
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    /*
     * A brief explanation of what was wrong with the function:
     *
     * The function did not use a thread safe List or an integer for counting the processed items.
     * It also returned the result before all the async tasks were finished, which is bad :).
     * */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        // we save a future for all the async tasks that are launched in a list, which we then use to
        // wait for any unfinished tasks, thus making sure we return all of the results
        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(100);
                        Item item = itemRepository.findById(id).orElse(null);
                        if (item == null) {
                            return;
                        }

                        processedCount.getAndAdd(1);

                        item.setStatus("PROCESSED");
                        itemRepository.save(item);
                        // we synchronize the access to make the addition thread safe
                        synchronized (processedItems) {
                            processedItems.add(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, executor))
                .toList();

        // we use .allOf() to combine all of our futures in the list, and create one combined future, but only when
        // they are all finished
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> processedItems);
    }

}

