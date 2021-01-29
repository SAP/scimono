
package com.sap.scimono.helper;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.Resource.Builder;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UnnecessarySchemasEraser<T extends Resource<T>> {

  public T eraseAllUnnecessarySchemas(final T resource, final String coreSchema) {

    T resourceWithValidExtensions = removeInvalidExtensions(resource, coreSchema);

    Set<String> schemasWithProvidedAttributes = resourceWithValidExtensions.getExtensions().keySet();
    Predicate<String> coreSchemaOrCustomSchemaWithProvidedAttributes = schema -> schema.equals(coreSchema)
        || schemasWithProvidedAttributes.contains(schema);

    // @formatter:off
    Set<String> necessarySchemas = resourceWithValidExtensions.getSchemas().stream()
      .filter(coreSchemaOrCustomSchemaWithProvidedAttributes)
      .collect(Collectors.toSet());
    // @formatter:on

    return resourceWithValidExtensions.builder().setSchemas(necessarySchemas).build();
  }

  private T removeInvalidExtensions(final T resource, final String coreSchema) {
    Builder<T> builder = resource.builder();
 // @formatter:off
    resource.getExtensions().keySet().stream()
      .filter(SchemasCallback::isCoreSchema)
      .filter(schema -> !schema.equals(coreSchema))
      .forEach(builder::removeExtension);
    // @formatter:on
    return builder.build();
  }
}
