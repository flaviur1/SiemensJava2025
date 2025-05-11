package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class InternshipApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void cleanUp() {
        itemRepository.deleteAll();
    }

    @Test
    void createNewItem() {
        Item item = new Item(null, "Test", "desc", "NEW", "test@example.com");

        webTestClient.post().uri("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(item)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Test")
                .jsonPath("$.email").isEqualTo("test@example.com");

        List<Item> items = itemRepository.findAll();
        assertThat(items).hasSize(1);
    }

}