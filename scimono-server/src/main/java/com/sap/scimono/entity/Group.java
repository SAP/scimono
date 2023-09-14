/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2013-2016 tarent solutions GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.sap.scimono.entity;

import static com.sap.scimono.entity.definition.CoreGroupAttributes.Constants.MEMBERS_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.DISPLAY_NAME_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.EXTERNAL_ID_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.ID_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.META_FIELD;
import static com.sap.scimono.entity.definition.ResourceConstants.SCHEMAS_FIELD;
import static java.util.Objects.hash;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.validation.ValidCoreSchema;
import com.sap.scimono.helper.Strings;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * This class represent a Group resource.
 * <p>
 * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-8">SCIM core schema
 * 2.0, sections 8</a>
 * </p>
 * <p>
 * client info: The scim schema is mainly meant as a connection link between the OSIAM server and by a client like the connector4Java. Some values
 * will be not accepted by the OSIAM server. These specific values have an own client info documentation section.
 * </p>
 */
@JsonInclude(Include.NON_EMPTY)
//TODO check with original
// @JsonFilter("attributeFilter")
public final class Group extends Resource<Group> {

  public static final String RESOURCE_TYPE_GROUP = "Group";
  public static final String SCHEMA = CORE_SCHEMA + RESOURCE_TYPE_GROUP;
  private static final long serialVersionUID = -2995603177584656028L;

  private final String displayName;
  @Valid
  private final Set<MemberRef> members;

  @JsonCreator
  private Group(@JsonProperty(ID_FIELD) final String id, @JsonProperty(EXTERNAL_ID_FIELD) final String externalId, @JsonProperty(META_FIELD) final Meta meta,
      @JsonProperty(value = SCHEMAS_FIELD, required = true) final Set<String> schemas, @JsonProperty(value = DISPLAY_NAME_FIELD, required = true) final String displayName,
      @JsonProperty(MEMBERS_FIELD) final LinkedHashSet<MemberRef> members) {
    super(id, externalId, meta, schemas);
    this.displayName = displayName;
    this.members = members != null ? members : new LinkedHashSet<>();
  }

  private Group(final Builder builder) {
    super(builder);
    displayName = builder.displayName;
    members = builder.members;
  }

  /**
   * Gets the human readable name of this {@link Group}.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Gets the list of members of this Group.
   * <p/>
   * <p>
   * For more detailed information please look at the <a href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-8">SCIM core schema
   * 2.0, sections 8</a>
   * </p>
   *
   * @return the list of Members as a Set
   */
  public Set<MemberRef> getMembers() {
    return Collections.unmodifiableSet(members);
  }

  public boolean containsMember(String memberId){
    return members.stream().anyMatch(member -> member.getValue().equals(memberId));
  }

  @Override
  public String toString() {
    Map<String, Object> valuesToDisplay = new LinkedHashMap<>();

    valuesToDisplay.put(DISPLAY_NAME_FIELD, displayName);
    valuesToDisplay.put(MEMBERS_FIELD, members);
    valuesToDisplay.put(ID_FIELD, getId());
    valuesToDisplay.put(EXTERNAL_ID_FIELD, getExternalId());
    valuesToDisplay.put(META_FIELD, getMeta());
    valuesToDisplay.put(SCHEMAS_FIELD, getSchemas());

    return Strings.createPrettyEntityString(valuesToDisplay, this.getClass());
  }

  @Override
  @ValidCoreSchema(SCHEMA)
  public Set<String> getSchemas() {
    return super.getSchemas();
  }

  @Override
  public Builder builder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + hash(displayName);
    result = prime * result + hash(members);
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
    if (!(obj instanceof Group)) {
      return false;
    }
    final Group other = (Group) obj;
    if (!Objects.equals(displayName, other.displayName)) {
      return false;
    }
    return Objects.equals(members, other.members);
  }

  /**
   * Builder class that is used to build {@link Group} instances
   */
  public static class Builder extends Resource.Builder<Group> {

    private String displayName;
    private Set<MemberRef> members = new LinkedHashSet<>();

    /**
     * creates a new Group.Builder based on the given displayName and group. All values of the given group will be copied expect the displayName will
     * be be overridden by the given one
     *
     * @param displayName the new displayName of the group
     * @param group a existing group
     */
    Builder(final String displayName, final Group group) {
      super(group);
      addSchema(SCHEMA);
      if (group != null) {
        this.displayName = group.displayName;
        members = new HashSet<>(group.members);
      }
      if (!Strings.isNullOrEmpty(displayName)) {
        this.displayName = displayName;
      }
    }

    /**
     * creates a new Group without a displayName
     */
    public Builder() {
      this(null, null);
    }

    /**
     * Constructs a new builder by copying all values from the given {@link Group}
     *
     * @param group {@link Group} to be copied from
     */
    public Builder(final Group group) {
      this(null, group);
    }

    /**
     * Constructs a new builder and sets the display name (See {@link Group#getDisplayName()}).
     *
     * @param displayName the display name
     */
    public Builder(final String displayName) {
      this(displayName, null);
    }

    @Override
    public Builder setId(final String id) {
      super.setId(id);
      return this;
    }

    @Override
    public Builder setMeta(final Meta meta) {
      super.setMeta(meta);
      return this;
    }

    @Override
    public Builder setExternalId(final String externalId) {
      super.setExternalId(externalId);
      return this;
    }

    /**
     * Sets the display name (See {@link Group#getDisplayName()}).
     *
     * @param displayName the display name of the {@link Group}
     * @return the builder itself
     */
    public Builder setDisplayName(final String displayName) {
      this.displayName = displayName;
      return this;
    }

    /**
     * Sets the list of members as {@link Set} (See {@link Group#getMembers()}).
     *
     * @param members the set of members
     * @return the builder itself
     */
    public Builder setMembers(final Set<MemberRef> members) {
      this.members = members != null ? new LinkedHashSet<>(members) : null;
      return this;
    }

    /**
     * Add the given member to the set of members.
     *
     * @param member The member to add.
     * @return The builder itself
     */
    public Builder addMember(final MemberRef member) {
      if (member != null && members.stream().noneMatch(m -> m.getValue().equals(member.getValue()))) {
        members.add(member);
      }
      return this;
    }

    public Builder addMembers(Collection<MemberRef> memberRefs){
      if (memberRefs != null) {
        for (final MemberRef memberRef : memberRefs) {
          addMember(memberRef);
        }
      }
      return this;
    }

    public Builder removeMembers() {
      members.clear();
      return this;
    }

    public Builder removeMember(MemberRef member){
      members.remove(member);
      return this;
    }

    public Builder addExtensions(Collection<Extension> extensions) {
      super.addExtensions(extensions);
      return this;
    }

    public Builder removeExtensions() {
      super.removeExtensions();
      return this;
    }

    @Override
    public Group build() {
      return new Group(this);
    }
  }
}
