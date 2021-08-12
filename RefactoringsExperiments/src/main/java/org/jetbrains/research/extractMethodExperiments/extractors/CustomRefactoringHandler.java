package org.jetbrains.research.extractMethodExperiments.extractors;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.jetbrains.research.extractMethodExperiments.utils.PsiUtil.findMethodBySignature;
import static org.jetbrains.research.extractMethodExperiments.utils.PsiUtil.getNumberOfLine;
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
        List<Change> changes = gitCommit.getChanges().stream().filter(f -> f.getVirtualFile().getName().endsWith(".java")).collect(Collectors.toList());
        List<PsiFile> changedSourceJavaFiles = new ArrayList<>();
        List<PsiFile> changedExtractedJavaFiles = new ArrayList<>();

        for(Change change : changes){
            try {
                PsiFile sourcePsiFile = PsiFileFactory.getInstance(project).createFileFromText("tmp",
                        JavaFileType.INSTANCE,
                        change.getBeforeRevision().getContent());
                changedSourceJavaFiles.add(sourcePsiFile);

                PsiFile extractedPsiFile = PsiFileFactory.getInstance(project).createFileFromText("tmp",
                        JavaFileType.INSTANCE,
                        change.getAfterRevision().getContent());
                changedExtractedJavaFiles.add(extractedPsiFile);
            } catch (VcsException e) {
                e.printStackTrace();
            }
        }

        for (Refactoring ref : extractMethodRefactorings) {
            ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) ref;
            UMLOperation extractedOperation = extractOperationRefactoring.getExtractedOperation();
            UMLOperation sourceOperation = extractOperationRefactoring.getSourceOperationBeforeExtraction();
            LocationInfo sourceLocationInfo = sourceOperation.getLocationInfo();
            LocationInfo extractedLocationInfo = extractedOperation.getLocationInfo();
            for (PsiFile file : changedSourceJavaFiles) {
                String filePath = file.getVirtualFile().getPath();
                String cleanRepoPath = repositoryPath.replace(".idea/misc.xml", "");
                PsiFile sourcePsiFile = null;
                PsiFile extractedPsiFile = null;
                if (filePath != null && sourceLocationInfo.getFilePath().equals(filePath.replace(cleanRepoPath, ""))) {
                    sourcePsiFile = PsiManager.getInstance(project).findFile(file);
                }
                if (filePath != null && extractedLocationInfo.getFilePath().equals(filePath.replace(cleanRepoPath, ""))) {
                    extractedPsiFile = PsiManager.getInstance(project).findFile(file);
                }
                if (sourcePsiFile != null && extractedPsiFile != null) {
                    PsiMethod newMethod = findMethodBySignature(extractedPsiFile, calculateSignature(extractedOperation));
                    PsiMethod oldMethod = findMethodBySignature(sourcePsiFile, calculateSignature(sourceOperation));
                    if (oldMethod != null && newMethod != null) {
                        writeFeaturesToFile(oldMethod, newMethod.getBody().getStatements());
                    }
                }
            }



        }
    }


    private void writeFeaturesToFile(PsiMethod sourcePsiMethod, PsiStatement[] statements) throws IOException {
        PsiFile psiFile = sourcePsiMethod.getContainingFile();
        int beginLine = getNumberOfLine(psiFile, sourcePsiMethod.getTextRange().getStartOffset());
        int endLine = getNumberOfLine(psiFile, sourcePsiMethod.getTextRange().getEndOffset());
        MetricCalculator metricCalculator = new MetricCalculator(Arrays.asList(statements), sourcePsiMethod, beginLine, endLine);
        FeaturesVector featuresVector = metricCalculator.getFeaturesVector();

        for (int i = 0; i < featuresVector.getDimension(); i++) {
            this.fileWriter.append(String.valueOf(featuresVector.getFeature(Feature.fromId(i))));
            if (i != featuresVector.getDimension() - 1)
                this.fileWriter.append(';');
        }

        this.fileWriter.append('\n');
    }

}
