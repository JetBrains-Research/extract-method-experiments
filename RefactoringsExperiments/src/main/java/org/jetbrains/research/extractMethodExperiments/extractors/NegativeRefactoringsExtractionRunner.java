package org.jetbrains.research.extractMethodExperiments.extractors;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import git4idea.GitCommit;
import git4idea.GitVcs;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethodExperiments.features.Feature;
import org.jetbrains.research.extractMethodExperiments.features.FeaturesVector;
import org.jetbrains.research.extractMethodExperiments.haas.Candidate;
import org.jetbrains.research.extractMethodExperiments.haas.HaasAlgorithm;
import org.jetbrains.research.extractMethodExperiments.metrics.MetricCalculator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.jetbrains.research.extractMethodExperiments.utils.PsiUtil.*;

/**
 * Processes repositories, gets the changes Java files from the latest commit,
 * and processes all methods to generate "negative" samples for dataset.
 * The "negative" samples are ones pieces of code that have the lowes score in terms of Haas ranking.
 */
public class NegativeRefactoringsExtractionRunner {
    private final Logger LOG = Logger.getInstance(NegativeRefactoringsExtractionRunner.class);
    private final List<String> repositoryPaths;
    private final FileWriter fileWriter;

    public NegativeRefactoringsExtractionRunner(List<String> repositoryPaths, FileWriter fw) {
        this.repositoryPaths = repositoryPaths;
        this.fileWriter = fw;
    }

    public void run() {
        for (String repoPath : repositoryPaths) {
            LOG.info("[RefactoringJudge]: Processing repo at: " + repoPath);
            try {
                collectSamples(repoPath);
            } catch (Exception e) {
                LOG.error("[RefactoringJudge]: Could not parse repository: " + repoPath);
            }
        }
        try {
            this.fileWriter.close();
        } catch (IOException e) {
            LOG.error("[RefactoringJudge]: Cannot close the file-writer.");
        }
        LOG.info("[RefactoringJudge]: Finished negative extraction");
    }

    private void collectSamples(String projectPath) {
        Project project = ProjectUtil.openOrImport(projectPath, null, true);
        if (project == null) {
            LOG.error("[RefactoringJudge]: Could not open project " + projectPath);
            return;
        }

        ProjectLevelVcsManager vcsManager = vcsSetup(project, projectPath);
        GitRepositoryManager gitRepoManager = ServiceManager.getService(project, GitRepositoryManager.class);

        VirtualFile[] gitRoots = vcsManager.getRootsUnderVcs(GitVcs.getInstance(project));
        for (VirtualFile root : gitRoots) {
            GitRepository repo = gitRepoManager.getRepositoryForRoot(root);
            if (repo != null) {
                try {
                    List<GitCommit> gitCommits = GitHistoryUtils.history(project, root, "--all");
                    gitCommits.forEach(c -> processCommit(c, project));
                } catch (VcsException e) {
                    LOG.error("[RefactoringJudge]: Error occurred while processing commit in " + projectPath);
                }
            }
        }
    }

    private void processCommit(GitCommit commit, Project project) {
        List<PsiJavaFile> javaFiles = extractFiles(project);

        for (PsiJavaFile javaFile : javaFiles) {
            try {
                handleMethods(javaFile);
            } catch (IOException e) {
                LOG.error("[RefactoringJudge]: Cannot handle commit with ID: " + commit.getId());
            }
        }
    }

    public void handleMethods(PsiFile psiFile) throws IOException {
        @NotNull Collection<PsiMethod> psiMethods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);
        for (PsiMethod method : psiMethods) {
            HaasAlgorithm haasAlgorithm = new HaasAlgorithm(method);
            List<Candidate> candidateList = haasAlgorithm.getCandidateList();
            writeFeaturesToFile(psiFile, method, candidateList);
        }
    }

    private void writeFeaturesToFile(PsiFile psiFile, PsiMethod method, List<Candidate> candidateList) throws IOException {
        for (Candidate candidate : candidateList) {
            if (candidate != null) {
                List<PsiStatement> statementList = candidate.getStatementList();
                int beginLine = getNumberOfLine(psiFile, statementList.get(0).getTextRange().getStartOffset());
                int endLine = getNumberOfLine(psiFile, statementList.get(statementList.size() - 1).getTextRange().getEndOffset());
                MetricCalculator metricCalculator = new MetricCalculator(candidate.getStatementList(), method, beginLine, endLine);

                FeaturesVector featuresVector = metricCalculator.getFeaturesVector();
                for (int i = 0; i < featuresVector.getDimension(); i++) {
                    this.fileWriter.append(String.valueOf(featuresVector.getFeature(Feature.fromId(i))));
                    this.fileWriter.append(';');
                }
                this.fileWriter.append(String.valueOf(candidate.getScore()));
                this.fileWriter.append('\n');
            }
        }
    }
}
