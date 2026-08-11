package com.lab.statistics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WordStatisticsService {
	 // liczenie pełnej mapy słów - bez limitu i sortowania
		public Map<String, Long> getFullWordCounts(Path path) {
		    try (BufferedReader reader = Files.newBufferedReader(path)) {
		        return reader.lines()
		                .map(line -> line.split("\\s+"))
		                .flatMap(Arrays::stream)
		                .map(String::toLowerCase)
		                .map(word -> word.replaceAll("[^a-z0-9ąęóśćżńź]+", ""))
		                .filter(word -> word.matches("[a-z0-9ąęóśćżńź]{3,}"))
		                .collect(Collectors.groupingBy(
		                        Function.identity(),
		                        Collectors.counting()
		                ));
		    } catch (IOException e) {
		        throw new RuntimeException(e);
		    }
		}

		// kosinus podobieństwa między dwoma mapami słów
		public double cosineSimilarity(Map<String, Long> a, Map<String, Long> b) {
		    // iloczyn skalarny
		    double dot = 0.0;
		    for (Map.Entry<String, Long> e : a.entrySet()) {
		        Long bv = b.get(e.getKey());
		        if (bv != null) {
		            dot += e.getValue() * bv;
		        }
		    }

		    // iloczyn norm
		    // norma wektora a
		    double normA = 0.0;
		    for (Long v : a.values()) {
		        normA += v * v;
		    }
		    normA = Math.sqrt(normA);

		    // norma wektora b
		    double normB = 0.0;
		    for (Long v : b.values()) {
		        normB += v * v;
		    }
		    normB = Math.sqrt(normB);

		    if (normA == 0.0 || normB == 0.0) {
		        return 0.0;
		    }
		    return dot / (normA * normB);
		}
		
		// pomocnicza metoda do policzenia podobieństwa jednego pliku do drugiego
		public double cosineSimilarity(Path doc, Path pattern) {
		    Map<String, Long> docCounts = getFullWordCounts(doc);
		    Map<String, Long> patternCounts = getFullWordCounts(pattern);
		    return cosineSimilarity(docCounts, patternCounts);
		}
		
		 /**
	     * Metoda zwraca najczęściej występujące słowa (ich liczbę określa wordsLimit,
	     * a słowa są sortowane względem częstotliwości ich występowania) we wskazanym pliku tekstowym.
	     */
	    public Map<String, Long> getLinkedCountedWords(Path path, int wordsLimit) {
	        try (BufferedReader reader = Files.newBufferedReader(path)) {
	            return reader.lines()
	                    // 1. Podział linii na słowa
	                    .map(line -> line.split("\\s+"))
	                    .flatMap(Arrays::stream)
	                    // 2. Konwersja do małych liter (żeby uprościć regex)
	                    .map(String::toLowerCase)
	                    // 3. Wycięcie wszystkich znaków, które nie tworzą słów
	                    .map(word -> word.replaceAll("[^a-z0-9ąęóśćżńź]+", ""))
	                    // 4. Filtrowanie słów - tylko z przynajmniej trzema znakami
	                    .filter(word -> word.matches("[a-z0-9ąęóśćżńź]{3,}"))
	                    // 5. Grupowanie słów względem liczebności ich występowania
	                    .collect(Collectors.groupingBy(
	                            Function.identity(),
	                            Collectors.counting()))
	                    .entrySet()
	                    .stream()
	                    // 6. Sortowanie względem przechowywanych w mapie wartości, malejąco
	                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
	                    // 7. Ograniczenie liczby słów do wartości z wordsLimit
	                    .limit(wordsLimit)
	                    .collect(Collectors.toMap(
	                            Map.Entry::getKey,
	                            Map.Entry::getValue,
	                            (k, v) -> {
	                                throw new IllegalStateException(String.format("Błąd! Duplikat klucza %s.", k));
	                            },
	                            LinkedHashMap::new
	                    ));
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	    }

	    // pomocnicza metoda do debugowania
	    public void printStatistics(Path path, int wordsLimit) {
	        Map<String, Long> stats = getLinkedCountedWords(path, wordsLimit);
	        System.out.printf("Statystyka słów dla pliku: %s%n", path);
	        stats.forEach((word, count) -> System.out.printf("%s = %d%n", word, count));
	    }
}
