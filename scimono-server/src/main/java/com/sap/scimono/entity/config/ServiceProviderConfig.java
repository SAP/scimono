
package com.sap.scimono.entity.config;

import static com.sap.scimono.entity.definition.ResourceConstants.META_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.SCHEMAS_FIELD;
import static java.util.Objects.hash;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.schema.validation.ValidCoreSchema;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServiceProviderConfig extends Resource<ServiceProviderConfig> {

  public static final String RESOURCE_TYPE_SP_CONFIG = "ServiceProviderConfig";
  public static final String SCHEMA = CORE_SCHEMA + RESOURCE_TYPE_SP_CONFIG;
  private static final long serialVersionUID = 7128622808969772612L;

  @JsonProperty
  private final String documentationUri;
  @JsonProperty
  private final PagingSetting paging;
  @JsonProperty(required = true)
  private final ProviderConfigSetting patch;
  @JsonProperty(required = true)
  private final ProviderConfigSetting changePassword;
  @JsonProperty(required = true)
  private final ProviderConfigSetting sort;
  @JsonProperty(required = true)
  private final ProviderConfigSetting etag;
  @JsonProperty(required = true)
  private final FilterSetting filter;
  @JsonProperty(required = true)
  private final BulkSetting bulk;
  @JsonProperty(required = true)
  private final List<AuthenticationSchemeSetting> authenticationSchemes;

  public ServiceProviderConfig(@JsonProperty(META_FIELD) Meta meta, @JsonProperty(SCHEMAS_FIELD) Set<String> schemas,
      @JsonProperty("documentationUri") String documentationUri, @JsonProperty("paging") PagingSetting paging,
      @JsonProperty(value = "patch", required = true) ProviderConfigSetting patch,
      @JsonProperty(value = "changePassword", required = true) ProviderConfigSetting changePassword,
      @JsonProperty(value = "sort", required = true) ProviderConfigSetting sort,
      @JsonProperty(value = "etag", required = true) ProviderConfigSetting etag,
      @JsonProperty(value = "filter", required = true) FilterSetting filter,
      @JsonProperty(value = "bulk", required = true) BulkSetting bulk,
      @JsonProperty(value = "authenticationSchemes", required = true) List<AuthenticationSchemeSetting> authenticationSchemes) {
    super(null, null, meta, schemas);
    this.documentationUri = documentationUri;
    this.patch = patch;
    this.changePassword = changePassword;
    this.sort = sort;
    this.etag = etag;
    this.filter = filter;
    this.paging = paging;
    this.bulk = bulk;
    this.authenticationSchemes = authenticationSchemes;
  }

  private ServiceProviderConfig(final Builder builder) {
    super(builder);
    documentationUri = builder.documentationUri;
    patch = new ProviderConfigSetting(builder.patchSupported);
    changePassword = new ProviderConfigSetting(builder.changePasswordSupported);
    sort = new ProviderConfigSetting(builder.sortSupported);
    etag = new ProviderConfigSetting(builder.etagSupported);
    filter = builder.filter;
    paging = builder.paging;
    bulk = builder.bulk;
    authenticationSchemes = builder.authenticationSchemes;
  }

  @Override
  @ValidCoreSchema(SCHEMA)
  public Set<String> getSchemas() {
    return super.getSchemas();
  }

  public String getDocumentationUri() {
    return documentationUri;
  }

  public ProviderConfigSetting getPatch() {
    return patch;
  }

  public ProviderConfigSetting getChangePassword() {
    return changePassword;
  }

  public ProviderConfigSetting getSort() {
    return sort;
  }

  public ProviderConfigSetting getEtag() {
    return etag;
  }

  public FilterSetting getFilter() {
    return filter;
  }

  public PagingSetting getPaging() {
    return paging;
  }

  public BulkSetting getBulk() {
    return bulk;
  }

  public List<AuthenticationSchemeSetting> getAuthenticationSchemes() {
    return authenticationSchemes;
  }

  @Override
  public Builder builder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + hash(authenticationSchemes);
    result = prime * result + hash(bulk);
    result = prime * result + hash(changePassword);
    result = prime * result + hash(documentationUri);
    result = prime * result + hash(etag);
    result = prime * result + hash(filter);
    result = prime * result + hash(paging);
    result = prime * result + hash(patch);
    result = prime * result + hash(sort);

    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof ServiceProviderConfig)) {
      return false;
    }
    ServiceProviderConfig other = (ServiceProviderConfig) obj;
    if (!Objects.equals(authenticationSchemes, other.authenticationSchemes)) {
      return false;
    }
    if (!Objects.equals(bulk, other.bulk)) {
      return false;
    }
    if (!Objects.equals(changePassword, other.changePassword)) {
      return false;
    }
    if (!Objects.equals(documentationUri, other.documentationUri)) {
      return false;
    }
    if (!Objects.equals(etag, other.etag)) {
      return false;
    }
    if (!Objects.equals(filter, other.filter)) {
      return false;
    }
    if (!Objects.equals(paging, other.paging)) {
      return false;
    }
    if (!Objects.equals(patch, other.patch)) {
      return false;
    }
    return Objects.equals(sort, other.sort);
  }

  /**
   * Builder class that is used to build {@link ServiceProviderConfig} instances
   */
  public static class Builder extends Resource.Builder<ServiceProviderConfig> {

    private String documentationUri;
    private boolean patchSupported;
    private boolean changePasswordSupported;
    private boolean sortSupported;
    private boolean etagSupported;
    private FilterSetting filter;
    private PagingSetting paging;
    private BulkSetting bulk;
    private final List<AuthenticationSchemeSetting> authenticationSchemes = new ArrayList<>();

    Builder(final ServiceProviderConfig resource) {
      super(resource);
    }

    public Builder() {
      this(null);
      addSchema(SCHEMA);
    }

    @Override
    public Builder setId(final String id) {
      super.setId(id);
      return this;
    }

    @Override
    public Builder setExternalId(final String externalId) {
      super.setExternalId(externalId);
      return this;
    }

    @Override
    public Builder setMeta(final Meta meta) {
      super.setMeta(meta);
      return this;
    }

    public Builder documentationUri(final String documentationUri) {
      this.documentationUri = documentationUri;
      return this;
    }

    public Builder patchSupported(final boolean patchSupported) {
      this.patchSupported = patchSupported;
      return this;
    }

    public Builder changePasswordSupported(final boolean changePasswordSupported) {
      this.changePasswordSupported = changePasswordSupported;
      return this;
    }

    public Builder sortSupported(final boolean sortSupported) {
      this.sortSupported = sortSupported;
      return this;
    }

    public Builder etagSupported(final boolean etagSupported) {
      this.etagSupported = etagSupported;
      return this;
    }

    public Builder filter(final FilterSetting filter) {
      this.filter = filter;
      return this;
    }

    public Builder paging(final PagingSetting paging) {
      this.paging = paging;
      return this;
    }

    public Builder bulk(final BulkSetting bulk) {
      this.bulk = bulk;
      return this;
    }

    public Builder addAuthenticationScheme(final AuthenticationSchemeSetting authScheme) {
      authenticationSchemes.add(authScheme);
      return this;
    }

    public Builder removeAuthenticationScheme(final AuthenticationSchemeSetting authScheme) {
      authenticationSchemes.remove(authScheme);
      return this;
    }

    public Builder removeAuthenticationSchemes() {
      authenticationSchemes.clear();
      return this;
    }

    @Override
    public ServiceProviderConfig build() {
      return new ServiceProviderConfig(this);
    }

  }

}
