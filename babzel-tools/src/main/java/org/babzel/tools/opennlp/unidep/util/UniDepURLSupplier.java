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

import java.net.URL;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.babzel.tools.util.WebClient;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class UniDepURLSupplier {
    @NonNull
    private final WebClient webClient;

    public URL supplyTreebankURL() {
        var treebankDownloadPageURL = supplyTreebankDownloadPageURL();
        return supplyTreebankURL(treebankDownloadPageURL);
    }

    @SneakyThrows
    private URL supplyTreebankDownloadPageURL() {
        var uniDepURL = new URL("http://universaldependencies.org");
        var response = webClient.makeGetRequest(uniDepURL);
        var htmlDoc = Jsoup.parse(new String(response.getBody().getOrElse(new byte[0])));
        var treebankDownloadPageAnchors = htmlDoc.select("a[href*='hdl.handle.net/11234']");
        Assert.isTrue(!treebankDownloadPageAnchors.isEmpty(), "Universal Dependencies treebank download page anchor not found");
        return new URL(response.getUrl(), treebankDownloadPageAnchors.attr("href"));
    }

    @SneakyThrows
    private URL supplyTreebankURL(URL treebankDownloadPageURL) {
        var response = webClient.makeGetRequest(treebankDownloadPageURL);
        var htmlDoc = Jsoup.parse(new String(response.getBody().getOrElse(new byte[0])));
        var treebankAnchors = htmlDoc.select("a[href*='ud-treebanks-v']");
        Assert.isTrue(!treebankAnchors.isEmpty(), "Universal Dependencies treebank download anchor not found");
        return new URL(response.getUrl(), treebankAnchors.attr("href"));
    }
}
