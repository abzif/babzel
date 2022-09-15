/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.babzel.tools;

import com.gargoylesoftware.htmlunit.WebClient;
import java.nio.file.Path;
import org.babzel.tools.util.RootDirectorySupplier;
import org.babzel.tools.util.WebClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = ToolsConfig.class)
public class ToolsConfig {
    public static final String WORK_DIRECTORY_PROPERTY = "work.directory";

    @Bean
    public WebClientFactory webClientFactory() {
        return () -> createSimpleWebClient();
    }

    @Bean
    public RootDirectorySupplier rootDirectorySupplier() {
        return () -> {
            var workDirectoryName = System.getProperty(WORK_DIRECTORY_PROPERTY, "");
            var rootDirectory = !workDirectoryName.isEmpty()
                    ? Path.of(workDirectoryName)
                    : Path.of(System.getProperty("user.home")).resolve(".cache").resolve("babzel");
            return rootDirectory.toAbsolutePath().normalize();
        };
    }

    public static WebClient createSimpleWebClient() {
        // create web client configured for simple download, without background processing
        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        return webClient;
    }
}
