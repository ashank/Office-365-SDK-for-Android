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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.msopentech.odatajclient.engine.client.ODataClientFactory;
import com.msopentech.odatajclient.engine.client.ODataV4Client;
import com.msopentech.odatajclient.engine.communication.request.retrieve.ODataMetadataRequest;
import com.msopentech.odatajclient.engine.communication.response.ODataRetrieveResponse;
import com.msopentech.odatajclient.engine.data.metadata.EdmV4Metadata;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.ComplexType;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.EntityContainer;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.EntitySet;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.EntityType;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.EnumType;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.NavigationProperty;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.Schema;
import com.msopentech.odatajclient.engine.data.metadata.edm.v4.Singleton;
import com.msopentech.odatajclient.engine.utils.ODataVersion;

/**
 * POJOs generator.
 */
@Mojo(name = "pojosV4", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MetadataMojo extends AbstractMetadataMojo {
    
    /**
     * Targets to be generated.
     */
    @Parameter(property = "generate", defaultValue = "interfaces,classes")
    private String generateTargets;

    @Override
    protected Utility getUtility() {
        return (Utility) utility;
    }

    @Override
    protected String getVersion() {
        return ODataVersion.V4.name().toLowerCase();
    }

    private static String AUTH_HEADER = "Basic b2RhdGExQENUU1Rlc3QuY2NzY3RwLm5ldDowN0FwcGxlcw==";
    
    private boolean generateInterfaces, generateClasses;

    @Override
    public void execute() throws MojoExecutionException {
        if (new File(outputDirectory + File.separator + TOOL_DIR).exists()) {
            getLog().info("Nothing to do because " + TOOL_DIR + " directory already exists. Clean to update.");
            return;
        }

        try {
            Velocity.addProperty(Velocity.RESOURCE_LOADER, "class");
            Velocity.addProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());

            generateInterfaces = generateTargets.contains("interfaces");
            generateClasses = generateTargets.contains("classes");
            
            ODataV4Client client = ODataClientFactory.getV4();
            
            final ODataMetadataRequest req = client.
                    getRetrieveRequestFactory().getMetadataRequest(serviceRootURL);

            req.addCustomHeader("Authorization", AUTH_HEADER);
            
            final ODataRetrieveResponse<EdmV4Metadata> res = req.execute();
            final EdmV4Metadata metadata = res.getBody();

            if (metadata == null) {
                throw new IllegalStateException("Metadata not found");
            }

            // first namespace is base; other will be subpackages of first
            // so "System" becomes "Microsoft.Exchange.Services.OData.Model.System"
            List<Schema> schemas = metadata.getSchemas();
            if (schemas.size() > 0) {
                String base = schemas.get(0).getNamespace();
                for (int i = 1; i < schemas.size(); ++i) {
                    schemas.get(i).setNamespace(base + "." + schemas.get(i).getNamespace());
                }
            }
            for (Schema schema : metadata.getSchemas()) {
                namespaces.add(schema.getNamespace().toLowerCase());
            }

            final Set<String> complexTypeNames = new HashSet<String>();
            final File services = mkdir("META-INF/services");

            for (Schema schema : metadata.getSchemas()) {
                utility = new Utility(metadata, schema, basePackage);

                // write package-info for the base package
                final String schemaPath = utility.getNamespace().toLowerCase().replace('.', File.separatorChar);
                final File base = mkPkgDir(schemaPath);
                final String pkg = basePackage + "." + utility.getNamespace().toLowerCase();
                parseObj(base, pkg, "package-info", "package-info.java");

                // write package-info for types package
                final File typesBaseDir = mkPkgDir(schemaPath + "/types");
                final String typesPkg = pkg + ".types";
                parseObj(typesBaseDir, typesPkg, "package-info", "package-info.java");

                final Map<String, Object> objs = new HashMap<String, Object>();

                if (generateInterfaces) {
                // write types into types package
                    for (ComplexType complex : schema.getComplexTypes()) {
                        final String className = utility.capitalize(complex.getName());
                        complexTypeNames.add(typesPkg + "." + className);
                        objs.clear();
                        objs.put("complexType", complex);
                        parseObj(typesBaseDir, typesPkg, "complexType", className + ".java", objs);
                    }

                    for (EnumType enumType : schema.getEnumTypes()) {
                        final String className = utility.capitalize(enumType.getName());
                        complexTypeNames.add(typesPkg + "." + className);
                        objs.clear();
                        objs.put("enumType", enumType);
                        parseObj(typesBaseDir, typesPkg, "enumType", className + ".java", objs);
                    }
                }

                for (EntityType entity : schema.getEntityTypes()) {
                    try {
                        objs.clear();
                        objs.put("entityType", entity);

                        final Map<String, String> keys;

                        EntityType baseType = null;
                        if (entity.getBaseType() == null) {
                            keys = utility.getEntityKeyType(entity);
                        } else {
                            baseType = schema.getEntityType(utility.getNameFromNS(entity.getBaseType()));
                            objs.put("baseType", utility.getJavaType(entity.getBaseType()));
                            while (baseType.getBaseType() != null) {
                                baseType = schema.getEntityType(utility.getNameFromNS(baseType.getBaseType()));
                            }
                            keys = utility.getEntityKeyType(baseType);
                        }

                        if (keys.size() > 1) {
                            // create compound key class
                        final String keyClassName = utility.capitalize(baseType == null
                                ? entity.getName()
                                : baseType.getName()) + "Key";
                            objs.put("keyRef", keyClassName);

                            if (entity.getBaseType() == null) {
                                objs.put("keys", keys);
                                parseObj(typesBaseDir, typesPkg, "entityTypeKey", keyClassName + ".java", objs);
                            }
                        }

                        if (generateInterfaces) {
                            parseObj(typesBaseDir, typesPkg, "entityType",
                                    "I" + utility.capitalize(entity.getName()) + ".java", objs);
                            parseObj(typesBaseDir, typesPkg,
                                    "entityCollection", "I" + utility.capitalize(entity.getName()) + "Collection.java", objs);
                        }
                        if (generateClasses) {
                            parseObj(typesBaseDir, typesPkg, "entityTypeImpl",
                                    utility.capitalize(entity.getName()) + ".java", objs);
                            parseObj(typesBaseDir, typesPkg, "entityCollectionImpl",
                                    utility.capitalize(entity.getName()) + "Collection.java", objs);
                        }
                    } catch (Exception e) {
                        getLog().error(e.getMessage());
                        if (e instanceof NullPointerException) {
                            final StringWriter stringWriter = new StringWriter();
                            final PrintWriter printWriter = new PrintWriter(stringWriter);
                            e.printStackTrace(printWriter);
                            getLog().error(stringWriter.toString());
                        }
                        continue;
                    }
                }

                List<EntitySet> additionalSets = new ArrayList<EntitySet>();
                
                // generate non-top level entity sets that accessible via other entity sets or singletons
                for (EntityContainer container: schema.getEntityContainers()) {
                    Map<String, String> paths = new HashMap<String, String>();
                    Map<String, Boolean> generatedEntitySets = new HashMap<String, Boolean>();
                    Map<String, String> entitySetNames = new HashMap<String, String>();
                    Collection<String> containerEntitySets = getEntitySetTypes(container);
                    for (EntityType type : schema.getEntityTypes()) {
                        generatedEntitySets.put(type.getName(), containerEntitySets.contains(type.getName()));
                    }

                    for (Singleton singleton: container.getSingletons()) {
                        String type = singleton.getType();
                        if (!paths.containsKey(utility.getNameFromNS(type))) {
                            paths.put(utility.getNameFromNS(type), "");
                        }
                        entitySetNames.put(utility.getNameFromNS(type), singleton.getName());
                        generateEntitySet(schema, base, pkg, objs, generatedEntitySets, entitySetNames, type, paths, additionalSets);
                    }
                    
                    for (EntitySet set : container.getEntitySets()) {
                        String type = set.getEntityType();
                        if (!paths.containsKey(utility.getNameFromNS(type))) {
                            paths.put(utility.getNameFromNS(type), "");
                        }
                        entitySetNames.put(utility.getNameFromNS(type), set.getName());
                        generateEntitySet(schema, base, pkg, objs, generatedEntitySets, entitySetNames, type, paths, additionalSets);
                    }
                    
                }
                
                // write container and top entity sets into the base package
                for (EntityContainer container : schema.getEntityContainers()) {

                    for (EntitySet entitySet : container.getEntitySets()) {
                        objs.clear();
                        objs.put("entitySet", entitySet);
                        if (generateInterfaces) {
                            parseObj(base, pkg, "entitySet", "I" + utility.capitalize(entitySet.getName()) + ".java", objs);
                        }
                        if (generateClasses) {
                            parseObj(base, pkg, "entitySetImpl", utility.capitalize(entitySet.getName()) + ".java", objs);
                        }
                    }

                    for (EntitySet set : additionalSets) {
                        container.getEntitySets().add(set);
                    }
                    
                    if (generateClasses) {
                        for (Singleton singleton: container.getSingletons()) {
                            objs.clear();
                            objs.put("singleton", singleton);
                            parseObj(base, pkg, "singleton", utility.capitalize(singleton.getName()) + ".java", objs);
                        }
                    }
                    
                    if (generateInterfaces) {
                        objs.clear();
                        objs.put("container", container);
                        parseObj(base, pkg, "container", utility.capitalize(container.getName()) + ".java", objs);
                    }
                }
                
                // default folders enumeration
                if (!pkg.endsWith("system") && generateInterfaces) {
                    objs.clear();
                    objs.put("userType", schema.getEntityType("User"));
                    parseObj(base, pkg, "defaultFolder", "DefaultFolder.java", objs);
                }
                
                parseObj(services, true, null, "services", "com.msopentech.odatajclient.proxy.api.AbstractComplexType",
                        Collections.singletonMap("services", (Object) complexTypeNames));
            }
        } catch (Throwable t) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);
            t.printStackTrace(printWriter);
            getLog().error(stringWriter.toString());
            
            throw (t instanceof MojoExecutionException)
                    ? (MojoExecutionException) t
                    : new MojoExecutionException("While executin mojo", t);
        }
    }

    /**
     * Generates a single entity set.
     * 
     * @param schema Schema which an entity set will be related to.
     * @param base Base directory for output classes.
     * @param pkg Package.
     * @param objs Objects to be put into velocity context.
     * @param generatedEntitySets Holds information if entity set for some type already generated to prevent multiple generations.
     * @param entitySetNames Maps entity type and its set name.
     * @param type Fully qualified enity type (contains schema namespace and class name).
     * @param paths Maps entity type and path to its set related to service root.
     * @param additionalSets List of all sets were generated.
     * @throws MojoExecutionException
     */
    private void generateEntitySet(Schema schema,
                                   final File base,
                                   final String pkg,
                                   final Map<String, Object> objs,
                                   Map<String, Boolean> generatedEntitySets,
                                   Map<String, String> entitySetNames,
                                   String type,
                                   Map<String, String> paths,
                                   List<EntitySet> additionalSets)
                    throws MojoExecutionException {
        Queue<String> typesQueue = new LinkedList<String>();

        for (NavigationProperty np : schema.getEntityType(utility.getNameFromNS(type)).getNavigationProperties()) {
            addTypeToQueue(entitySetNames, type, paths, typesQueue, np);
        }

        while (!typesQueue.isEmpty()) {
            String currentType = typesQueue.poll();
            if (generatedEntitySets.get(currentType)) {
                continue;
            }

            EntitySet generatedSet = new EntitySet();
            generatedSet.setEntityType(utility.getNameInNamespace(currentType));
            String name = entitySetNames.get(currentType);
            generatedSet.setName(name);
            additionalSets.add(generatedSet);

            objs.clear();
            objs.put("entitySet", generatedSet);

            EntityType currentEntityType = schema.getEntityType(currentType);
            while (true) {
                for (NavigationProperty np : currentEntityType.getNavigationProperties()) {
                    addTypeToQueue(entitySetNames, currentType, paths, typesQueue, np);
                }
                if (currentEntityType.getBaseType() != null) {
                    currentEntityType = schema.getEntityType(utility.getNameFromNS(currentEntityType.getBaseType()));
                } else {
                    break;
                }
            }

            /******************************* EXCHANGE-SPECIFIC ******************************************/
            // As we know from spec we cannot directly create a message inside /Me/Messages
            // we must create it inside /Me/path/to/some/folder/Messages
            // the path may be one of:
            // 1. Predefined folder name - as described in metadata in navigation properties of User entity
            // example: Inbox -> /Me/Inbox/Messages
            // 2. Folder with given id
            // example: Folders('abc') -> /Me/Folders('abc')/Messages
            // 3. A child folder (may be recursively)
            // example: Folders('abc')/ChildFolders('xyz') -> /Me/Folders('abc')/ChildFolders('xyz')/Messages
            
            if (name.equals("Messages")) {
                objs.put("pathToSet", "Me/");
                objs.put("createPath", "Me/%s/Messages");
                objs.put("overridePath", true);
            } else if (name.equals("Events")) {
                objs.put("pathToSet", "Me/");
                objs.put("createPath", "Me/Calendars('%s')/Events");
                objs.put("overridePath", true);
            }
            
            
            if (!paths.get(currentType).equals("")) {
                objs.put("pathToSet", paths.get(currentType));
            }
            if (utility.capitalize(name).equals("Folders")) {
                objs.put("userType", schema.getEntityType("User"));
            }
            
            /*************************** END OF EXCHANGE-SPECIFIC BLOCK ********************************/
            if (generateInterfaces) {
                parseObj(base, pkg, "entitySet", "I" + utility.capitalize(name) + ".java", objs);
            }
            if (generateClasses) {
                parseObj(base, pkg, "entitySetImpl", utility.capitalize(name) + ".java", objs);
            }
            generatedEntitySets.put(currentType, true);
        }
    }
    
    /**
     * Adds type to queue for generation.
     * 
     * @param entitySetNames Maps entity type and its set name.
     * @param currentType Fully qualified enity type (contains schema namespace and class name).
     * @param paths Maps entity type and path to its set related to service root.
     * @param typesQueue A queue.
     * @param np Navigation property to extract entity type and set name.
     */
    private void addTypeToQueue(Map<String, String> entitySetNames,
                                String currentType,
                                Map<String, String> paths,
                                Queue<String> typesQueue,
                                NavigationProperty np) {
        typesQueue.add(utility.getNameFromNS(np.getType()));
        if (!entitySetNames.containsKey(utility.getNameFromNS(np.getType()))) {
            entitySetNames.put(utility.getNameFromNS(np.getType()), np.getName());
        }
        if (!paths.containsKey(utility.getNameFromNS(np.getType()))) {
            paths.put(utility.getNameFromNS(np.getType()),
                    paths.get(utility.getNameFromNS(currentType)) + entitySetNames.get(utility.getNameFromNS(currentType)) +
                        (getUtility().isSingleton(entitySetNames.get(utility.getNameFromNS(currentType))) ? "/" : "(%s)/"));
        }
    }
    
    /**
     * Gets types of entities in all entity sets in given entity container.
     * 
     * @param container Container.
     * @return Entity types list.
     */
    private Collection<String> getEntitySetTypes(EntityContainer container) {
        HashSet<String> types = new HashSet<String>();
        for (EntitySet set: container.getEntitySets()) {
            types.add(set.getEntityType());
        }
        
        return types;
    }
}
