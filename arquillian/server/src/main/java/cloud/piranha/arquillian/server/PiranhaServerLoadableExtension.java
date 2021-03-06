/*
 * Copyright (c) 2002-2021 Manorrock.com. All Rights Reserved.
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
package cloud.piranha.arquillian.server;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import cloud.piranha.micro.loader.MicroConfiguration;

/**
 * The extension sets up the Arquillian Server Connector
 * 
 * @author Arjan Tijms
 *
 */
public class PiranhaServerLoadableExtension implements LoadableExtension {

    // Defines the deployable container used; PiranhaServerDeployableContainer.class 
    // This is the actual "connector" that controls Piranha.
    
    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, PiranhaServerDeployableContainer.class);
    }
    
    // Defines the configuration class used: PiranhaServerContainerConfiguration.class
    // Defines the protocol used: "Servlet 3.0"
    
    public abstract static class PiranhaServerContainerBase implements DeployableContainer<PiranhaServerContainerConfiguration> {
        
        @Override
        public Class<PiranhaServerContainerConfiguration> getConfigurationClass() {
            return PiranhaServerContainerConfiguration.class;
        }

        @Override
        public ProtocolDescription getDefaultProtocol() {
            return new ProtocolDescription("Servlet 3.0");
        }
        
        @Override
        public void start() throws LifecycleException {
            // We don't start Piranha separately. Start and Deploy is one step.
        }
        
        @Override
        public void deploy(Descriptor descriptor) throws DeploymentException {
            // We don't deploy by descriptor (and neither does Arquillian it seems)
            
        }
        
        @Override
        public void undeploy(Descriptor descriptor) throws DeploymentException {
         // We don't undeploy by descriptor (and neither does Arquillian it seems)
        }

        @Override
        public void stop() throws LifecycleException {
            // We don't stop Piranha separately. Stop and Undeploy is one step.
        }

    }
    
    // Defines the configuration class to be essentially the same as MicroConfiguration.class
    
    public static class PiranhaServerContainerConfiguration extends MicroConfiguration implements ContainerConfiguration {
        @Override
        public void validate() throws ConfigurationException {
            postConstruct();
        }
        
    }
}
