package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.example.DocumentManager.*;
import static org.junit.jupiter.api.Assertions.*;

class DocumentManagerTest {

    private DocumentManager manager;

    private static final String DEVELOPER_ID = "3ae1a0fb-6c6b-40bb-93e7-29fe5473d095";

    @BeforeEach
    void setUp() {
        manager = new DocumentManager();
    }

    @Test
    @DisplayName("If document for save is null, then throw NPE")
    void whenSaveNull_thenThrowNPE() {
        assertThrows(NullPointerException.class, () -> manager.save(null));
    }

    @Test
    @DisplayName("When save new document it gets id")
    void whenSaveNewDocument_thenDocumentGetsId() {
        Document document = manager.save(createDocumentWithoutId());
        assertNotNull(document);
        assertNotNull(document.getId());
    }

    private Document createDocumentWithoutId() {
        return Document.builder()
                .title("Назва документа")
                .created(Instant.now())
                .author(new Author("1", "Автор"))
                .content("Вміст документа")
                .build();
    }

    @Test
    @DisplayName("When save a document it's id don't mutate")
    void whenSaveDocument_thenDocumentIdNotMutate() {
        Document documentForSave = createDocumentWithId();
        Document document = manager.save(documentForSave);

        assertEquals(documentForSave.getId(), document.getId());
    }

    private Document createDocumentWithId() {
        return Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Назва документа")
                .created(Instant.now())
                .author(new Author("1", "Автор"))
                .content("Вміст документа")
                .build();
    }

    @Test
    @DisplayName("When findById for saved document then return saved document in Optional")
    void whenFindDocumentById_thenReturnDocument() {
        Document documentForSave = createDocumentWithId();
        manager.save(documentForSave);

        Optional<Document> optionalDocument = manager.findById(documentForSave.getId());
        assertTrue(optionalDocument.isPresent());
    }

    @Test
    @DisplayName("When findById for non-existent id then return empty Optional")
    void whenFindByIdForNonExistentId_thenReturnEmptyOptional() {
        Document documentForSave = createDocumentWithId();

        Optional<Document> optionalDocument = manager.findById(documentForSave.getId());
        assertFalse(optionalDocument.isPresent());
    }

    @Test
    @DisplayName("When SearchRequest is null then throw NPE")
    void whenSearchRequestIsNull_thenThrowNPE() {
        assertThrows(NullPointerException.class, () -> manager.search(null));
    }

    @Test
    @DisplayName("When search by title prefixes then four documents match")
    void whenSearchByTitle_thenReturnFourDocuments() {
        saveFiveDocuments();
        SearchRequest request = SearchRequest.builder()
                .titlePrefixes(List.of("Опис", "План"))
                .build();

        List<Document> documentList = manager.search(request);
        assertFalse(documentList.isEmpty());
        assertEquals(4, documentList.size());
    }

    @Test
    @DisplayName("When search by contents then two documents match")
    void whenSearchByContents_thenReturnTwoDocuments() {
        saveFiveDocuments();
        SearchRequest request = SearchRequest.builder()
                .containsContents(List.of("проект"))
                .build();

        List<Document> documentList = manager.search(request);
        assertFalse(documentList.isEmpty());
        assertEquals(2, documentList.size());
    }

    @Test
    @DisplayName("When search by authorIds then two documents match")
    void whenSearchByAuthorIds_thenReturnTwoDocuments() {
        saveFiveDocuments();
        SearchRequest request = SearchRequest.builder()
                .authorIds(List.of(DEVELOPER_ID))
                .build();

        List<Document> documentList = manager.search(request);
        assertFalse(documentList.isEmpty());
        assertEquals(2, documentList.size());
    }

    @Test
    @DisplayName("When search by createdFrom then three documents match")
    void whenSearchByCreatedFrom_thenReturnThreeDocuments() {
        saveFiveDocuments();
        SearchRequest request = SearchRequest.builder()
                .createdFrom(Instant.now().minus(Period.ofDays(15)))
                .build();

        List<Document> documentList = manager.search(request);
        assertFalse(documentList.isEmpty());
        assertEquals(3, documentList.size());
    }

    @Test
    @DisplayName("When search by createdTo then three documents match")
    void whenSearchByCreatedTo_thenReturnThreeDocuments() {
        saveFiveDocuments();
        SearchRequest request = SearchRequest.builder()
                .createdTo(Instant.now().minus(Period.ofDays(9)))
                .build();

        List<Document> documentList = manager.search(request);
        assertFalse(documentList.isEmpty());
        assertEquals(3, documentList.size());
    }

    @Test
    @DisplayName("When search by all SearchRequest fields then one document match")
    void whenSearchByAllFields_thenReturnOneDocument() {
        saveFiveDocuments();
        SearchRequest request = SearchRequest.builder()
                .titlePrefixes(List.of("Опис"))
                .containsContents(List.of("виконує"))
                .authorIds(List.of(DEVELOPER_ID))
                .createdFrom(Instant.now().minus(Period.ofDays(1)))
                .createdTo(Instant.now().plus(Period.ofDays(1)))
                .build();

        List<Document> documentList = manager.search(request);
        assertFalse(documentList.isEmpty());
        assertEquals(1, documentList.size());
    }

    private void saveFiveDocuments() {
        Author customer = new Author(generateAuthorId(), "Замовник");
        Author teamLead = new Author(generateAuthorId(), "Тімлід");
        Author developer = new Author(DEVELOPER_ID, "Розробник");

        Document document1 = Document.builder()
                .title("Опис проекта")
                .content("Проект призначений для поліпшення життя людей")
                .author(customer)
                .created(Instant.now().minus(Period.ofWeeks(4)))
                .build();
        manager.save(document1);

        Document document2 = Document.builder()
                .title("Технічне завдання")
                .content("Розробити проект для поліпшення життя людей")
                .author(customer)
                .created(Instant.now().minus(Period.ofWeeks(3)))
                .build();
        manager.save(document2);

        Document document3 = Document.builder()
                .title("План виконання проекта")
                .content("1) Розробка макету - 1 тиждень." +
                        "2) Додавання нових фіч - 4 тижні." +
                        "3) Тестування - 2 тижні." +
                        "4) Введення в експлуатацію - 1 тиждень.")
                .author(teamLead)
                .created(Instant.now().minus(Period.ofWeeks(2)))
                .build();
        manager.save(document3);

        Document document4 = Document.builder()
                .title("Опис фічі №1")
                .content("Виконує багато корисного")
                .author(developer)
                .created(Instant.now().minus(Period.ofWeeks(1)))
                .build();
        manager.save(document4);

        Document document5 = Document.builder()
                .title("Опис фічі №2")
                .content("Виконує багато корисного")
                .author(developer)
                .created(Instant.now())
                .build();
        manager.save(document5);
    }


    private String generateAuthorId() {
        return UUID.randomUUID().toString();
    }
}












