
package com.sap.scimono.helper;

import static com.sap.scimono.entity.schema.AttributeDataType.COMPLEX;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.Manager;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.validation.patch.PatchValidationException;
import com.sap.scimono.exception.SCIMException;

public class ReadOnlyAttributesCleaner<T extends Resource<T>> {

  private SchemasCallback schemaAPI;

  public ReadOnlyAttributesCleaner(final SchemasCallback schemaAPI) {
    this.schemaAPI = schemaAPI;
  }

  public T clean(final T resource) {

    List<Extension> extensions = resource.getExtensions().values().stream().map(extension -> {
      if (extension instanceof EnterpriseExtension) {
        EnterpriseExtension enterpriseExtension = (EnterpriseExtension) extension;
        Manager managerWithoutDisplayName = new Manager.Builder(enterpriseExtension.getManager()).setDisplayName(null).build();
        return new EnterpriseExtension.Builder(enterpriseExtension).setManager(managerWithoutDisplayName).build();
      }
      Map<String, Object> attributes = extension.getAttributes();
      removeReadOnlyAttributes(schemaAPI.getSchema(extension.getUrn()).toAttribute(), attributes);
      return new Extension.Builder(extension).setAttributes(attributes).build();
    }).collect(Collectors.toList());

    return resource.builder().removeExtensions().addExtensions(extensions).build();
  }

  private boolean removeReadOnlyAttributes(final Attribute targetAttribute, final Object value) {
    if ("readOnly".equals(targetAttribute.getMutability())) {
      return true;
    }

    if (!COMPLEX.toString().equals(targetAttribute.getType())) {
      return false;
    }

    if (targetAttribute.isMultiValued()) {
      if (value instanceof Collection) {
        @SuppressWarnings("unchecked")
        Collection<Object> valueCollection = (Collection<Object>) value;

        // @formatter:off
        Attribute singleValuedAttribute = new Attribute.Builder()
            .name(targetAttribute.getName())
            .multiValued(false)
            .type(targetAttribute.getType())
            .mutability(targetAttribute.getMutability())
            .addSubAttributes(targetAttribute.getSubAttributes())
            .build();

        valueCollection.removeAll(valueCollection.stream()
            .filter(object -> removeReadOnlyAttributes(singleValuedAttribute, object))
            .collect(Collectors.toList()));
        // @formatter:on
      } else {
        throw new PatchValidationException(SCIMException.Type.INVALID_SYNTAX,
            String.format("Value attribute with name %s is array", targetAttribute.getName()));
      }
    }

    if (value instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> valueMap = (Map<String, Object>) value;

      for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
        // @formatter:off
        Attribute subAttribute = targetAttribute.getSubAttributes().stream()
            .filter(attribute -> entry.getKey().equalsIgnoreCase(attribute.getName()))
            .findAny()
            .orElseThrow(() -> new PatchValidationException(SCIMException.Type.INVALID_SYNTAX, String.format("Value attribute with name %s does not exist", entry.getKey())));
        // @formatter:on
        if (removeReadOnlyAttributes(subAttribute, entry.getValue())) {
          valueMap.remove(entry.getKey());
        }
      }
    }

    return false;
  }
}
