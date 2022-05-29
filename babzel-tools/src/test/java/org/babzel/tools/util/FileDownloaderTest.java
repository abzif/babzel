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
import com.google.common.jimfs.Jimfs;
import java.net.URL;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.babzel.tools.ToolsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileDownloaderTest {
    private MockWebConnection mockWebConnection;
    private FileDownloader downloader;

    @BeforeEach
    public void setUp() {
        mockWebConnection = new MockWebConnection();
        var webClient = ToolsConfig.createSimpleWebClient();
        webClient.setWebConnection(mockWebConnection);
        downloader = new FileDownloader(() -> webClient);
    }

    @Test
    public void downloadFile() throws Exception {
        mockWebConnection.setResponse(new URL("http://host/p/file.txt"), "###content###");
        Path outputPath = Jimfs.newFileSystem().getPath("some", "dir", "file.txt");

        downloader.downloadFile(new URL("http://host/p/file.txt"), outputPath);

        Assertions.assertThat(outputPath).content().isEqualTo("###content###");
    }
}
