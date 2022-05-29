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

import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.common.jimfs.Jimfs;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.babzel.tools.ToolsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileUpToDateCheckerTest {
    private MockWebConnection mockWebConnection;
    private FileUpToDateChecker checker;

    @BeforeEach
    public void setUp() {
        mockWebConnection = new MockWebConnection();
        var webClient = ToolsConfig.createSimpleWebClient();
        webClient.setWebConnection(mockWebConnection);
        checker = new FileUpToDateChecker(() -> webClient);
    }

    @Test
    public void urlDependent_OutputPathDoesNotExists() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path outputPath = rootPath.resolve("output.txt");

        boolean isUpToDate = checker.isUpToDate(outputPath, new URL("http://host/path"));

        Assertions.assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void urlDependent_NoHeadersToCheck() throws Exception {
        mockWebConnection.setResponse(new URL("http://host/path"), "input");
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");

        boolean isUpToDate = checker.isUpToDate(outputPath, new URL("http://host/path"));

        Assertions.assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void urlDependent_LastModifiedCheckFails() throws Exception {
        mockWebConnection.setResponse(new URL("http://host/path"),
                "input", 200, "OK",
                "text/plain", List.of(
                        new NameValuePair("Last-Modified", "Thu, 04 Nov 2021 16:31:21 GMT")));
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");
        Files.setLastModifiedTime(outputPath, FileTime.from(ZonedDateTime.of(2021, 11, 4, 16, 31, 20, 0, ZoneId.ofOffset("", ZoneOffset.UTC)).toInstant()));

        boolean isUpToDate = checker.isUpToDate(outputPath, new URL("http://host/path"));

        Assertions.assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void urlDependent_ContentLengthCheckFails() throws Exception {
        mockWebConnection.setResponse(new URL("http://host/path"),
                "input", 200, "OK",
                "text/plain", List.of(
                        new NameValuePair("Content-Length", "333")));
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");

        boolean isUpToDate = checker.isUpToDate(outputPath, new URL("http://host/path"));

        Assertions.assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void urlDependent_UpToDate() throws Exception {
        mockWebConnection.setResponse(new URL("http://host/path"),
                "input", 200, "OK",
                "text/plain", List.of(
                        new NameValuePair("Content-Length", "6"),
                        new NameValuePair("Last-Modified", "Thu, 04 Nov 2021 16:31:21 GMT")));
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");
        Files.setLastModifiedTime(outputPath, FileTime.from(ZonedDateTime.of(2021, 11, 4, 16, 31, 21, 0, ZoneId.ofOffset("", ZoneOffset.UTC)).toInstant()));

        boolean isUpToDate = checker.isUpToDate(outputPath, new URL("http://host/path"));

        Assertions.assertThat(isUpToDate).isEqualTo(true);
    }

    @Test
    public void pathDependent_OutputPathDoesNotExists() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path inputPath = rootPath.resolve("input.txt");
        Files.writeString(inputPath, "input");
        Path outputPath = rootPath.resolve("output.txt");

        boolean isUpToDate = checker.isUpToDate(outputPath, inputPath);

        Assertions.assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void pathDependent_InputPathDoesNotExist() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path inputPath = rootPath.resolve("input.txt");
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");

        boolean isUpToDate = checker.isUpToDate(outputPath, inputPath);

        Assertions.assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void pathDependent_LastModifiedCheckFails() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path inputPath = rootPath.resolve("input.txt");
        Files.writeString(inputPath, "input");
        Files.setLastModifiedTime(inputPath, FileTime.fromMillis(111));
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");
        Files.setLastModifiedTime(outputPath, FileTime.fromMillis(110));

        boolean isUpToDate = checker.isUpToDate(outputPath, inputPath);

        Assertions.assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void pathDependent_UpToDate() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path inputPath = rootPath.resolve("input.txt");
        Files.writeString(inputPath, "input");
        Files.setLastModifiedTime(inputPath, FileTime.fromMillis(111));
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");
        Files.setLastModifiedTime(outputPath, FileTime.fromMillis(111));

        boolean isUpToDate = checker.isUpToDate(outputPath, inputPath);

        Assertions.assertThat(isUpToDate).isEqualTo(true);
    }
}
