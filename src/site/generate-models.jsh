List<String> languageCodes = List.of(
    "cs",
    "de",
    "el",
    "en",
    "es",
    "fr",
    "he",
    "it",
    "ja",
    "ko",
    "pl",
    "pt",
    "ru",
    "uk",
    "zh");

List<String> modelMarkdownLines = new ArrayList<>();
modelMarkdownLines.add("# Pre-trained models");
modelMarkdownLines.add("");

String javaOpts = System.getProperty("java.opts", "");
String toolsSimpleJar = System.getProperty("tools.simple.jar", "");
String toolsLuceneJar = System.getProperty("tools.lucene.jar", "");
Path workDirectory = Path.of(System.getProperty("work.dir", "."));
Path siteDirectory = Path.of(System.getProperty("site.dir", "."));
Integer maxExecHours = Integer.parseInt(System.getProperty("max.exec.hours", "-1"));



String getResourceDirectoryName(String languageCode, String normalizerType) {
    return String.format("%s-%s", languageCode, normalizerType);
}

String getModelFileName(String languageCode, String modelType) {
    return String.format("%s-%s.onlpm", languageCode, modelType);
}

String getReportFileName(String languageCode, String modelType) {
    return String.format("%s-%s.txt", languageCode, modelType);
}

String getLanguageName(String languageCode) {
    Locale l = Locale.forLanguageTag(languageCode);
    return l != null ? l.getDisplayLanguage(Locale.ENGLISH).toLowerCase() : "?";
}

String getPropertyValue(Path reportFile, String propertyName) throws Exception {
    String prefix = propertyName + "=";
    return Files.readAllLines(reportFile).stream()
            .filter(line -> line.startsWith(prefix))
            .map(line -> line.substring(prefix.length()).trim())
            .findFirst()
            .orElse("?");
}

String getTrainingSampleSizeInThousands(Path reportFile) throws Exception {
    try {
        int sampleSize = Integer.parseInt(getPropertyValue(reportFile, "Training-Sample-Size"));
        return String.format("%sk", sampleSize / 1000);
    } catch (NumberFormatException e) {
        return "?";
    }
}

String getTrainingAlgorithm(Path reportFile) throws Exception {
    return getPropertyValue(reportFile, "Training-Algorithm");
}

String getEvaluationScore(Path reportFile) throws Exception {
    return getPropertyValue(reportFile, "Evaluation-Score");
}

String createLanguageHeader(Path workDir, String languageCode) throws Exception {
    String languageName = getLanguageName(languageCode);
    String resourceDirName = getResourceDirectoryName(languageCode, "lucene");
    String reportFileName = getReportFileName(languageCode, "lemmatizer");
    Path reportPath = workDir.resolve(resourceDirName).resolve(reportFileName);
    String trainingSampleSize = getTrainingSampleSizeInThousands(reportPath);
    return String.format("1. language code: **%s**, language name: **%s**, training sample size: **%s**", languageCode, languageName, trainingSampleSize);
}

String createModelHeader(Path workDir, String languageCode, String normalizerType) {
    String resourceDirName = getResourceDirectoryName(languageCode, normalizerType);
    return String.format("   - %s", resourceDirName);
}

String createModelLine(Path workDir, String languageCode, String normalizerType, String modelType) throws Exception {
    String resourceDirName = getResourceDirectoryName(languageCode, normalizerType);
    String modelFileName = getModelFileName(languageCode, modelType);
    String reportFileName = getReportFileName(languageCode, modelType);
    Path reportPath = workDir.resolve(resourceDirName).resolve(reportFileName);
    String algorithm = getTrainingAlgorithm(reportPath);
    String score = getEvaluationScore(reportPath);
    return String.format("     - model file: **[%s](models/%s/%s)**, evaluation report: **[%s](models/%s/%s)**, training algorithm: **%s**, evaluation score: **%s**",
            modelFileName, resourceDirName, modelFileName,
            reportFileName, resourceDirName, reportFileName,
            algorithm, score);
}

