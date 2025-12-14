package nz.ac.canterbury.seng302.homehelper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Map.entry;

/**
 * Service responsible for detecting profanity in messages. Profanity is detected on a custom local filter based on
 * a word list then checked against ProfanityAPI (External API). The local implementation is modified from
 * https://gist.github.com/PimDeWitte/c04cc17bc5fa9d7e3aee6670d4105941
 */
@Service
public class ProfanityService {
    private final Logger logger = LogManager.getLogger(ProfanityService.class);

    /**
     * Map of common leetspeak symbol substitutions for profanity. Extended with suggestions from ChatGPT.
     */
    private static final Map<Character, Character> SYMBOL_SUBSTITUTIONS = Map.<Character, Character>ofEntries(
            entry('@', 'a'), entry('4', 'a'), entry('^', 'a'), entry('$', 's'),
            entry('5', 's'), entry('!', 'i'), entry('1', 'i'), entry('|', 'i'),
            entry('l', 'i'), entry('3', 'e'), entry('&', 'e'), entry('€', 'e'),
            entry('0', 'o'), entry('9', 'g'), entry('6', 'g'), entry('7', 't'),
            entry('+', 't'), entry('2', 'z'), entry('(', 'c'), entry('<', 'c'),
            entry('[', 'c'), entry('}', 'd'), entry('?', 'y'), entry('%', 'x'),
            entry('#', 'h'), entry('£', 'l'),

            entry('á', 'a'), entry('à', 'a'), entry('â', 'a'), entry('ä', 'a'),
            entry('ã', 'a'), entry('å', 'a'), entry('ā', 'a'), entry('é', 'e'),
            entry('è', 'e'), entry('ê', 'e'), entry('ë', 'e'), entry('ē', 'e'),
            entry('í', 'i'), entry('ì', 'i'), entry('î', 'i'), entry('ï', 'i'),
            entry('ī', 'i'), entry('ó', 'o'), entry('ò', 'o'), entry('ô', 'o'),
            entry('ö', 'o'), entry('õ', 'o'), entry('ō', 'o'), entry('ú', 'u'),
            entry('ù', 'u'), entry('û', 'u'), entry('ü', 'u'), entry('ū', 'u'),
            entry('ç', 'c'), entry('ñ', 'n'), entry('ß', 'b'), entry('ž', 'z'),
            entry('š', 's')
    );


    private static final String API_URL = "https://vector.profanity.dev";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Pattern wordPattern = Pattern.compile("[^\\s\\p{Punct}]+");
    private Set<String> badWords;
    private int longestBadWord;

    /**
     * Initializes the profanity word list from a resource file.
     */
    @PostConstruct
    public void init() {
        // read the bad words from a text file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/profanity.txt")), StandardCharsets.UTF_8))) {

            badWords = reader.lines()
                    .map(line -> line.trim().toLowerCase())
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toSet());
            longestBadWord = badWords.stream()
                    .mapToInt(String::length)
                    .max()
                    .orElse(0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load bad words", e);
        }
    }

    /**
     * Checks if a message contains profanity. It will first check via the custom implementation and then ProfanityAPI
     * if the custom implementation did not detect anything.
     *
     * @param message The message to be checked.
     * @return true if profanity is found, false otherwise.
     */
    public boolean containsProfanity(String message) {
        return checkProfanityLocal(message) || checkProfanityAPI(message);
    }

    /**
     * Checks a message against a locally loaded set of bad words. This method has been modified from
     * https://gist.github.com/PimDeWitte/c04cc17bc5fa9d7e3aee6670d4105941
     *
     * @param message The message to be checked.
     * @return true if profanity is found, false otherwise.
     */
    public boolean checkProfanityLocal(String message) {
        String cleaned = normalizeMessage(message);
        Matcher matcher = wordPattern.matcher(cleaned);
        for (MatchResult match : matcher.results().toList()) {
            String token = match.group();
            int start = match.start();
            for (int offset = 0; offset < token.length(); offset++) {
                for (int length = 1; length <= token.length() - offset && length <= longestBadWord; length++) {
                    String wordToCheck = token.substring(offset, offset + length);
                    if (badWords.contains(wordToCheck)) {
                        if (isStandaloneWord(cleaned, start + offset, start + offset + length)) {
                            logger.info("Local Profanity check failed on word: '{}'", wordToCheck);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if a substring of the message is a standalone word. This prevents words such as "assignment" being
     * detected as "ass".
     *
     * @param input The message to be checked.
     * @param start The start index of the word.
     * @param end   The end index of the word.
     * @return true if the word is standalone, false otherwise.
     */
    private boolean isStandaloneWord(String input, int start, int end) {
        boolean isStartValid = start == 0 || !Character.isLetterOrDigit(input.charAt(start - 1));
        boolean isEndValid = end == input.length() || !Character.isLetterOrDigit(input.charAt(end));
        return isStartValid && isEndValid;
    }

    /**
     * Checks a message against ProfanityAPI (an external API). If the API requests fails, it is handled
     * gracefully and returns false to indicate no profanity was detected.
     *
     * @param message The message to be checked.
     * @return true if profanity is found, false otherwise.
     */
    public boolean checkProfanityAPI(String message) {
        try {
            ObjectNode jsonObject = objectMapper.createObjectNode();
            jsonObject.put("message", "My tag is " + message);
            String body = objectMapper.writer().writeValueAsString(jsonObject);

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode jsonNode = objectMapper.readTree(response.body());
                    boolean isProfanity = jsonNode.get("isProfanity").asBoolean();
                    if (isProfanity) {
                        logger.info("API Profanity check failed on message: '{}'", message);
                    }
                    return isProfanity;
                }
                logger.warn("Unable to detect profanity. Received response code {}: {}",
                        response.statusCode(), response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to detect profanity", e);
            return false;
        }
    }

    /**
     * Normalises the input message by lowercasing, replacing common leetspeak/symbols with letters, and removing
     * punctuation.
     *
     * @param input The raw user input.
     * @return The normalized version of the input string.
     */
    private String normalizeMessage(String input) {
        StringBuilder cleaned = new StringBuilder();
        input = input.toLowerCase();

        for (char c : input.toCharArray()) {
            if (Character.isLetter(c) || Character.isSpaceChar(c)) {
                cleaned.append(c);
            } else if (SYMBOL_SUBSTITUTIONS.containsKey(c)) {
                cleaned.append(SYMBOL_SUBSTITUTIONS.get(c));
            }
        }
        return cleaned.toString();
    }
}

