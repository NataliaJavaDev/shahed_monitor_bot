package com.tgbot.shahedmonitorbot.tdlib;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Service
public class TemporaryDictionaryAnalyzer {

    private static final boolean ENABLED = false;

    private static final String[] PREFIXES = {
            // "б",
            // "в",
            // "г",
            // "д",
            // "м",
            // "о",
            // "п",
            // "р",
            // "с",
            // "т",
            // "у",
            // "ф",
            // "х",
            // "ш"
            // "з",
            // "л"
    };

    private static final Path EXPORTS_DIR = Path.of("exports");
    private static final Path ANALYSIS_DIR = Path.of("exports", "analysis");

    @PostConstruct
    public void analyze() {
        if (!ENABLED) {
            return;
        }

        if (!Files.exists(EXPORTS_DIR)) {
            System.out.println("Exports directory not found: " + EXPORTS_DIR.toAbsolutePath());
            return;
        }

        try {
            Files.createDirectories(ANALYSIS_DIR);

            for (String prefix : PREFIXES) {
                Map<String, Integer> counts = new HashMap<>();

                try (var paths = Files.walk(EXPORTS_DIR)) {
                    paths
                            .filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(".txt"))
                            .filter(path -> !path.startsWith(ANALYSIS_DIR))
                            .forEach(path -> analyzeFile(path, prefix, counts));
                }

                writeReport(prefix, counts);
            }

            System.out.println("Dictionary analysis completed: " + ANALYSIS_DIR.toAbsolutePath());

        } catch (IOException e) {
            System.out.println("Failed to analyze dictionary: " + e.getMessage());
        }
    }

    private void analyzeFile(
            Path path,
            String prefix,
            Map<String, Integer> counts
    ) {
        try {
            String content = Files.readString(path).toLowerCase();

            String[] words = content.split("[^а-щьюяєіїґa-z0-9]+");

            for (String word : words) {
                if (word.isBlank()) {
                    continue;
                }

                if (word.startsWith(prefix)) {
                    counts.merge(word, 1, Integer::sum);
                }
            }

        } catch (IOException e) {
            System.out.println("Failed to analyze file: " + path + " -> " + e.getMessage());
        }
    }

    private void writeReport(
            String prefix,
            Map<String, Integer> counts
    ) throws IOException {
        Path resultFile = ANALYSIS_DIR.resolve(prefix + "-words-report.txt");

        StringBuilder report = new StringBuilder();

        counts.entrySet().stream()
                .sorted(
                        Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                                .thenComparing(Map.Entry.comparingByKey())
                )
                .forEach(entry -> report.append(entry.getKey())
                        .append(" — ")
                        .append(entry.getValue())
                        .append('\n'));

        Files.writeString(resultFile, report.toString());
    }
}