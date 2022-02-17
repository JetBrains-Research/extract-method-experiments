package org.jetbrains.research.extractMethod.core.extractors;

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
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jetbrains.research.extractMethod.core.utils.WriteUtil.writeAuxLocFeatures;
import static org.jetbrains.research.extractMethod.core.utils.PsiUtil.findMethodBySignature;
import static org.jetbrains.research.extractMethod.core.utils.StringUtil.calculateSignature;
import static org.jetbrains.research.extractMethod.core.utils.WriteUtil.writeCodeFragment;
import static org.jetbrains.research.extractMethod.metrics.MetricCalculator.writeFeaturesToFile;

public class CustomRefactoringHandler extends RefactoringHandler {
    private final Project project;
    private final GitCommit gitCommit;
    private final String repoFullName;
    private final String priorCommitHash;
    private final FileWriter fileWriter;
    private final Logger LOG = LogManager.getLogger(CustomRefactoringHandler.class);

    public CustomRefactoringHandler(Project project,
                                    String repoFullName,
                                    GitCommit gitCommit,
                                    String priorCommitHash,
                                    FileWriter fileWriter) {
        this.project = project;
        this.repoFullName = repoFullName;
        this.gitCommit = gitCommit;
        this.priorCommitHash = priorCommitHash;
        this.fileWriter = fileWriter;
    }

    private static String getMethodSlice(PsiFile psiFile, int beginLine, int endLine) {
        String[] fileLines = psiFile.getText().split("\n");
        List<String> resultingLines = new ArrayList<>();
        for (int i = 0; i < fileLines.length; i++)
            if (i + 1 >= beginLine && i + 1 <= endLine)
                resultingLines.add(fileLines[i]);

        return String.join("\n", resultingLines);
    }

    @Override
    public boolean skipCommit(String commitId) {
        return false;
    }

    @Override
    public void handle(String commitId, List<Refactoring> refactorings) {
        try {
            handleCommit(refactorings);
        } catch (Exception e) {
            handleException(commitId, e);
        }
    }

    public void handleException(String commitId, Exception e) {
        LOG.error(String.format("Cannot handle commit with ID %s, at project %s", commitId, project.getName()));
    }

    private void handleCommit(List<Refactoring> refactorings) throws Exception {
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
            } catch (NullPointerException e) {
                LOG.error("Cannot extract changes from commit: " + gitCommit.getId());
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
                    String extractedFragment = getMethodSlice(changedSourceJavaFiles.get(path),
                            codeLocation.getStartLine(), codeLocation.getEndLine());
                    handleFragment(dummyMethod, extractedFragment, sourceLocationInfo.getFilePath(), codeLocation.getStartLine(), codeLocation.getEndLine());

                    // TODO: check uniformity of the file-paths between neg and pos
                }
            }
        }
    }

    private void handleFragment(PsiMethod dummyPsiMethod, String codeStr, String filePath,
                                int beginLine, int endLine) throws IOException {

        writeFeaturesToFile(dummyPsiMethod, codeStr, beginLine, endLine, this.fileWriter);

        writeAuxLocFeatures(this.repoFullName, priorCommitHash, filePath, beginLine, endLine, this.fileWriter);
        this.fileWriter.append(String.format("%f;", 0.0)); // Setting haas-score as 0 for uniformity
        writeCodeFragment(codeStr, this.fileWriter);
        this.fileWriter.append('\n'); // Setting haas-score as 0 for uniformity
    }
}
