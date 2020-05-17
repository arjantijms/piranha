/*
 * Copyright (c) 2002-2020 Manorrock.com. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package cloud.piranha.micro;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.util.List;
import java.util.stream.Stream;

/**
 * The configuration settings for Piranha Micro
 *
 *
 * @author Arjan Tijms
 *
 */
public class MicroConfiguration {

    private String version;
    private String modules;
    private String dependencies;
    private String repositories;
    private boolean offline;
    private int port;

    private List<String> modulesList;
    private List<String> repositoriesList;
    private List<String> mergedDependencies;
    

    /**
     * Default constructor. Initializes most of the stuff from System properties.
     */
    public MicroConfiguration() {
        this(
            System.getProperty("piranha.version", MicroConfiguration.class.getPackage().getImplementationVersion()),
            System.getProperty("piranha.modules", "piranha-micro"), 
            System.getProperty("piranha.dependencies", ""),
            System.getProperty("piranha.repositories", "https://repo1.maven.org/maven2"), 
            Boolean.valueOf(System.getProperty("piranha.offline", "false")),
            Integer.valueOf(System.getProperty("piranha.port", "8080")),
            
            null, 
            null, 
            null);
    }

    /**
     * Constructor.
     * 
     * @param version Piranha version.
     * @param modules Piranha modules.
     * @param dependencies Piranha dependencies.
     * @param repositories Piranha repositories.
     * @param offline Offline flag.
     * @param port http port on which Piranha listens to requests
     * @param modulesList List of modules.
     * @param repositoriesList List of repos.
     * @param mergedDependencies List of merged dependencies.
     */
    public MicroConfiguration(
        String version, 
        String modules, 
        String dependencies, 
        String repositories, 
        boolean offline, 
        int port,
        
        List<String> modulesList, 
        List<String> repositoriesList, 
        List<String> mergedDependencies) {
        
        this.version = version;
        this.modules = modules;
        this.dependencies = dependencies;
        this.repositories = repositories;
        this.offline = offline;
        this.port = port;
        
        this.modulesList = modulesList;
        this.repositoriesList = repositoriesList;
        this.mergedDependencies = mergedDependencies;
    }

    public MicroConfiguration postConstruct() {
        modulesList = stream(modules.split(","))
                .map(module -> module.trim())
                .collect(toList());

        Stream<String> moduleDependenciesStream = modulesList.stream()
                .map(module -> "cloud.piranha:" + module + ":" + version);

        Stream<String> dependenciesStream = stream(dependencies.split(","))
                .map(dep -> dep.trim())
                .filter(dep -> !dep.isEmpty());

        repositoriesList = stream(repositories.split(","))
                .map(repo -> repo.trim())
                .filter(repo -> !repo.isEmpty())
                .collect(toList());

        mergedDependencies = concat(moduleDependenciesStream, dependenciesStream)
                .collect(toList());
        
        return this;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getModules() {
        return modules;
    }

    public void setModules(String modules) {
        this.modules = modules;
    }

    public String getRepositories() {
        return repositories;
    }

    public void setRepositories(String repositories) {
        this.repositories = repositories;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
    
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<String> getModulesList() {
        return modulesList;
    }

    public List<String> getRepositoriesList() {
        return repositoriesList;
    }

    public List<String> getMergedDependencies() {
        return mergedDependencies;
    }

}