# Babzel - OpenNLP models generator

Babzel is a library which allows computing models for [Apache OpenNLP](https://opennlp.apache.org) from [Universal Dependencies](https://universaldependencies.org) annotated language files.
OpenNLP supports natural language processing with tools like: sentence detector, tokenizer, part of speech tagger, lemmatizer etc.
However models for various languages are not easily available.
This project adresses these shortcomings and allows to train and evaluate models for any language supported by Universal Dependencies treebank.
Models can also be interactively verified.

## Pre-trained models

Pre-trained models for various languages are automatically computed and available [here](https://abzif.github.io/babzel/models.html)

## Training and evaluation process

All files (input or generated models) are processed in the root directory (or subdirectories) which is **$HOME/.cache/babzel**

Universal Dependencies treebank consists of **conllu** files for many languages. **Conllu** file contains annotated sentences in a particular language. Annotations describe tokens and part of speech and lemma for every token. Possible POS tags are listed [here](https://universaldependencies.org/u/pos)

Text used for training is normalized. Two normalizers are supported so far:
- simple (text is lowercased only using Locale.ENGLISH)
- lucene (text is normalized using [Apache Lucene](https://lucene.apache.org) analyzers - text is lowercased, normalized, folded to ascii equivalents where possible. Such models may be used in [Apache SOLR](https://solr.apache.org) or [Elasticsearch](https://www.elastic.co) which supports OpenNLP analyzers

The process of training and evaluation of models roughly consists of the following steps:
- Download universal dependencies treebank (only if does not exists locally or newer version is available).
- Unpack conllu files for a particular language
- For every supported trainer (sentence-detector, tokenizer, pos-tagger, lemmatizer) perform further steps. Training is performed only if a model does not exists or newer conllu file is available.
  - Read the sentences from conllu file
  - Optional: Try to fix the data (in example for 'de' language)
  - Convert sentences to sample stream for a particular trainer (token sample stream, lemma sample stream etc)
  - Train and evaluate model. Several available algorithms are tried and evaluated. Only the best one is choosen.
  - Save model and evaluation report.

## Usage

Download appropriate fat jar from [Packages](https://github.com/abzif?tab=packages&repo_name=babzel) (babzel-tools-simple or babzel-tools-lucene)

Training and evaluation:
```
  java -jar <jar-file-name> train <two-letter-language-code> <optional-working-directory>
```
If working directory is not specified then models are trained and evaluated in directory **$HOME/.cache/babzel**

Interactive verification:
```
  java -jar <jar-file-name> verify <two-letter-language-code> <optional-working-directory>
```
In this mode user is prompted to enter some text. Text is divided into sentences, tokenized, lemmatized etc. The results are printed on screen.

## Lucene analysis chain

Lucene models are trained assuming specific chain of analyzer filters. Such chain must be preserved in order for models to work properly.

```
<analyzer>
  <!-- lowercase -->
  <charFilter class="solr.ICUNormalizer2CharFilterFactory" name="nfkc_cf" mode="compose"/>
  <!-- fold to ascii, drop accents, expand ligatures etc -->
  <charFilter class="solr.ICUNormalizer2CharFilterFactory" name="nfc" mode="decompose"/>
  <charFilter class="solr.MappingCharFilterFactory" mapping="fold-to-ascii.txt"/>
  <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="\p{InCombiningDiacriticalMarks}" replacement=""/>
  <!-- tokenizer -->
  <tokenizer class="solr.OpenNLPTokenizerFactory" sentenceModel="xy-sentence-detector.onlpm" tokenizerModel="xy-tokenizer.onlpm"/>
  <!-- part of speech tagging -->
  <filter class="solr.OpenNLPPOSFilterFactory" posTaggerModel="xy-pos-tagger.onlpm"/>
  <!-- lemmatizer -->
  <filter class="solr.OpenNLPLemmatizerFilterFactory" lemmatizerModel="xy-lemmatizer.onlpm"/>
  <!-- other necessary filters TypeTokenFilterFactory, TypeAsPayloadFilterFactory etc -->
</analyzer>
```
[fold-to-ascii.txt](babzel-tools-lucene/src/main/resources/org/babzel/tools/util/fold-to-ascii.txt) is a file which normalizes additional characters which are not handled by ICU normalizer.

## Evaluation results

Several models were trained for different language types (using lucene text normalizer). The results of their evaluation are presented below.
Available sentences are divided into training/evaluation sets. Every 10th sentence goes to evaluation set. 90% of sentences is used for training.

- **language**: language code + language name
- **training sentences**: approximate number of training sentences
- **models**: training algorithm (algorithm with the best evaluation score) + score (ranging from 0.0 to 1.0)

### Alphabetic latin languages
These languages uses alphabetic latin script with native diactritic characters. Words are separated by whitespace.

|   language    | training sentences | sentence-detector |     tokenizer     |   pos-tagger   |   lemmatizer   |
| :-----------: | :----------------: | :---------------: | :---------------: | :------------: | :------------: |
| de<br>german  |        65k         | MAXENT_QN<br>0.72 | MAXENT_QN<br>0.99 | MAXENT<br>0.94 | MAXENT<br>0.96 |
| en<br>english |        35k         | MAXENT_QN<br>0.74 | MAXENT_QN<br>0.99 | MAXENT<br>0.94 | MAXENT<br>0.98 |
| es<br>spanish |        30k         | MAXENT_QN<br>0.96 | MAXENT_QN<br>0.99 | MAXENT<br>0.94 | MAXENT<br>0.98 |
| fr<br>french  |        25k         | MAXENT_QN<br>0.92 | MAXENT_QN<br>0.99 | MAXENT<br>0.95 | MAXENT<br>0.98 |
| pl<br>polish  |        36k         |  MAXENT<br>0.95   | MAXENT_QN<br>0.99 | MAXENT<br>0.96 | MAXENT<br>0.96 |

Models generated for such types of languages have good quality.
These types of languages are supported very well.
Sentence detection score is relatively low, because many sentences in the sample were not properly ended.

### Alphabetic non-latin languages
These languages uses alphabetic non-latin scripts (greek, cyrylic). Words are separated by whitespace.

|    language     | training sentences | sentence-detector |     tokenizer      |     pos-tagger     |   lemmatizer   |
| :-------------: | :----------------: | :---------------: | :----------------: | :----------------: | :------------: |
|   el<br>greek   |         2k         | MAXENT_QN<br>0.90 | MAXENT_QN<br>0.99  | PERCEPTRON<br>0.95 | MAXENT<br>0.95 |
|  ru<br>russian  |        99k         | MAXENT_QN<br>0.93 | MAXENT_QN<br>0.99  |   MAXENT<br>0.96   | MAXENT<br>0.97 |
| uk<br>ukrainian |         6k         |  MAXENT<br>0.91   | PERCEPTRON<br>0.99 |   MAXENT<br>0.94   | MAXENT<br>0.94 |

These types of languages are also well supported.

### Abjads languages
These languages are commonly written from right to left. Vovels are often omitted. Words are separated by whitespace.

|   language    | training sentences | sentence-detector  |     tokenizer     |   pos-tagger   |         lemmatizer         |
| :-----------: | :----------------: | :----------------: | :---------------: | :------------: | :------------------------: |
| ar<br>arabic  |         7k         | MAXENT_QN<br>0.71  | MAXENT_QN<br>0.97 | MAXENT<br>0.93 | Serialization<br>exception |
| he<br>hebrew  |         8k         | PERCEPTRON<br>0.94 | MAXENT_QN<br>0.92 | MAXENT<br>0.94 |       MAXENT<br>0.96       |

Evaluation score is a bit lower for these languages.
Lemmatizer model training for arabian language fails. Computed model cannot be serialized. Don't know the reason.

### South-east asian languages
These languages uses logographs/syllabic scripts. Words usually are not separated which causes problems with tokenization.

|    language    | training sentences | sentence-detector |     tokenizer      |     pos-tagger     |   lemmatizer   |
| :------------: | :----------------: | :---------------: | :----------------: | :----------------: | :------------: |
| ja<br>japanese |        16k         | MAXENT_QN<br>0.96 | NAIVEBAYES<br>0.79 | PERCEPTRON<br>0.96 | MAXENT<br>0.97 |
|  ko<br>korean  |        30k         | MAXENT_QN<br>0.94 | MAXENT_QN<br>0.99  |   MAXENT<br>0.89   | MAXENT<br>0.90 |
| zh<br>chinese  |         9k         |  MAXENT<br>0.98   | MAXENT_QN<br>0.91  | PERCEPTRON<br>0.94 | MAXENT<br>0.99 |

The results are not that impressive.
Tokenization quality for japanese is quite low. Tokenizer seems not to support well such types of languages.
Maybe if tokenizer have a dictionary of "known words" then trained model would be better.
POS tagging/lemmatization for korean language is also not good.
Chinese tokenization quality is higher than for japanese.
Chinese words are shorter than japanese words, it means that surrounding context is shorter.
This may explain why tokenizer better segments chinese words than japanese words.
