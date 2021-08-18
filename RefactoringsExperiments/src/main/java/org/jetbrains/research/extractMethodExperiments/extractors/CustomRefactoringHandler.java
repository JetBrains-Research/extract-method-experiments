package org.jetbrains.research.extractMethodExperiments.extractors;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiMethod;
import git4idea.GitCommit;
import gr.uom.java.xmi.LocationInfo;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.CodeRange;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.research.extractMethodExperiments.features.Feature;
import org.jetbrains.research.extractMethodExperiments.features.FeaturesVector;
import org.jetbrains.research.extractMethodExperiments.metrics.MetricCalculator;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jetbrains.research.extractMethodExperiments.utils.PsiUtil.findMethodBySignature;
import static org.jetbrains.research.extractMethodExperiments.utils.StringUtil.calculateSignature;

public class CustomRefactoringHandler extends RefactoringHandler {
    private final Project project;
    private final GitCommit gitCommit;
    private final String repositoryPath;
    private final FileWriter fileWriter;
    private final Logger LOG = LogManager.getLogger(CustomRefactoringHandler.class);

    public CustomRefactoringHandler(Project project,
                                    String repositoryPath,
                                    GitCommit gitCommit,
                                    FileWriter fileWriter) {
        this.project = project;
        this.repositoryPath = repositoryPath;
        this.gitCommit = gitCommit;
        this.fileWriter = fileWriter;
    }

    @Override
    public boolean skipCommit(String commitId) {
        return false;
    }

    @Override
    public void handle(String commitId, List<Refactoring> refactorings) {
        try {
            handleCommit(refactorings);
        } catch (IOException e) {
            handleException(commitId, e);
        }
    }

    public void handleException(String commitId, Exception e) {
        LOG.error("[RefactoringJudge]: Cannot handle commit with ID: " + commitId);
    }

    private void handleCommit(List<Refactoring> refactorings) throws IOException {
        List<Refactoring> extractMethodRefactorings = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.EXTRACT_OPERATION)
                .collect(Collectors.toList());
        if (extractMethodRefactorings.size() == 0) return;

        List<Change> changes = gitCommit.getChanges().stream().filter(f -> f.getVirtualFile() != null &&
                f.getVirtualFile().getName().endsWith(".java")).collect(Collectors.toList());
        Map<String, PsiFile> changedSourceJavaFiles = new HashMap<>();

        if (changes.size() == 0) return;

        for (Change change : changes) {
            try {
                PsiFile sourcePsiFile = PsiFileFactory.getInstance(project).createFileFromText("tmp",
                        JavaFileType.INSTANCE,
                        change.getBeforeRevision().getContent());
                changedSourceJavaFiles.put(change.getBeforeRevision().getFile().getPath(), sourcePsiFile);
            } catch (VcsException e) {
                LOG.error("[RefactoringJudge]: Cannot extract changes from commit: " + gitCommit.getId());
            }
        }

        for (Refactoring ref : extractMethodRefactorings) {
            ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) ref;
            UMLOperation sourceOperation = extractOperationRefactoring.getSourceOperationBeforeExtraction();
            LocationInfo sourceLocationInfo = sourceOperation.getLocationInfo();
            CodeRange codeLocation = extractOperationRefactoring.getExtractedCodeRangeFromSourceOperation();
            for (String path : changedSourceJavaFiles.keySet()) {
                if (path.endsWith(sourceLocationInfo.getFilePath())) {
                    PsiMethod dummyMethod = findMethodBySignature(changedSourceJavaFiles.get(path), calculateSignature(sourceOperation));
                    String extractedCode = getMethodSlice(changedSourceJavaFiles.get(path),
                            codeLocation.getStartLine(), codeLocation.getEndLine());
                    writeFeaturesToFile(dummyMethod, sourceLocationInfo.getFilePath(),
                            extractedCode, codeLocation.getStartLine(), codeLocation.getEndLine());
                }
            }
        }
    }

    private void writeFeaturesToFile(PsiMethod dummyPsiMethod, String filePath,
                                     String code, int beginLine, int endLine) throws IOException {

        Path tmpRepoPath = Paths.get(repositoryPath);
        String repoName = tmpRepoPath.getName(tmpRepoPath.getNameCount() - 1).toString();
        MetricCalculator metricCalculator =
                new MetricCalculator(code, dummyPsiMethod, repositoryPath, filePath, beginLine, endLine);

        FeaturesVector featuresVector = metricCalculator.getFeaturesVector();

        for (int i = 0; i < featuresVector.getDimension(); i++) {
            this.fileWriter.append(String.valueOf(featuresVector.getFeature(Feature.fromId(i))));
            this.fileWriter.append(';');
        }

        this.fileWriter.append(repoName);
        this.fileWriter.append('\n');
    }

    private static String getMethodSlice(PsiFile psiFile, int beginLine, int endLine) {
        String[] fileLines = psiFile.getText().split("\n");
        List<String> resultingLines = new ArrayList<>();
        for (int i = 0; i < fileLines.length; i++) {
            if (i + 1 >= beginLine && i + 1 <= endLine) resultingLines.add(fileLines[i]);
        }
        return String.join("\n", resultingLines);
    }
}
