/**
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 */
package com.microsoft.office.plugin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.msopentech.odatajclient.engine.data.metadata.AbstractEdmMetadata;
import com.msopentech.odatajclient.engine.data.metadata.EdmType;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractEntityContainer;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractEntitySet;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractEntityType;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractNavigationProperty;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractProperty;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractSchema;
import com.msopentech.odatajclient.engine.data.metadata.edm.PropertyRef;
import com.msopentech.odatajclient.engine.utils.MetadataUtils;
import com.msopentech.odatajclient.engine.utils.NavigationPropertyBindingDetails;

public abstract class AbstractUtility {

    protected static final String FC_TARGET_PATH = "fcTargetPath";

    protected static final String FC_SOURCE_PATH = "fcSourcePath";

    protected static final String FC_KEEP_IN_CONTENT = "fcKeepInContent";

    protected static final String FC_CONTENT_KIND = "fcContentKind";

    protected static final String FC_NS_PREFIX = "fcNSPrefix";

    protected static final String FC_NS_URI = "fcNSURI";

    protected static final String TYPE_SUB_PKG = "types";

    protected final String basePackage;

    protected final String schemaName;

    protected final String namespace;

    /**
     * Maps schema to list of its entity types.
     */
    protected final Map<String, List<AbstractEntityType>> allEntityTypes =
            new HashMap<String, List<AbstractEntityType>>();

    public AbstractUtility(final String namespace, final String schemaName, final String basePackage) {
        this.basePackage = basePackage;
        this.schemaName = schemaName == null ? getNameFromNS(namespace) : schemaName;
        this.namespace = namespace;
    }

    /**
     * Gets {@link EdmType} by given expression.
     * 
     * @param metadata Metadata to find type expression.
     * @param expression Fully qualified type expression (as given in metadata).
     * @return {@link EdmType} object.
     */
    public abstract EdmType getEdmType(
            final AbstractEdmMetadata<?, ?, ?, ?, ?, ?, ?> metadata, final String expression);

    protected abstract AbstractEdmMetadata<?, ?, ?, ?, ?, ?, ?> getMetadata();

    protected abstract AbstractSchema<?, ?, ?, ?> getSchema();

    /**
     * Collects all entity types in metadata and stores them into {@link AbstractUtility#allEntityTypes}.
     */
    protected void collectEntityTypes() {
        for (AbstractSchema<?, ?, ?, ?> _schema : getMetadata().getSchemas()) {
            allEntityTypes.put(_schema.getNamespace(), new ArrayList<AbstractEntityType>(_schema.getEntityTypes()));
            if (StringUtils.isNotBlank(_schema.getAlias())) {
                allEntityTypes.put(_schema.getAlias(), new ArrayList<AbstractEntityType>(_schema.getEntityTypes()));
            }
        }
    }

    /**
     * Converts type expression (as described in metadata) to java type that will be generated.
     * Returned string may be used for sources generation or for class loading.
     * 
     * @param typeExpression Type expression.
     * @return String containing package and class name that will be generated for given OData type.
     */
    public String getJavaType(final String typeExpression) {
        final StringBuilder res = new StringBuilder();
        
        final boolean isCollection = typeExpression.startsWith("Collection(") && typeExpression.endsWith(")");
        
        String ns = typeExpression.substring(0, typeExpression.lastIndexOf('.'));
        if (isCollection) {
            ns = ns.replaceFirst("Collection\\(", "");
        }
        
        // namespace of first schema is base namespace; other ones will be subpackages of first
        List<?> schemas = getMetadata().getSchemas();
        String base = ((AbstractSchema<?, ?, ?, ?>)schemas.get(0)).getNamespace();
        
        // make this subpackage
        if (!ns.equals(base) && !ns.equals("Edm")) {
            ns = base + "." + ns;
        }

        final EdmType edmType = getEdmType(getMetadata(), typeExpression);

        if (edmType.isCollection() && !edmType.isEntityType()) {
            res.append("Collection<");
        }

        if ("Edm.Stream".equals(typeExpression)) {
            res.append(InputStream.class.getName());
        } else if (edmType.isSimpleType()) {
            res.append(edmType.getSimpleType().javaType().getSimpleName());
        } else if (edmType.isComplexType()) {
            res.append(basePackage).append('.').append(ns.toLowerCase()).append('.').
                    append(TYPE_SUB_PKG).append('.').append(capitalize(edmType.getComplexType().getName()));
        } else if (edmType.isEntityType()) {
            res.append(basePackage).append('.').append(ns.toLowerCase()).append('.').
                    append(TYPE_SUB_PKG).append('.').append("I" + capitalize(edmType.getEntityType().getName()));
        } else if (edmType.isEnumType()) {
            res.append(basePackage).append('.').append(ns.toLowerCase()).
                    append('.').append(TYPE_SUB_PKG).append('.').append(capitalize(edmType.getEnumType().getName()));
        } else {
            throw new IllegalArgumentException("Invalid type expression '" + typeExpression + "'");
        }

        if (edmType.isCollection()) {
            if (edmType.isEntityType()) {
                res.append("Collection");
            } else {
                res.append(">");
            }
        }

        return res.toString();
    }

