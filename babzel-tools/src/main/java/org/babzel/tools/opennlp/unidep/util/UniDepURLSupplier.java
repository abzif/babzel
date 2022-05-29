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
package org.babzel.tools.opennlp.unidep.util;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.net.URL;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.babzel.tools.util.WebClientFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class UniDepURLSupplier {
    @NonNull
    private final WebClientFactory webClientFactory;

    public URL supplyTreebankURL() {
        WebClient webClient = webClientFactory.createWebClient();
        HtmlAnchor treebankDownloadPageAnchor = supplyTreebankDownloadPageAnchor(webClient);
        return supplyTreebankURL(webClient, treebankDownloadPageAnchor);
    }

    @SneakyThrows
    private HtmlAnchor supplyTreebankDownloadPageAnchor(WebClient webClient) {
        HtmlPage mainPage = webClient.getPage("http://universaldependencies.org");
        Optional<HtmlAnchor> treebankDownloadPageAnchor = mainPage.getAnchors().stream()
                .filter(a -> a.getHrefAttribute().contains("hdl.handle.net/11234"))
                .findFirst();
        Assert.isTrue(treebankDownloadPageAnchor.isPresent(), "Universal Dependencies treebank download page anchor not found");
        return treebankDownloadPageAnchor.get();
    }

    @SneakyThrows
    private URL supplyTreebankURL(WebClient webClient, HtmlAnchor treebankDownloadPageAnchor) {
        HtmlPage treebankDownloadPage = treebankDownloadPageAnchor.click();
        Optional<HtmlAnchor> treebankAnchor = treebankDownloadPage.getAnchors().stream()
                .filter(a -> a.getHrefAttribute().contains("ud-treebanks-v"))
                .findFirst();
        Assert.isTrue(treebankAnchor.isPresent(), "Universal Dependencies treebank download anchor not found");
        return treebankDownloadPage.getFullyQualifiedUrl(treebankAnchor.get().getHrefAttribute());
    }
}
