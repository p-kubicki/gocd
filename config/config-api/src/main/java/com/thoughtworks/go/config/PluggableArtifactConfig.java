/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.config;

import com.google.gson.Gson;
import com.thoughtworks.go.config.validation.NameTypeValidator;
import com.thoughtworks.go.domain.ArtifactType;
import com.thoughtworks.go.domain.ConfigErrors;
import com.thoughtworks.go.domain.config.Configuration;
import com.thoughtworks.go.domain.config.ConfigurationProperty;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@ConfigCollection(value = ConfigurationProperty.class)
@AttributeAwareConfigTag(value = "artifact", attribute = "type", attributeValue = "external")
public class PluggableArtifactConfig extends Configuration implements ArtifactConfig {
    private final ConfigErrors errors = new ConfigErrors();

    @ConfigAttribute(value = "id", allowNull = true)
    protected String id;
    @ConfigAttribute(value = "storeId", allowNull = true)
    private String storeId;

    public PluggableArtifactConfig() {
    }

    public PluggableArtifactConfig(String id, String storeId, ConfigurationProperty... configurationProperties) {
        super(configurationProperties);
        this.id = id;
        this.storeId = storeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    @Override
    public ArtifactType getArtifactType() {
        return ArtifactType.plugin;
    }

    @Override
    public String getArtifactTypeValue() {
        return "Pluggable Artifact";
    }

    @Override
    public boolean validateTree(ValidationContext validationContext) {
        validate(validationContext);
        return !hasErrors();
    }

    @Override
    public void validate(ValidationContext validationContext) {
        validateMandatoryAttributes();
        if (hasErrors()) {
            return;
        }

        super.validateUniqueness(getArtifactTypeValue());
        if (!new NameTypeValidator().isNameValid(storeId)) {
            errors.add("storeId", NameTypeValidator.errorMessage("pluggable artifact storeId", storeId));
        }

        if (isNotBlank(storeId)) {
            final ArtifactStore artifactStore = validationContext.artifactStores().find(storeId);

            if (artifactStore == null) {
                addError("storeId", String.format("Artifact store with id `%s` does not exist.", storeId));
            }
        }

        if (!new NameTypeValidator().isNameValid(id)) {
            errors.add("id", NameTypeValidator.errorMessage("pluggable artifact id", id));
        }
    }

    private void validateMandatoryAttributes() {
        if (StringUtils.isBlank(this.id)) {
            errors.add("id", "\"Id\" is required for PluggableArtifact");
        }

        if (StringUtils.isBlank(this.storeId)) {
            errors.add("storeId", "\"Store id\" is required for PluggableArtifact");
        }
    }

    @Override
    public void validateUniqueness(List<ArtifactConfig> existingArtifactConfigList) {
        for (ArtifactConfig existingArtifactConfig : existingArtifactConfigList) {
            if (existingArtifactConfig instanceof PluggableArtifactConfig) {
                final PluggableArtifactConfig pluggableArtifactConfig = (PluggableArtifactConfig) existingArtifactConfig;

                if (this.getId().equalsIgnoreCase(pluggableArtifactConfig.getId())) {
                    this.addError("id", String.format("Duplicate pluggable artifacts  with id `%s` defined.", getId()));
                    existingArtifactConfig.addError("id", String.format("Duplicate pluggable artifacts  with id `%s` defined.", getId()));
                }
                if (this.getStoreId().equalsIgnoreCase(pluggableArtifactConfig.getStoreId())) {
                    if (this.size() == pluggableArtifactConfig.size() && this.containsAll(pluggableArtifactConfig)) {
                        this.addError("id", "Duplicate pluggable artifacts  configuration defined.");
                        existingArtifactConfig.addError("id", "Duplicate pluggable artifacts  configuration defined.");
                    }
                }
                return;
            }
        }
        existingArtifactConfigList.add(this);
    }

    public String toJSON() {
        final HashMap<String, Object> artifactStoreAsHashMap = new HashMap<>();
        artifactStoreAsHashMap.put("id", getId());
        artifactStoreAsHashMap.put("storeId", getStoreId());
        artifactStoreAsHashMap.put("configuration", getConfigurationAsMap(true));
        return new Gson().toJson(artifactStoreAsHashMap);
    }

    @Override
    public ConfigErrors errors() {
        return errors;
    }

    @Override
    public boolean hasErrors() {
        return super.hasErrors() || !errors.isEmpty();
    }

    @Override
    public void addError(String fieldName, String message) {
        errors.add(fieldName, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluggableArtifactConfig)) return false;
        if (!super.equals(o)) return false;

        PluggableArtifactConfig that = (PluggableArtifactConfig) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return storeId != null ? storeId.equals(that.storeId) : that.storeId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (storeId != null ? storeId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PluggableArtifactConfig{" +
                "id='" + id + '\'' +
                ", storeId='" + storeId + '\'' +
                '}';
    }
}