    /**
     * Gets {@link EdmType} for type of given entity set.
     * 
     * @param entitySet {@link AbstractEntitySet} to get type.
     * @return {@link EdmType} for type of given entity set.
     */
    public EdmType getEdmType(final AbstractEntitySet entitySet) {
        return getEdmType(getMetadata(), entitySet.getEntityType());
    }

    /**
     * Gets entity key types for entity type of given entity set.
     * 
     * @param entitySet {@link AbstractEntitySet} to retrieve entity type for getting entity key types.
     * @return
     */
    public Map<String, String> getEntityKeyType(final AbstractEntitySet entitySet) {
        return getEntityKeyType(getEdmType(entitySet).getEntityType());
    }

    /**
     * Gets entity key types for given entity type.
     * 
     * @param entityType Entity type.
     * @return entity key types.
     */
    public Map<String, String> getEntityKeyType(final AbstractEntityType entityType) {
        AbstractEntityType baseType = entityType;
        while (baseType.getKey() == null && baseType.getBaseType() != null) {
            baseType = getEdmType(getMetadata(), baseType.getBaseType()).getEntityType();
        }

        final List<String> properties = new ArrayList<String>();
        for (PropertyRef pref : baseType.getKey().getPropertyRefs()) {
            properties.add(pref.getName());
        }
        final Map<String, String> res = new HashMap<String, String>();

        for (AbstractProperty prop : baseType.getProperties()) {
            if (properties.contains(prop.getName())) {
                res.put(prop.getName(), getJavaType(prop.getType()));
            }
        }
        return res;
    }

    /**
     * Appends namespace to the beginning of given name.
     * 
     * @param name Name where namespace will be appended.
     * @return namespace + "." + name.
     */
    public final String getNameInNamespace(final String name) {
        return getSchema().getNamespace() + "." + name;
    }

    /**
     * Gets OData type for given entity type.
     * 
     * @param entityType Entity type.
     * @return OData type (with namespace).
     */
    public final String getNameInNamespace(final EdmType entityType) {
        return entityType.getNamespaceOrAlias() + "." + entityType.getEntityType().getName();
    }

    /**
     * Checks if two given type expressions describe the same OData type.
     * 
     * @param entityTypeExpression first type expression (with or without namespace).
     * @param fullTypeExpression Full type expression.
     * @param collection Collection marker.
     * @return <tt>true</tt> if two types are the same; <tt>false</tt> otherwise.
     */
    public boolean isSameType(
            final String entityTypeExpression, final String fullTypeExpression, final boolean collection) {

        final Set<String> types = new HashSet<String>(2);

        types.add((collection ? "Collection(" : StringUtils.EMPTY)
                + getNameInNamespace(entityTypeExpression)
                + (collection ? ")" : StringUtils.EMPTY));
        if (StringUtils.isNotBlank(getSchema().getAlias())) {
            types.add((collection ? "Collection(" : StringUtils.EMPTY)
                    + getSchema().getAlias() + "." + entityTypeExpression
                    + (collection ? ")" : StringUtils.EMPTY));
        }

        return types.contains(fullTypeExpression);
    }

    /**
     * Gets all descendants of given base type.
     * 
     * @param base Base type.
     * @param descendants The descendants list (out parameter).
     */
    private void populateDescendants(final EdmType base, final List<String> descendants) {
        for (Map.Entry<String, List<AbstractEntityType>> entry : allEntityTypes.entrySet()) {
            for (AbstractEntityType type : entry.getValue()) {
                if (StringUtils.isNotBlank(type.getBaseType())
                        && base.getEntityType().getName().equals(getNameFromNS(type.getBaseType()))) {

                    final EdmType entityType = getEdmType(getMetadata(), entry.getKey() + "." + type.getName());

                    descendants.add(getNameInNamespace(entityType));
                    populateDescendants(entityType, descendants);
                }
            }
        }
    }

