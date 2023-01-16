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

import com.google.common.jimfs.Jimfs;
import io.vavr.collection.HashMap;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FileUpToDateCheckerTest {
    @Mock
    private WebClient webClient;
    @InjectMocks
    private FileUpToDateChecker checker;

    @Test
    public void urlDependent_OutputPathDoesNotExists() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path outputPath = rootPath.resolve("output.txt");

        boolean isUpToDate = checker.isUpToDate(outputPath, new URL("http://host/path"));

        assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void urlDependent_NoHeadersToCheck() throws Exception {
        given(webClient.getHeaders(any())).willReturn(HashMap.empty());
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");

        boolean isUpToDate = checker.isUpToDate(outputPath, new URL("http://host/path"));

        verify(webClient).getHeaders(new URL("http://host/path"));
        verifyNoMoreInteractions(webClient);
        assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void urlDependent_LastModifiedCheckFails() throws Exception {
        given(webClient.getHeaders(any())).willReturn(HashMap.of("Last-Modified", "Thu, 04 Nov 2021 16:31:21 GMT"));
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");
        Files.setLastModifiedTime(outputPath, FileTime.from(ZonedDateTime.of(2021, 11, 4, 16, 31, 20, 0, ZoneId.ofOffset("", ZoneOffset.UTC)).toInstant()));

        boolean isUpToDate = checker.isUpToDate(outputPath, new URL("http://host/path"));

        verify(webClient).getHeaders(new URL("http://host/path"));
        verifyNoMoreInteractions(webClient);
        assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void urlDependent_ContentLengthCheckFails() throws Exception {
        given(webClient.getHeaders(any())).willReturn(HashMap.of("Content-Length", "333"));
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");

        boolean isUpToDate = checker.isUpToDate(outputPath, new URL("http://host/path"));

        verify(webClient).getHeaders(new URL("http://host/path"));
        verifyNoMoreInteractions(webClient);
        assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void urlDependent_UpToDate() throws Exception {
        given(webClient.getHeaders(any())).willReturn(HashMap.of("Last-Modified", "Thu, 04 Nov 2021 16:31:21 GMT", "Content-Length", "6"));
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");
        Files.setLastModifiedTime(outputPath, FileTime.from(ZonedDateTime.of(2021, 11, 4, 16, 31, 21, 0, ZoneId.ofOffset("", ZoneOffset.UTC)).toInstant()));

        boolean isUpToDate = checker.isUpToDate(outputPath, new URL("http://host/path"));

        verify(webClient).getHeaders(new URL("http://host/path"));
        verifyNoMoreInteractions(webClient);
        assertThat(isUpToDate).isEqualTo(true);
    }

    @Test
    public void pathDependent_OutputPathDoesNotExists() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path inputPath = rootPath.resolve("input.txt");
        Files.writeString(inputPath, "input");
        Path outputPath = rootPath.resolve("output.txt");

        boolean isUpToDate = checker.isUpToDate(outputPath, inputPath);

        assertThat(isUpToDate).isEqualTo(false);
    }

    @Test
    public void pathDependent_InputPathDoesNotExist() throws Exception {
        Path rootPath = Jimfs.newFileSystem().getPath("");
        Path inputPath = rootPath.resolve("input.txt");
        Path outputPath = rootPath.resolve("output.txt");
        Files.writeString(outputPath, "output");

        boolean isUpToDate = checker.isUpToDate(outputPath, inputPath);

        assertThat(isUpToDate).isEqualTo(false);
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

        assertThat(isUpToDate).isEqualTo(false);
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

        assertThat(isUpToDate).isEqualTo(true);
    }
}
