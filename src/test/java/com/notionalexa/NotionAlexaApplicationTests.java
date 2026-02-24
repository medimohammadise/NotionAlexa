package com.notionalexa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "notion.api-key=test-key",
        "notion.database-id=test-db-id"
})
class NotionAlexaApplicationTests {

    @Test
    void contextLoads() {
    }
}
