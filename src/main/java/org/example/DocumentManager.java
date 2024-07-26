package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> documents = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        Objects.requireNonNull(document);

        if (document.id == null || document.id.isBlank()) {
            document.id = generateDocumentId();
        }

        documents.put(document.id, document);

        return document;
    }

    private String generateDocumentId() {
        return Stream.generate(UUID::randomUUID)
                .takeWhile(uuid -> !documents.containsKey(uuid.toString()))
                .findFirst()
                .orElseThrow()
                .toString();
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        Objects.requireNonNull(request);

        Stream<Document> documentStream = documents.values().stream();

        List<String> titlePrefixes = request.titlePrefixes;
        if (titlePrefixes != null && !titlePrefixes.isEmpty()) {
            documentStream = documentStream.filter(document -> titlePrefixes.stream()
                    .anyMatch(prefix -> document.title.startsWith(prefix)));
        }

        List<String> containsContents = request.containsContents;
        if (containsContents != null && !containsContents.isEmpty()) {
            documentStream = documentStream.filter(document -> containsContents.stream()
                    .anyMatch(content -> document.content.toLowerCase().contains(content.toLowerCase())));
        }

        List<String> authorIds = request.authorIds;
        if (authorIds != null && !authorIds.isEmpty()) {
            documentStream = documentStream.filter(document -> authorIds.stream()
                    .anyMatch(authorId -> document.author.id.equals(authorId)));
        }

        if (request.createdFrom != null) {
            documentStream = documentStream
                    .filter(document -> request.createdFrom.isBefore(document.created));
        }

        if (request.createdTo != null) {
            documentStream = documentStream
                    .filter(document -> request.createdTo.isAfter(document.created));
        }

        return documentStream.collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (documents.containsKey(id)) {
            return Optional.of(documents.get(id));
        }

        return Optional.empty();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
