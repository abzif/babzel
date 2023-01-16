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

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import lombok.SneakyThrows;
import org.babzel.tools.util.RootDirectorySupplier;
import org.babzel.tools.util.WebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = ToolsConfig.class)
public class ToolsConfig {
    public static final String WORK_DIRECTORY_PROPERTY = "work.directory";

    @Bean
    public WebClient webClient() {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        return new WebClient() {
            @Override
            @SneakyThrows
            public byte[] readContentAsBytes(URL url) {
                var request = HttpRequest.newBuilder().GET().uri(url.toURI()).build();
                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                return response.body();
            }

            @Override
            @SneakyThrows
            public Map<String, String> getHeaders(URL url) {
                var request = HttpRequest.newBuilder().method("HEAD", HttpRequest.BodyPublishers.noBody()).uri(url.toURI()).build();
                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                return HashMap.ofAll(response.headers().map()).mapValues(vl -> vl.get(0));
            }
        };
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
}
