package com.brov3r.protegon.modules;

import com.avrix.utils.ChatUtils;
import com.avrix.utils.PlayerUtils;
import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoPlayer;
import zombie.chat.ChatBase;
import zombie.chat.ChatMessage;
import zombie.core.raknet.UdpConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles chat message filtering and anti-cheat measures for chat interactions.
 */
public class ChatAC {
    private static final List<String> cachedBanWords = new ArrayList<>();

    /**
     * Initializing filters for prohibited words.
     */
    public static void initFilters() {
        List<String> banWordsURLList = Main.getConfig().getStringList("chatFilter.banWordsRawListUrl");

        for (String url : banWordsURLList) {
            try {
                List<String> wordsFromUrl = fetchWordsFromUrl(url);
                cachedBanWords.addAll(wordsFromUrl);
            } catch (IOException e) {
                System.err.printf("[!] Failed to fetch ban words from URL %s: %s%n", url, e.getMessage());
            }
        }
    }

    /**
     * Loads words from the specified URL.
     *
     * @param urlString The URL to load the words.
     * @return A list of words loaded from the URL.
     * @throws IOException if an I/O error occurs while loading data.
     */
    private static List<String> fetchWordsFromUrl(String urlString) throws IOException {
        List<String> words = new ArrayList<>();
        URL url = new URL(urlString);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // We split the line into words, taking into account possible separation by commas or new lines
                String[] wordsArray = line.trim().split("[,\\s]+");
                for (String word : wordsArray) {
                    if (!word.isEmpty()) {
                        words.add(word.toLowerCase()); // Convert the word to lower case and add it to the list
                    }
                }
            }
        }
        return words;
    }

    /**
     * Handles incoming chat messages, filters forbidden words, and applies anti-cheat measures.
     *
     * @param chatBase The ChatBase instance representing the chat base.
     * @param message  The ChatMessage instance containing the chat message details.
     * @return true if the chat message is allowed; false if it contains forbidden words and actions are taken.
     */
    public static boolean handleChatMessage(ChatBase chatBase, ChatMessage message) {
        String text = message.getText();
        String[] words = text.split(" ");
        IsoPlayer player = PlayerUtils.getPlayerByUsername(message.getAuthor());

        if (player == null) return false;

        // Check if chat filter is enabled and if player has rights to bypass it
        if (!Main.getConfig().getBoolean("chatFilter.isEnable") || AntiCheatUtils.isPlayerHasRights(player))
            return true;

        for (String word : words) {
            if (!isForbiddenWord(word.toLowerCase())) continue;

            AntiCheatUtils.punishPlayer(Main.getConfig().getInt("chatFilter.punishType"),
                    player,
                    Main.getConfig().getString("chatFilter.punishText").replace("<WORD>", word));

            if (Main.getConfig().getInt("chatFilter.punishType") != 0) {
                UdpConnection connection = PlayerUtils.getUdpConnectionByPlayer(player);
                if (connection == null) return false;

                // Send a blocking message to the player if punishment type is not zero
                ChatUtils.sendMessageToPlayer(connection, Main.getConfig().getString("chatFilter.blockingChatMessage")
                        .replace("<WORD>", word)
                        .replace("<SPACE>", ChatUtils.SPACE_SYMBOL));
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a word is forbidden based on configured filters.
     *
     * @param word The word to check.
     * @return true if the word is forbidden; false if allowed.
     */
    private static boolean isForbiddenWord(String word) {
        ArrayList<String> whiteWords = new ArrayList<>(List.of(Main.getConfig().getString("chatFilter.whiteListWords")
                .trim()
                .replace("\n", "")
                .replace(" ", "")
                .split(",")));

        ArrayList<String> blackWords = new ArrayList<>(List.of(Main.getConfig().getString("chatFilter.blackListWords")
                .trim()
                .replace("\n", "")
                .replace(" ", "")
                .split(",")));

        // Check against white list words
        if (whiteWords.contains(word.toLowerCase())) return false;

        // Check against black list words
        if (blackWords.contains(word.toLowerCase())) return true;

        // Check against pattern list words
        for (String template : Main.getConfig().getStringList("chatFilter.patternListWords")) {
            Pattern pattern = Pattern.compile(template);
            Matcher matcher = pattern.matcher(word);

            if (matcher.find()) return true;
        }

        return cachedBanWords.contains(word.toLowerCase());
    }
}