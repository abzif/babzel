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
package org.babzel.tools.util;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileUpToDateChecker {
    private final WebClientFactory webClientFactory;

    @SneakyThrows
    public boolean isUpToDate(@NonNull Path outputPath, @NonNull URL inputURL) {
        if (Files.exists(outputPath)) {
            FileTime outputLastModified = Files.getLastModifiedTime(outputPath);
            long outputFileLength = Files.size(outputPath);
            WebClient webClient = webClientFactory.createWebClient();
            WebRequest webRequest = new WebRequest(inputURL, HttpMethod.HEAD);
            Page page = webClient.getPage(webRequest);
            String lastModifiedHeader = page.getWebResponse().getResponseHeaderValue("Last-Modified");
            String contentLengthHeader = page.getWebResponse().getResponseHeaderValue("Content-Length");
            if (lastModifiedHeader != null || contentLengthHeader != null) {
                boolean lastModifiedUpToDate = lastModifiedHeader != null
                        ? getLastModifiedMillis(lastModifiedHeader) <= outputLastModified.toMillis()
                        : true;
                boolean contentLengthUpToDate = contentLengthHeader != null
                        ? getContentLength(contentLengthHeader) <= outputFileLength
                        : true;
                return lastModifiedUpToDate && contentLengthUpToDate;
            }
        }
        return false;
    }

    private long getLastModifiedMillis(String lastModifiedHeader) {
        return ZonedDateTime.parse(lastModifiedHeader, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli();
    }

    private long getContentLength(String contentLengthHeader) {
        return Long.parseLong(contentLengthHeader);
    }

    @SneakyThrows
    public boolean isUpToDate(@NonNull Path outputPath, @NonNull Path inputPath) {
        if (Files.exists(outputPath) && Files.exists(inputPath)) {
            FileTime outputLastModified = Files.getLastModifiedTime(outputPath);
            FileTime inputLastModified = Files.getLastModifiedTime(inputPath);
            return inputLastModified.compareTo(outputLastModified) <= 0;
        }
        return false;
    }
}
