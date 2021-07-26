package org.jetbrains.research.extractMethodExperiments.extractors;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import git4idea.GitCommit;
import gr.uom.java.xmi.LocationInfo;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.jetbrains.research.extractMethodExperiments.features.Feature;
import org.jetbrains.research.extractMethodExperiments.features.FeaturesVector;
import org.jetbrains.research.extractMethodExperiments.metrics.MetricCalculator;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.jetbrains.research.extractMethodExperiments.utils.PsiUtil.findMethodByName;
import static org.jetbrains.research.extractMethodExperiments.utils.PsiUtil.getNumberOfLine;

public class CustomRefactoringHandler extends RefactoringHandler {
    private final Project project;
    private final GitCommit gitCommit;
    private final String repositoryPath;
    private final FileWriter fileWriter;
    private Logger LOG = Logger.getInstance(CustomRefactoringHandler.class);

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
        LOG.error("Cannot handle commit with ID: " + commitId);
    }

    private void handleCommit(List<Refactoring> refactorings) throws IOException {
        List<Refactoring> extractMethodRefactorings = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.EXTRACT_OPERATION)
                .collect(Collectors.toList());

        List<VirtualFile> changedJavaFiles = gitCommit.getChanges().stream()
                .filter(f -> f.getVirtualFile() != null && f.getVirtualFile().getName().endsWith(".java"))
                .map(Change::getVirtualFile)
                .collect(Collectors.toList());

        for (Refactoring ref : extractMethodRefactorings) {
            ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) ref;
            UMLOperation extractedOperation = extractOperationRefactoring.getExtractedOperation();
            LocationInfo locationInfo = extractedOperation.getLocationInfo();
            for (VirtualFile file : changedJavaFiles) {
                String filePath = file.getCanonicalPath();
                String cleanRepoPath = repositoryPath.replace(".idea/misc.xml", "");
                if (filePath != null && locationInfo.getFilePath().equals(filePath.replace(cleanRepoPath, ""))) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                    if (psiFile != null) {
                        PsiMethod method = findMethodByName(psiFile, extractedOperation.getName());
                        if (method != null) {
                            writeFeaturesToFile(psiFile, method);
                        }
                    }
                }
            }
        }
    }

    private void writeFeaturesToFile(PsiFile psiFile, PsiMethod psiElement) throws IOException {
        int beginLine = getNumberOfLine(psiFile, psiElement.getTextRange().getStartOffset());
        int endLine = getNumberOfLine(psiFile, psiElement.getTextRange().getEndOffset());
        MetricCalculator metricCalculator = new MetricCalculator(psiElement, beginLine, endLine);
        FeaturesVector featuresVector = metricCalculator.getFeaturesVector();
        for (int i = 0; i < featuresVector.getDimension(); i++) {
            this.fileWriter.append(String.format("%.4f", featuresVector.getFeature(Feature.fromId(i))));
            this.fileWriter.append(';');
        }

        this.fileWriter.append('\n');
    }
}
