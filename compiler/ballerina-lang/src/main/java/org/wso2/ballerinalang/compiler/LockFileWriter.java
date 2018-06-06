/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Write Ballerina.lock to target after every build.
 *
 * @since 0.973.1
 */
public class LockFileWriter {
    private static final CompilerContext.Key<LockFileWriter> LOCK_FILE_WRITER_KEY = new CompilerContext.Key<>();
    private final SourceDirectory sourceDirectory;
    private List<StringBuilder> builderList = new ArrayList<>();
    private List<LockPackage> ballerinaLockPackages = new ArrayList<>();

    /**
     * Constructor of LockFileWriter.
     *
     * @param context compiler context
     */
    private LockFileWriter(CompilerContext context) {
        context.put(LOCK_FILE_WRITER_KEY, this);
        this.sourceDirectory = context.get(SourceDirectory.class);
        if (this.sourceDirectory == null) {
            throw new IllegalArgumentException("source directory has not been initialized");
        }
    }

    /**
     * Get an instance of the LockFileWriter.
     *
     * @param context compiler context
     * @return instance of the LockFileWriter
     */
    public static LockFileWriter getInstance(CompilerContext context) {
        LockFileWriter lockFileWriter = context.get(LOCK_FILE_WRITER_KEY);
        if (lockFileWriter == null) {
            lockFileWriter = new LockFileWriter(context);
        }
        return lockFileWriter;
    }

    /**
     * Get ballerina version.
     *
     * @return ballerina version
     */
    private static String getBallerinaVersion() {
        try (InputStream inputStream = LockFileWriter.class
                .getResourceAsStream(LockFileConstants.LAUNCHER_PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(LockFileConstants.BALLERINA_VERSION_PROPERTY);
        } catch (Throwable ignore) {
        }
        return "unknown";
    }

    /**
     * Distinct entries in list by value.
     *
     * @param key objects
     * @param <T> method which determines the uniqueness
     * @return distinct set of elements
     */
    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> key) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(key.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Generate list of dependencies of package.
     *
     * @param packageSymbol package symbol
     */
    void getPkgDependencies(BPackageSymbol packageSymbol) {
        List<BPackageSymbol> importPackages = getImportPackages(packageSymbol);
        ballerinaLockPackages.add(new LockPackage(packageSymbol.pkgID.orgName.value + "/" +
                                                          packageSymbol.pkgID.name.value,
                                                  packageSymbol.pkgID.version.value,
                                                  getImportsAsStr(importPackages)));
        if (importPackages.size() > 0) {
            for (BPackageSymbol importPackage : importPackages) {
                getPkgDependencies(importPackage);
            }
        }
    }

    /**
     * Get import package info as a string.
     *
     * @param packageSymbols list of package symbols of imported packages
     * @return import package info as a string
     */
    private String getImportsAsStr(List<BPackageSymbol> packageSymbols) {
        return packageSymbols.stream().map(bPackageSymbol -> bPackageSymbol.pkgID.orgName.value + "/" +
                bPackageSymbol.pkgID.name.value + " " + bPackageSymbol.pkgID.version.value)
                             .collect(Collectors.joining(","));
    }

    /**
     * Get import packages of package.
     *
     * @param packageNode packafe node
     * @return import package list
     */
    private List<BPackageSymbol> getImportPackages(BPackageSymbol packageNode) {
        return packageNode.imports.stream()
                                  .filter(pkg -> !pkg.pkgID.orgName.value.equals(LockFileConstants.BALLERINA))
                                  .collect(Collectors.toList());
    }

    /**
     * Generate the project description.
     *
     * @param manifest manfiest object
     * @return string with the project description
     */
    private StringBuilder generateProjectDesc(Manifest manifest) {
        return new StringBuilder()
                .append("[" + LockFileConstants.PROJECT + "]").append("\n")
                .append(LockFileConstants.ORG_NAME).append(" = ")
                .append(String.format("\"%s\"", manifest.getName())).append("\n")
                .append(LockFileConstants.VERSION).append(" = ")
                .append(String.format("\"%s\"", manifest.getVersion())).append("\n")
                .append(LockFileConstants.BALLERINA_VERSION).append(" = ")
                .append(String.format("\"%s\"", getBallerinaVersion())).append("\n");
    }

    /**
     * Write Ballerina.lock file.
     *
     * @param manifest manifest object
     */
    void writeLockFile(Manifest manifest) {
        List<LockPackage> packageList = ballerinaLockPackages.stream().filter(distinctByKey(LockPackage::toString))
                                                             .collect(Collectors.toList());
        for (LockPackage lockPackage : packageList) {
            builderList.add(new StringBuilder().append("[[").append("package").append("]] \n")
                                               .append("name = ")
                                               .append(String.format("\"%s\"", lockPackage.getName())).append("\n")
                                               .append("version = ")
                                               .append(String.format("\"%s\"", lockPackage.getVersion())));
            if (!lockPackage.getDependencies().isEmpty()) {
                builderList.add(new StringBuilder().append("dependencies = [")
                                                   .append(String.format("\"%s\"", lockPackage.getDependencies()))
                                                   .append("] \n"));
            } else {
                builderList.add(new StringBuilder().append(""));
            }
        }

        Path ballerinaLockFilePath = this.sourceDirectory.getPath().resolve(ProjectDirConstants.TARGET_DIR_NAME)
                                                         .resolve(Paths.get(LockFileConstants
                                                                                    .BALLERINA_LOCK_FILE_NAME));
        builderList.add(0, generateProjectDesc(manifest));

        StringBuilder sb = new StringBuilder(builderList.size() * 10);
        for (StringBuilder line : builderList) {
            sb.append(line);
            sb.append("\n");
        }
        try {
            Files.write(ballerinaLockFilePath, sb.toString().getBytes());
        } catch (IOException ignore) {
        }
    }
}