    /**
     * Returns OData names of given entity type and its descendants.
     * 
     * @param entityType Source type.
     * @return OData names of given entity type and its descendants.
     */
    public List<String> getDescendantsOrSelf(final EdmType entityType) {
        final List<String> descendants = new ArrayList<String>();

        descendants.add(getNameInNamespace(entityType));
        populateDescendants(entityType, descendants);

        return descendants;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String capitalize(final String str) {
        return StringUtils.capitalize(str);
    }

    public String uncapitalize(final String str) {
        return StringUtils.uncapitalize(str);
    }

    public Map<String, String> getFcProperties(final AbstractProperty property) {
        final Map<String, String> fcProps = new HashMap<String, String>();

        if (StringUtils.isNotBlank(property.getFcTargetPath())) {
            fcProps.put(FC_TARGET_PATH, property.getFcTargetPath());
        }
        if (StringUtils.isNotBlank(property.getFcSourcePath())) {
            fcProps.put(FC_SOURCE_PATH, property.getFcSourcePath());
        }
        if (StringUtils.isNotBlank(property.getFcNSPrefix())) {
            fcProps.put(FC_NS_PREFIX, property.getFcNSPrefix());
        }
        if (StringUtils.isNotBlank(property.getFcNSURI())) {
            fcProps.put(FC_NS_URI, property.getFcNSURI());
        }
        fcProps.put(FC_CONTENT_KIND, property.getFcContentKind().name());
        fcProps.put(FC_KEEP_IN_CONTENT, Boolean.toString(property.isFcKeepInContent()));

        return fcProps;
    }

    /**
     * Extracts type name from fully qualified name.
     * 
     * @param ns Fully qualified name.
     * @return OData type name.
     */
    public final String getNameFromNS(final String ns) {
        return getNameFromNS(ns, false);
    }

    /**
     * Extracts type name from fully qualified name.
     * 
     * @param ns Fully qualified name.
     * @param toLowerCase indicates should the result will be lowercased.
     * @return OData type name.
     */
    public final String getNameFromNS(final String ns, final boolean toLowerCase) {
        if (ns == null) {
            return null;
        }
        final int lastpt = ns.lastIndexOf('.');
        String res = ns.substring(lastpt < 0 ? 0 : lastpt + 1);
        if (res.endsWith(")")) {
            // if name like Collection(NameSpace.Type)
            res = res.substring(0, res.length() - 1);
        }
        return toLowerCase ? res.toLowerCase() : res;
    }

    /**
     * Gets navigation binding details.
     * 
     * @param property Navigation property to be included into result.
     * @param targetPath Path to target entity type.
     * @param bindingInfoSchema Schema to be used if it is not specified in targetPath.
     * @param bindingInfoContainer Container to be used if it is not specified in targetPath.
     * @return {@link NavigationPropertyBindingDetails} instance containing all necessary data.
     */
    protected NavigationPropertyBindingDetails getNavigationBindingDetails(
            final AbstractNavigationProperty property,
            final String targetPath,
            final AbstractSchema<?, ?, ?, ?> bindingInfoSchema,
            final AbstractEntityContainer<?> bindingInfoContainer) {

        final String[] target = targetPath.split("/");
        final AbstractEntityContainer<?> targetContainer;
        final AbstractSchema<?, ?, ?, ?> targetSchema;
        final AbstractEntitySet targetES;

        if (target.length > 1) {
            int lastDot = target[0].lastIndexOf(".");
            final String targetSchemaNamespace = target[0].substring(0, lastDot);
            final String containerName = target[0].substring(lastDot + 1);

            targetSchema = MetadataUtils.getSchemaByNamespaceOrAlias(getMetadata(), targetSchemaNamespace);
            targetContainer = MetadataUtils.getContainerByName(targetSchema, containerName);
            targetES = MetadataUtils.getEntitySet(targetContainer, target[1]);
        } else {
            targetContainer = bindingInfoContainer;
            targetSchema = bindingInfoSchema;
            targetES = MetadataUtils.getEntitySet(targetContainer, target[0]);
        }
        return new NavigationPropertyBindingDetails(property, targetES, targetContainer, targetSchema);
    }

    /**
     * Gets return type of navigation property.
     * 
     * @param property Navigation property to check return type.
     * @return Navigation property return type (in OData format).
     */
    public abstract String getNavigationType(final AbstractNavigationProperty property);
}