void appendModelMarkdownLines(Path workDir, String languageCode, String normalizerType, List<String> modelMarkdownLines) throws Exception {
    modelMarkdownLines.add(createModelHeader(workDir, languageCode, normalizerType));
    modelMarkdownLines.add(createModelLine(workDir, languageCode, normalizerType, "sentence-detector"));
    modelMarkdownLines.add(createModelLine(workDir, languageCode, normalizerType, "tokenizer"));
    modelMarkdownLines.add(createModelLine(workDir, languageCode, normalizerType, "pos-tagger"));
    modelMarkdownLines.add(createModelLine(workDir, languageCode, normalizerType, "lemmatizer"));
}

void appendModelMarkdown(Path workDir, String languageCode, List<String> modelMarkdownLines) throws Exception {
    modelMarkdownLines.add(createLanguageHeader(workDir, languageCode));
    appendModelMarkdownLines(workDir, languageCode, "simple", modelMarkdownLines);
    appendModelMarkdownLines(workDir, languageCode, "lucene", modelMarkdownLines);
}

void copyModelAndReport(Path workDir, String languageCode, String normalizerType, String modelType, Path siteDir) throws Exception {
    String resourceDirName = getResourceDirectoryName(languageCode, normalizerType);
    String modelFileName = getModelFileName(languageCode, modelType);
    String reportFileName = getReportFileName(languageCode, modelType);
    Path srcModelFile = workDir.resolve(resourceDirName).resolve(modelFileName);
    Path dstModelFile = siteDir.resolve("resources").resolve("models").resolve(resourceDirName).resolve(modelFileName);
    Path srcReportFile = workDir.resolve(resourceDirName).resolve(reportFileName);
    Path dstReportFile = siteDir.resolve("resources").resolve("models").resolve(resourceDirName).resolve(reportFileName);
    Files.createDirectories(dstModelFile.getParent());
    Files.copy(srcModelFile, dstModelFile, StandardCopyOption.REPLACE_EXISTING);
    Files.copy(srcReportFile, dstReportFile, StandardCopyOption.REPLACE_EXISTING);

}

void copyModelDir(Path workDir, String languageCode, String normalizerType, Path siteDirectory) throws Exception {
    copyModelAndReport(workDir, languageCode, normalizerType, "sentence-detector", siteDirectory);
    copyModelAndReport(workDir, languageCode, normalizerType, "tokenizer", siteDirectory);
    copyModelAndReport(workDir, languageCode, normalizerType, "pos-tagger", siteDirectory);
    copyModelAndReport(workDir, languageCode, normalizerType, "lemmatizer", siteDirectory);
}

void copyModelFiles(Path workDir, String languageCode, Path siteDirectory) throws Exception {
    copyModelDir(workDir, languageCode, "simple", siteDirectory);
    copyModelDir(workDir, languageCode, "lucene", siteDirectory);
}

void trainModel(Path workDir, String languageCode, String javaOpts, String jarFileName) throws Exception {
    ProcessBuilder builder = new ProcessBuilder("java", javaOpts, "-jar", jarFileName, "train", languageCode, workDir.toAbsolutePath().toString());
    builder.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT);
    Process process = builder.start();
    int exitCode = process.waitFor();
    if (exitCode != 0) {
        throw new IllegalStateException("failed");
    }
}

void abortProcessingIfTooLong(java.time.LocalDateTime startTime) {
    var currentTime = java.time.LocalDateTime.now();
    if (maxExecHours > 0 && currentTime.minusHours(maxExecHours).compareTo(startTime) > 0) {
        System.out.println(String.format("### Processing lasts longer than %d hours, aborting ###", maxExecHours));
        System.exit(1);
    }
}

if (toolsSimpleJar.length() > 0 && toolsLuceneJar.length() > 0) {
    var startTime = java.time.LocalDateTime.now();
    for (String languageCode : languageCodes) {
        try {
            abortProcessingIfTooLong(startTime);
            trainModel(workDirectory, languageCode, javaOpts, toolsSimpleJar);
            abortProcessingIfTooLong(startTime);
            trainModel(workDirectory, languageCode, javaOpts, toolsLuceneJar);
            copyModelFiles(workDirectory, languageCode, siteDirectory);
            appendModelMarkdown(workDirectory, languageCode, modelMarkdownLines);
        } catch (Exception e) {
        }
    }
    Path modelMarkdownFile = siteDirectory.resolve("markdown").resolve("models.md");
    Files.createDirectories(modelMarkdownFile.getParent());
    Files.write(modelMarkdownFile, modelMarkdownLines);
}

/exit
