package org.jetbrains.research.extractMethodExperiments.utils.trie;

import java.util.HashMap;

public class TrieNode {
    private final HashMap<Character, TrieNode> children;
    private final String content;
    private boolean isWord;

    public TrieNode() {
        children = new HashMap<>();
        content = "";
        isWord = false;
    }

    public HashMap<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isWord() {
        return isWord;
    }

    public void setEndOfWord(boolean word) {
        isWord = word;
    }


}
