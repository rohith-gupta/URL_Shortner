package com.urlshortener.repository;

import com.urlshortener.entity.UrlMapping;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the repository against the real (H2, test-scope) schema. Also serves as a
 * schema/mapping validation: if {@link UrlMapping}'s column mappings didn't match the schema
 * Hibernate generates here, these tests would fail at context startup, not just at assertion
 * time.
 */
@DataJpaTest
class UrlMappingRepositoryTest {

    @Autowired
    private UrlMappingRepository repository;

    @Test
    void savesAndReadsBackAllFields() {
        UrlMapping saved = repository.saveAndFlush(new UrlMapping("https://example.com/page", "abc1234"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOriginalUrl()).isEqualTo("https://example.com/page");
        assertThat(saved.getShortCode()).isEqualTo("abc1234");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getClickCount()).isZero();
    }

    @Test
    void existsByShortCode_reflectsPersistedState() {
        repository.saveAndFlush(new UrlMapping("https://example.com", "present"));

        assertThat(repository.existsByShortCode("present")).isTrue();
        assertThat(repository.existsByShortCode("missing")).isFalse();
    }

    @Test
    void shortCodeUniqueConstraint_rejectsDuplicates() {
        repository.saveAndFlush(new UrlMapping("https://example.com/one", "dupecod"));

        assertThatThrownBy(() ->
                repository.saveAndFlush(new UrlMapping("https://example.com/two", "dupecod"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByShortCode_returnsMapping_orEmptyWhenMissing() {
        repository.saveAndFlush(new UrlMapping("https://example.com/found", "findme1"));

        assertThat(repository.findByShortCode("findme1")).isPresent()
                .get().extracting(UrlMapping::getOriginalUrl).isEqualTo("https://example.com/found");
        assertThat(repository.findByShortCode("nope0001")).isEmpty();
    }

    @Test
    void incrementClickCount_incrementsAtomicallyAndRepeatedly() {
        repository.saveAndFlush(new UrlMapping("https://example.com", "clicked"));

        int rowsAffected = repository.incrementClickCount("clicked");
        assertThat(rowsAffected).isEqualTo(1);
        assertThat(repository.findByShortCode("clicked").orElseThrow().getClickCount()).isEqualTo(1);

        repository.incrementClickCount("clicked");
        repository.incrementClickCount("clicked");
        assertThat(repository.findByShortCode("clicked").orElseThrow().getClickCount()).isEqualTo(3);
    }

    @Test
    void incrementClickCount_unknownShortCode_affectsNoRows() {
        assertThat(repository.incrementClickCount("nosuch1")).isZero();
    }
}
