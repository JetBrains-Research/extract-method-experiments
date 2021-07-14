package org.jetbrains.research.extractMethodExperiments.utils.trie;

import com.github.javaparser.ast.Node;
import org.apache.commons.lang3.StringUtils;

public class NodeUtils {
    public static int locs(Node node) {
        return node.getEnd().get().line - node.getBegin().get().line + 1;
    }

    public static int locsString(String fragment) {
        return StringUtils.countMatches(fragment, '\n') + 1;
    }
}
