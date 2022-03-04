package org.jetbrains.research.extractMethod.core.extractors;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethod.core.haas.Candidate;
import org.jetbrains.research.extractMethod.core.haas.HaasAlgorithm;
import org.jetbrains.research.extractMethod.metrics.MetricCalculator;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.jetbrains.research.extractMethod.metrics.location.LocationVector;
import org.jetbrains.research.extractMethod.metrics.utils.DatasetRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static org.jetbrains.research.extractMethod.core.utils.LocationUtil.buildLocationVector;
import static org.jetbrains.research.extractMethod.core.utils.PsiUtil.extractFiles;
import static org.jetbrains.research.extractMethod.core.utils.PsiUtil.getNumberOfLine;

/**
 * Processes repositories, gets the changes Java files from the latest commit,
 * and processes all methods to generate "negative" samples for dataset.
 * The "negative" samples are ones pieces of code that have the lowes score in terms of Haas ranking.
 */
public class NegativesExtractor implements RefactoringsExtractor {
    private final Logger LOG = LogManager.getLogger(NegativesExtractor.class);
    private final FileWriter fileWriter;

    public NegativesExtractor(FileWriter fw) {
        this.fileWriter = fw;
    }

    public static String statementsAsStr(List<PsiStatement> statementList) {
        StringBuilder result = new StringBuilder();
        for (PsiStatement statement : statementList) {
            result.append(statement.getText());
            result.append('\n');
        }
        return result.toString().strip();
    }

    private static String getRelativePath(Project project, PsiJavaFile file) {
        // Removes prefix "PsiDirectory:" (13 characters)
        String absolutePath = file.getContainingDirectory().toString().substring(13);
        absolutePath = absolutePath + '/' + file.getName();
        String projectPath = project.getBasePath();

        // Relativize absolute path via Paths
        Path relativeFilePath = Paths.get(projectPath).relativize(Paths.get(absolutePath));
        return relativeFilePath.toString();
    }

    @Override
    public void collectSamples(Project project, String repoFullName, String headCommitHash) {
        List<PsiJavaFile> javaFiles = extractFiles(project);
        for (PsiJavaFile javaFile : javaFiles) {
            try {
                handleMethods(javaFile, getRelativePath(project, javaFile), repoFullName, headCommitHash);
            } catch (IOException e) {
                LOG.error("Cannot process file " + javaFile.getName());
            }
        }
    }

    public void handleMethods(PsiFile psiFile, String filePath, String repoFullName, String headCommitHash) throws IOException {
        @NotNull Collection<PsiMethod> psiMethods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);
        for (PsiMethod method : psiMethods) {
            HaasAlgorithm haasAlgorithm = new HaasAlgorithm(method);
            List<Candidate> candidateList = haasAlgorithm.getCandidateList();
            handleCandidates(psiFile, method, candidateList, filePath, repoFullName, headCommitHash);
        }
    }

    private void handleCandidates(PsiFile psiFile, PsiMethod psiMethod, List<Candidate> candidateList, String filePath, String repoFullName, String headCommitHash) throws IOException {
        for (Candidate candidate : candidateList) {
            if (candidate != null) {
                List<PsiStatement> statementList = candidate.getStatementList();
                int beginLine = getNumberOfLine(psiFile, statementList.get(0).getTextRange().getStartOffset());
                int endLine = getNumberOfLine(psiFile, statementList.get(statementList.size() - 1).getTextRange().getEndOffset());

                String codeStr = statementsAsStr(candidate.getStatementList());

                FeaturesVector featuresVector = new
                        MetricCalculator(psiMethod, codeStr, beginLine, endLine).getFeaturesVector();

                LocationVector locationVector = buildLocationVector(repoFullName, headCommitHash,
                        filePath, beginLine, endLine);

                DatasetRecord jsonRecord = new DatasetRecord(featuresVector, locationVector,
                        candidate.getScore(), codeStr);
                jsonRecord.writeRecord(this.fileWriter);
            }
        }
    }
}
