package com.paulo.helpdesk_api_java.repositories.specifications;

import com.paulo.helpdesk_api_java.entities.Ticket;
import com.paulo.helpdesk_api_java.entities.enums.TicketPriority;
import com.paulo.helpdesk_api_java.entities.enums.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> withFilters(
            TicketStatus status,
            TicketPriority priority,
            Long clientId,
            Long attendantId,
            String search) {
        return Specification.allOf(
                equal("status", status),
                equal("priority", priority),
                nestedIdEqual("client", clientId),
                nestedIdEqual("attendant", attendantId),
                textContains(search));
    }

    private static <T> Specification<Ticket> equal(String field, T value) {
        return value == null
                ? Specification.unrestricted()
                : (root, query, builder) -> builder.equal(root.get(field), value);
    }

    private static Specification<Ticket> nestedIdEqual(String relation, Long id) {
        return id == null
                ? Specification.unrestricted()
                : (root, query, builder) -> builder.equal(root.get(relation).get("id"), id);
    }

    private static Specification<Ticket> textContains(String search) {
        if (search == null || search.isBlank()) {
            return Specification.unrestricted();
        }

        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("title")), pattern),
                builder.like(builder.lower(root.get("description")), pattern));
    }
}
