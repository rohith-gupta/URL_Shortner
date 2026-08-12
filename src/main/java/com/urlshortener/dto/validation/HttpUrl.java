package com.urlshortener.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constrains a string to be a syntactically valid URL with an {@code http} or {@code https}
 * scheme (case-insensitive) and a non-empty host. Blank/null values are considered valid by
 * this constraint — pair with {@code @NotBlank} to require a value.
 *
 * <p>{@code @Target} deliberately includes FIELD, METHOD, and PARAMETER (not just FIELD): when
 * this annotation is placed on a record component, javac copies it to the backing field, the
 * accessor method, and the canonical constructor parameter, but only for the target kinds this
 * annotation actually declares — narrowing this would silently stop the constraint from being
 * enforced on record-based DTOs.
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HttpUrlValidator.class)
public @interface HttpUrl {

    String message() default "must be a valid http or https URL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
