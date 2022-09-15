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

import io.vavr.control.Option;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import lombok.SneakyThrows;
import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.babzel.tools.opennlp.model.util.ModelPersister;
import org.babzel.tools.opennlp.unidep.UniDepConlluModelProcessor;
import org.babzel.tools.opennlp.unidep.util.UniDepConlluModelFileSupplier;
import org.babzel.tools.util.RootDirectorySupplier;
import org.babzel.tools.util.TextNormalizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ToolsCmd {
    public static void main(String[] args) {
        try ( var ctx = new AnnotationConfigApplicationContext(ToolsConfig.class)) {
            if (args.length == 2 || args.length == 3) {
                var cmd = args[0];
                var language = args[1];
                var workDir = args.length == 3 ? args[2] : "";
                System.setProperty(ToolsConfig.WORK_DIRECTORY_PROPERTY, workDir);
                if ("train".equals(cmd)) {
                    trainModels(ctx, language);
                } else if ("verify".equals(cmd)) {
                    verifyModels(ctx, language);
                } else {
                    printHelp(ctx);
                }
            } else {
                printHelp(ctx);
            }
        }
    }

    private static void printHelp(ApplicationContext ctx) {
        var rootDir = ctx.getBean(RootDirectorySupplier.class).getRootDirectory();
        System.out.println("Usage: <command> <two-letter-language-code> [<work-directory>]");
        System.out.println("  <command> can be one of:");
        System.out.println("    train - train language models");
        System.out.println("    verify - interactive verification of trained models");
        System.out.println("  <two-letter-language-code> - in example 'en', 'es', 'zh' etc");
        System.out.println("  <work-directory> - root of directory where models will be generated");
        System.out.println("    default value: " + rootDir);
    }

    private static void trainModels(ApplicationContext ctx, String language) {
        var processor = ctx.getBean(UniDepConlluModelProcessor.class);
        processor.processUniDepConlluModel(language);
    }

    @SneakyThrows
    private static void verifyModels(ApplicationContext ctx, String language) {
        var textNormalizer = ctx.getBean(TextNormalizer.class);
        var fileSupplier = ctx.getBean(UniDepConlluModelFileSupplier.class);
        var modelPersister = ctx.getBean(ModelPersister.class);
        if (!(Files.isRegularFile(fileSupplier.getSentenceModelFile(language))
                && Files.isRegularFile(fileSupplier.getTokenizerModelFile(language))
                && Files.isRegularFile(fileSupplier.getPOSModelFile(language)))) {
            System.out.println(String.format("Missing model files for language '%s'", language));
            return;
        }
        var sentenceDetector = new SentenceDetectorME(modelPersister.readModel(fileSupplier.getSentenceModelFile(language), SentenceModel.class));
        var tokenizer = new TokenizerME(modelPersister.readModel(fileSupplier.getTokenizerModelFile(language), TokenizerModel.class));
        var posTagger = new POSTaggerME(modelPersister.readModel(fileSupplier.getPOSModelFile(language), POSModel.class));
        var lemmatizer = Files.isRegularFile(fileSupplier.getLemmatizerModelFile(language))
                ? Option.<Lemmatizer>some(new LemmatizerME(modelPersister.readModel(fileSupplier.getLemmatizerModelFile(language), LemmatizerModel.class)))
                : Option.<Lemmatizer>none();
        while (true) {
            System.out.println("Enter text or 'q' to quit");
            var reader = new BufferedReader(new InputStreamReader(System.in));
            var line = reader.readLine();
            if ("q".equalsIgnoreCase(line)) {
                break;
            } else {
                verifyModels(line, language, textNormalizer, sentenceDetector, tokenizer, posTagger, lemmatizer);
            }
        }
    }

    private static void verifyModels(
            String text,
            String language,
            TextNormalizer textNormalizer,
            SentenceDetector sentenceDetector,
            Tokenizer tokenizer,
            POSTagger posTagger,
            Option<Lemmatizer> lemmatizer) {
        text = textNormalizer.normalizeText(text, language);
        var sentences = sentenceDetector.sentDetect(text);
        for (var sentence : sentences) {
            System.out.println(sentence);
            var tokens = tokenizer.tokenize(sentence);
            var posTags = posTagger.tag(tokens);
            var lemmas = lemmatizer.isDefined()
                    ? lemmatizer.get().lemmatize(tokens, posTags)
                    : tokens;
            for (int i = 0; i < tokens.length; i++) {
                System.out.println(String.format("%s\t%s\t%s", tokens[i], posTags[i], lemmas[i]));
            }
            System.out.println("");
        }
    }
}
