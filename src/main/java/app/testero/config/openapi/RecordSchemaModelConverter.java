package app.testero.config.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Fills in the {@code required} and {@code nullable} metadata that springdoc cannot infer for
 * records.
 *
 * <p>Out of the box only request DTOs get a {@code required} list, as a side effect of their
 * validation annotations; every response field comes out optional. Consumers generating types
 * from the spec (see testero-web#134) then have to treat everything as possibly absent.
 *
 * <p>No DTO uses {@code @JsonInclude}, so Jackson serialises every record component — including
 * the ones holding {@code null}. The accurate contract is therefore:
 *
 * <ul>
 *   <li>every component is <b>required</b> (it is always present in the payload);</li>
 *   <li>a component annotated {@link Nullable} is additionally <b>nullable</b> — present, but
 *       possibly null;</li>
 *   <li>a component explicitly annotated
 *       {@code @Schema(requiredMode = NOT_REQUIRED)} is left out of {@code required}. This is for
 *       request DTOs, where the client really may omit a field.</li>
 * </ul>
 *
 * <p>The intent lives on the record itself, so no per-field annotation is needed for the common
 * case.
 */
public class RecordSchemaModelConverter implements ModelConverter {

    private static final String REF_PREFIX = "#/components/schemas/";

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context,
                             Iterator<ModelConverter> chain) {
        Schema<?> resolved = chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
        if (resolved == null) {
            return null;
        }

        Class<?> raw = rawClassOf(type);
        if (raw == null || !raw.isRecord()) {
            return resolved;
        }

        Schema<?> model = modelOf(resolved, context);
        if (model != null && model.getProperties() != null) {
            applyRecordContract(raw, model);
        }
        return resolved;
    }

    /**
     * Rewrites the schema's {@code required} list from the record's components and marks the
     * nullable ones. The list follows declaration order, so the emitted spec is stable.
     */
    private void applyRecordContract(Class<?> record, Schema<?> model) {
        Map<String, ?> properties = model.getProperties();
        List<String> required = new ArrayList<>();

        for (RecordComponent component : record.getRecordComponents()) {
            String name = propertyName(component);
            if (!(properties.get(name) instanceof Schema<?> property)) {
                // Not serialised (e.g. @JsonIgnore) — nothing to declare.
                continue;
            }
            if (!isExplicitlyOptional(component)) {
                required.add(name);
            }
            if (isNullable(component)) {
                property.setNullable(true);
            }
        }

        model.setRequired(required.isEmpty() ? null : required);
    }

    /** The JSON name of a component, honouring {@code @JsonProperty} — the DTOs use it heavily. */
    private String propertyName(RecordComponent component) {
        JsonProperty jsonProperty = findAnnotation(component, JsonProperty.class);
        if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
            return jsonProperty.value();
        }
        return component.getName();
    }

    /**
     * True when the component carries JSpecify's {@link Nullable}. It is a {@code TYPE_USE}
     * annotation, so it lands on the component's annotated type rather than on the component.
     */
    private boolean isNullable(RecordComponent component) {
        if (component.getAnnotatedType().isAnnotationPresent(Nullable.class)) {
            return true;
        }
        return findAnnotation(component, Nullable.class) != null;
    }

    private boolean isExplicitlyOptional(RecordComponent component) {
        io.swagger.v3.oas.annotations.media.Schema schema =
                findAnnotation(component, io.swagger.v3.oas.annotations.media.Schema.class);
        return schema != null
                && schema.requiredMode()
                == io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
    }

    /**
     * Looks for an annotation wherever the compiler may have put it: an annotation written on a
     * record component is propagated to the field, the accessor and the constructor parameter
     * according to its own {@code @Target}, and is readable from the component itself only when
     * {@code RECORD_COMPONENT} is among those targets.
     */
    private <A extends Annotation> A findAnnotation(RecordComponent component, Class<A> type) {
        A onComponent = component.getAnnotation(type);
        if (onComponent != null) {
            return onComponent;
        }
        A onAccessor = component.getAccessor().getAnnotation(type);
        if (onAccessor != null) {
            return onAccessor;
        }
        return fieldAnnotation(component, type);
    }

    private <A extends Annotation> A fieldAnnotation(RecordComponent component, Class<A> type) {
        try {
            Field field = component.getDeclaringRecord().getDeclaredField(component.getName());
            return field.getAnnotation(type);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * A resolved record is normally returned as a {@code $ref}; the schema to enrich is the model
     * the chain registered in the context.
     */
    private Schema<?> modelOf(Schema<?> resolved, ModelConverterContext context) {
        String ref = resolved.get$ref();
        if (ref == null) {
            return resolved;
        }
        if (!ref.startsWith(REF_PREFIX)) {
            return null;
        }
        return context.getDefinedModels().get(ref.substring(REF_PREFIX.length()));
    }

    private Class<?> rawClassOf(AnnotatedType type) {
        if (type.getType() == null) {
            return null;
        }
        return Json.mapper().constructType(type.getType()).getRawClass();
    }
}
