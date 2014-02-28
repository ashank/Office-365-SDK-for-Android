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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.msopentech.odatajclient.engine.data.metadata.AbstractEdmMetadata;
import com.msopentech.odatajclient.engine.data.metadata.EdmType;
import com.msopentech.odatajclient.engine.data.metadata.EdmV4Metadata;
import com.msopentech.odatajclient.engine.data.metadata.EdmV4Type;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractEntityContainer;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractEntitySet;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractNavigationProperty;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractSchema;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.Action;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.EntitySet;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.EntityType;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.Function;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.NavigationProperty;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.NavigationPropertyBinding;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.Schema;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.Singleton;
import com.msopentech.odatajclient.engine.utils.MetadataUtils;
import com.msopentech.odatajclient.engine.utils.NavigationPropertyBindingDetails;
import com.msopentech.odatajclient.engine.utils.QualifiedName;

public class Utility extends AbstractUtility {

    private final EdmV4Metadata metadata;

    private final Schema schema;

    public Utility(final EdmV4Metadata metadata, final Schema schema, final String basePackage) {
        super(schema.getNamespace(), schema.getAlias(), basePackage);
        this.metadata = metadata;
        this.schema = schema;

        collectEntityTypes();
    }

    @Override
    public EdmType getEdmType(final AbstractEdmMetadata<?, ?, ?, ?, ?, ?, ?> metadata, final String expression) {
        return new EdmV4Type((EdmV4Metadata) metadata, expression);
    }

    /**
     * Gets entity key type for given singleton.
     * 
     * @param singleton Singleton to get entity key type.
     * @return Entity key types.
     */
    public Map<String, String> getEntityKeyType(final Singleton singleton) {
        return getEntityKeyType(getEdmType(metadata, singleton.getType()).getEntityType());
    }
    
    /**
     * Gets edm type for entity type of given singleton.
     * 
     * @param singleton Singleton.
     * @return {@link EdmType} of entity type of given singleton.
     */
    public EdmType getEdmType(final Singleton singleton) {
        return getEdmType(getMetadata(), singleton.getType());
    }

    @Override
    protected EdmV4Metadata getMetadata() {
        return metadata;
    }

    @Override
    protected Schema getSchema() {
        return schema;
    }

    @Override
    public String getNavigationType(final AbstractNavigationProperty property) {
        return ((NavigationProperty) property).getType();
    }

    /**
     * Gets {@link NavigationPropertyBindingDetails} by given property and source type.
     * 
     * @param sourceEntityType Source entity type.
     * @param property navigation property.
     * @return {@link NavigationPropertyBindingDetails} instance.
     */
    public NavigationPropertyBindingDetails getNavigationBindingDetails(final String sourceEntityType, final AbstractNavigationProperty property) {

        NavigationPropertyBindingDetails bindingDetails = null;
        final List<Schema> schemas = getMetadata().getSchemas();
        for (int i = 0; bindingDetails == null && i < schemas.size(); i++) {
            final Schema sc = schemas.get(i);
            if (sc.getEntityContainer() != null) {
                bindingDetails = getNavigationBindingDetails(sc, sourceEntityType, property);
            }
        }
        return bindingDetails;
    }

    /**
     * Gets function from current schema by name.
     * 
     * @param name function name.
     * @return {@link Function} object.
     */
    public Function getFunctionByName(final String name) {
        final QualifiedName qname = new QualifiedName(name);

        final Schema targetSchema = (Schema) MetadataUtils.getSchemaByNamespaceOrAlias(metadata, qname.getNamespace());

        if (targetSchema != null) {
            for (Function function : targetSchema.getFunctions()) {
                if (function.getName().equals(qname.getName())) {
                    return function;
                }
            }
        }

        return null;
    }

