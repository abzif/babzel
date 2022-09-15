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

import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.common.jimfs.Jimfs;
import java.nio.file.FileSystem;
import static org.assertj.core.api.Assertions.assertThat;
import org.babzel.tools.ToolsConfigTest.XToolsConfig;
import org.babzel.tools.opennlp.unidep.UniDepConlluModelProcessor;
import org.babzel.tools.util.ResourceDirectorySupplier;
import org.babzel.tools.util.RootDirectorySupplier;
import org.babzel.tools.util.TextNormalizer;
import org.babzel.tools.util.WebClientFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {XToolsConfig.class})
public class ToolsConfigTest {
    @Configuration
    public static class XToolsConfig extends ToolsConfig {
        @Override
        @Bean
        public WebClientFactory webClientFactory() {
            return () -> createMockedWebClient(mockWebConnection());
        }

        @Bean
        public MockWebConnection mockWebConnection() {
            return new MockWebConnection();
        }

        @Override
        @Bean
        public RootDirectorySupplier rootDirectorySupplier() {
            return () -> fileSystem().getPath("root");
        }

        @Bean
        public FileSystem fileSystem() {
            return Jimfs.newFileSystem();
        }

        public static WebClient createMockedWebClient(MockWebConnection mockWebConnection) {
            WebClient webClient = ToolsConfig.createSimpleWebClient();
            webClient.setWebConnection(mockWebConnection);
            return webClient;
        }

        @Bean
        public TextNormalizer textNormalizer() {
            return new TextNormalizer() {
                @Override
                public String normalizeText(String language, String text) {
                    return text;
                }
            };
        }

        @Bean
        public ResourceDirectorySupplier resourceDirectorySupplier() {
            return language -> rootDirectorySupplier().getRootDirectory().resolve(String.format("%s-res", language));
        }
    }

    @Autowired
    private UniDepConlluModelProcessor processor;

    @Test
    public void config() {
        assertThat(processor).isNotNull();
    }
}
