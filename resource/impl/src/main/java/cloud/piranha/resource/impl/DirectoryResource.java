/*
 * Copyright (c) 2002-2025 Manorrock.com. All Rights Reserved.
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
package cloud.piranha.resource.impl;

import cloud.piranha.resource.api.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.System.Logger.Level.DEBUG;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The default DirectoryResource.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
public class DirectoryResource implements Resource {

    /**
     * Stores the logger.
     */
    private static final System.Logger LOGGER = System.getLogger(DirectoryResource.class.getName());

    /**
     * Stores the root directory.
     */
    private File rootDirectory;

    /**
     * Constructor.
     */
    public DirectoryResource() {
    }

    /**
     * Constructor.
     *
     * @param rootDirectory the root directory.
     */
    public DirectoryResource(String rootDirectory) {
        this(new File(rootDirectory));
    }

    /**
     * Constructor.
     *
     * @param rootDirectory the root directory.
     */
    public DirectoryResource(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public Stream<String> getAllLocations() {
        try {
            Path rootPath = Paths.get(rootDirectory.toURI());
            Path root = Paths.get("/");
            return Files.walk(rootPath)
                    .filter(Predicate.not(Files::isDirectory))
                    .map(rootPath::relativize)
                    .map(root::resolve)
                    .map(p -> p.toString().replace("\\", "/"));
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    @Override
    public String getName() {
        return rootDirectory.getName();
    }

    /**
     * {@return the resource}
     */
    @SuppressWarnings("deprecation")
    @Override
    public URL getResource(String location) {
        URL result = null;
        File file = resolveFile(rootDirectory, location);
        if (file != null && file.exists()) {
            try {
                result = new URL(file.toURI().toString());
            } catch (MalformedURLException mue) {
                LOGGER.log(DEBUG, "Malformed URL for find directory resource", mue);
            }
        }
        return result;
    }

    /**
     * @param location the resource location.
     * @return the input stream, or null if not found.
     * @see Resource#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream(String location) {
        InputStream result = null;
        File file = resolveFile(rootDirectory, location);
        if (file != null) {
            try {
                result = new FileInputStream(file);
            } catch (FileNotFoundException fnfe) {
                LOGGER.log(DEBUG, "Unable to find directory resource", fnfe);
            }
        }
        return result;
    }

    /**
     * Resolves a location against a root directory, as long as the result stays within that directory, so that a
     * location with ".." segments can't reach files outside it.
     *
     * @param rootDirectory the root directory.
     * @param location the location.
     * @return the file, or null when there's no root directory or location, or the location is outside the root
     * directory or not a valid path.
     */
    static File resolveFile(File rootDirectory, String location) {
        if (rootDirectory == null || location == null) {
            return null;
        }
        try {
            Path root = rootDirectory.toPath().toAbsolutePath().normalize();
            Path file = new File(rootDirectory, location).toPath().toAbsolutePath().normalize();
            return file.startsWith(root) ? file.toFile() : null;
        } catch (InvalidPathException ipe) {
            LOGGER.log(DEBUG, "Invalid directory resource location", ipe);
            return null;
        }
    }

    /**
     * {@return the root directory}
     */
    public File getRootDirectory() {
        return this.rootDirectory;
    }

    /**
     * Set the root directory.
     *
     * @param rootDirectory the root directory.
     */
    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public String toString() {
        return getName() + " " + super.toString();
    }
}
