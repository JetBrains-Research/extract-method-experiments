package org.jetbrains.research.extractMethod.core.extractors;

import com.intellij.openapi.project.Project;

public interface RefactoringsExtractor {
    public void collectSamples(Project project);
}
