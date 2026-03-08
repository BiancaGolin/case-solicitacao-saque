package org.example.solicitacaosaque.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;

@Data
@Document(collection = "idempotency")
public class Idempotency {

    @Id
    @Indexed(unique = true)
    private String idempotencyKey;
    private String payloadHash;
    private String responseJson;
    private OffsetDateTime createdAt;
}
