package org.jetbrains.research.extractMethodExperiments.extractors;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import git4idea.GitCommit;
import gr.uom.java.xmi.LocationInfo;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.jetbrains.research.extractMethodExperiments.csv.SparseCSVBuilder;
import org.jetbrains.research.extractMethodExperiments.metrics.MetricCalculator;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;

import java.util.List;
import java.util.stream.Collectors;

import static org.jetbrains.research.extractMethodExperiments.utils.PsiUtil.getNumberOfLine;

public class CustomRefactoringHandler extends RefactoringHandler {
    private final Project project;
    private final GitCommit gitCommit;
    private final String repositoryPath;
    private Logger LOG = Logger.getInstance(CustomRefactoringHandler.class);

    public CustomRefactoringHandler(Project project,
                                    String repositoryPath,
                                    GitCommit gitCommit) {
        this.project = project;
        this.repositoryPath = repositoryPath;
        this.gitCommit = gitCommit;
    }

    @Override
    public boolean skipCommit(String commitId) {
        return false;
    }

    @Override
    public void handle(String commitId, List<Refactoring> refactorings) {
        handleCommit(refactorings);
    }

    public void handleException(String commitId, Exception e) {
        LOG.error("Cannot handle commit with ID: " + commitId);
    }

    private void handleCommit(List<Refactoring> refactorings) {
        List<Refactoring> extractMethodRefactorings = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.EXTRACT_OPERATION)
                .collect(Collectors.toList());

        List<VirtualFile> changedJavaFiles = gitCommit.getChanges().stream()
                .filter(f -> f.getVirtualFile() != null && f.getVirtualFile().getName().endsWith(".java"))
                .map(Change::getVirtualFile)
                .collect(Collectors.toList());

        for (Refactoring ref : extractMethodRefactorings) {
            SparseCSVBuilder.sharedInstance.writeVector(true);
            ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) ref;
            UMLOperation extractedOperation = extractOperationRefactoring.getExtractedOperation();
            LocationInfo locationInfo = extractedOperation.getLocationInfo();
            for (VirtualFile file : changedJavaFiles) {
                if (locationInfo.getFilePath().equals(file.getCanonicalPath())) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                    if (psiFile != null) {
                        PsiElement psiElement = psiFile.findElementAt(locationInfo.getStartOffset());
                        MetricCalculator metricCalculator = new MetricCalculator(
                                (PsiMethod) psiElement,
                                getNumberOfLine(psiFile, psiElement.getTextRange().getStartOffset()),
                                getNumberOfLine(psiFile, psiElement.getTextRange().getEndOffset()));
                        //TODO: write result feature to the file
                    }
                }
            }
        }
    }
}
