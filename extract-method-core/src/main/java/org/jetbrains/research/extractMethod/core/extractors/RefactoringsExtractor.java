package org.jetbrains.research.extractMethod.core.extractors;

import com.intellij.openapi.project.Project;

public interface RefactoringsExtractor {
    void collectSamples(Project project);
}