    /**
     * Gets action from current schema by name.
     * 
     * @param name action name.
     * @return {@link Action} object.
     */
    public Action getActionByName(final String name) {
        final QualifiedName qname = new QualifiedName(name);

        final Schema targetSchema = (Schema) MetadataUtils.getSchemaByNamespaceOrAlias(metadata, qname.getNamespace());

        if (targetSchema != null) {
            for (Action action : targetSchema.getActions()) {
                if (action.getName().equals(qname.getName())) {
                    return action;
                }
            }
        }

        return null;
    }

    
    /**
     * Gets list of functions where given type is bound to.
     * 
     * @param typeExpression Type expression.
     * @param collection Collection flag.
     * @return List of functions where given type is bound to
     */
    public List<Function> getFunctionsBoundTo(final String typeExpression, final boolean collection) {

        final List<Function> result = new ArrayList<Function>();

        for (Function function : schema.getFunctions()) {
            if (function.isBound()) {
                for (int i = 0; i < function.getParameters().size(); i++) {
                    if (isSameType(typeExpression, function.getParameters().get(i).getType(), collection)) {
                        result.add(function);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets list of actions where given type is bound to.
     * 
     * @param typeExpression Type expression.
     * @param collection Collection flag.
     * @return List of actions where given type is bound to
     */
    public List<Action> getActionsBoundTo(final String typeExpression, final boolean collection) {

        final List<Action> result = new ArrayList<Action>();

        for (Action action : schema.getActions()) {
            if (action.isBound()) {
                for (int i = 0; i < action.getParameters().size(); i++) {
                    if (isSameType(typeExpression, action.getParameters().get(i).getType(), collection)) {
                        result.add(action);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets {@link NavigationPropertyBindingDetails} for given navigation property using given parameters.
     * 
     * @param schema Schema where this navigation property is declared.
     * @param sourceEntityType Source entity type.
     * @param property Navigation property to extract binding details.
     * @return {@link NavigationPropertyBindingDetails} instance that describes given navigation property.
     */
    private NavigationPropertyBindingDetails getNavigationBindingDetails(final AbstractSchema<?, ?, ?, ?> schema, final String sourceEntityType,
            final AbstractNavigationProperty property) {

        for (AbstractEntityContainer<?> container : schema.getEntityContainers()) {
            for (AbstractEntitySet es : container.getEntitySets()) {
                if (es.getEntityType().equals(sourceEntityType)) {
                    final NavigationPropertyBinding binding = MetadataUtils.getNavigationBindingByPath(
                            ((EntitySet) es).getNavigationPropertyBindings(), property.getName());
                    if (binding != null) {
                        return getNavigationBindingDetails(property, binding.getTarget(), schema, container);
                    }
                }
            }
        }

        // implicit entity set as described in CSDL 7.1.5.
        if (((NavigationProperty) property).isContainsTarget()) {
            return new NavigationPropertyBindingDetails(property, 
                    getEntitySetFromNavigationProperty((NavigationProperty) property), schema.getDefaultEntityContainer(), schema);
        }
        
        // no entity set, ordinal collection
        return new NavigationPropertyBindingDetails(property, null, schema.getDefaultEntityContainer(), schema);
    }
    
    /**
     * Gets {@link EntitySet} for given navigation property. 
     * 
     * @param property Property to construct entity set.
     * @return Entity set object with appropriate name and entity type.
     */
    private EntitySet getEntitySetFromNavigationProperty(NavigationProperty property) {
        if (!property.getType().startsWith("Collection(") || !property.getType().endsWith(")")) {
            return null;
        }
        
        final EntitySet entitySet = new EntitySet();
        
        entitySet.setEntityType(property.getType().substring(11, property.getType().length() - 1));
        entitySet.setName(property.getName());
        
        return entitySet;
    }

    /**
     * Checks if current schema contains {@link Singleton} with given name.
     * 
     * @param name Singleton name.
     * @return <tt>true</tt> if current schema contains singleton with given name, <tt>false</tt> otherwise.
     */
    public boolean isSingleton(String name) {
        for (Singleton singleton : schema.getEntityContainer().getSingletons()) {
            if (singleton.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Gets entity type by given type expression.
     * 
     * @param typeExpr Type expression.
     * @return Entity type found or <tt>null</tt>.
     */
    public EntityType getEntityTypeByName(String typeExpr) {
        if (typeExpr == null) {
            return null;
        }
        for (Schema schema: getMetadata().getSchemas()) {
            for (EntityType type: schema.getEntityTypes()) {
                if (typeExpr.equals(schema.getNamespace() + "." + type.getName())) {
                    return type;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Gets name of entity container for current schema.
     * 
     * @return Entity container name or <tt>null</tt> if no entity container was found.
     */
    public String getEntityContainerName() {
        for (Schema schema: getMetadata().getSchemas()) {
            if (schema.getEntityContainer() != null) {
                return schema.getEntityContainer().getName();
            }
        }
        
        return null;
    }
    
    /**
     * Gets java type for entity set that will be navigated to from given navigation property.
     * 
     * @param property Navigation property.
     * @return String that may be used in templates or for classes loading.
     */
    public String getEntitySetJavaType(NavigationProperty property) {
        StringBuilder builder = new StringBuilder(basePackage);
        builder.append(".");
        
        String ns = property.getType().substring(0, property.getType().lastIndexOf('.')).replaceFirst("Collection\\(", "");
        builder.append(ns.toLowerCase());
        builder.append(".I");
        
        String typeName = property.getType().substring(property.getType().lastIndexOf('.') + 1, property.getType().length() - 1);
        builder.append(typeName);
        
        return builder.append("s").toString();
    }
}
