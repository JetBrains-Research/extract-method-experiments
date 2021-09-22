package org.jetbrains.research.extractMethod.core.extractors;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import git4idea.GitCommit;
import git4idea.GitVcs;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethod.core.haas.Candidate;
import org.jetbrains.research.extractMethod.core.haas.HaasAlgorithm;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.jetbrains.research.extractMethod.metrics.MetricCalculator.writeFeaturesToFile;
import static org.jetbrains.research.extractMethod.core.utils.PsiUtil.*;
import static org.jetbrains.research.extractMethod.core.utils.PsiUtil.getNumberOfLine;

/**
 * Processes repositories, gets the changes Java files from the latest commit,
 * and processes all methods to generate "negative" samples for dataset.
 * The "negative" samples are ones pieces of code that have the lowes score in terms of Haas ranking.
 */
public class NegativeRefactoringsExtractionRunner {
    private final Logger LOG = LogManager.getLogger(NegativeRefactoringsExtractionRunner.class);
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
            handleCandidates(psiFile, method, candidateList);
        }
    }

    private void handleCandidates(PsiFile psiFile, PsiMethod method, List<Candidate> candidateList) throws IOException {
        for (Candidate candidate : candidateList) {
            if (candidate != null) {
                List<PsiStatement> statementList = candidate.getStatementList();
                int beginLine = getNumberOfLine(psiFile, statementList.get(0).getTextRange().getStartOffset());
                int endLine = getNumberOfLine(psiFile, statementList.get(statementList.size() - 1).getTextRange().getEndOffset());

                String repoName = psiFile.getProject().getName();

                String statementsString = statementsAsStr(candidate.getStatementList());

                writeFeaturesToFile(method, statementsString, repoName, beginLine, endLine, this.fileWriter);
                this.fileWriter.append(String.format(";%f\n", candidate.getScore()));
            }
        }
    }

    public static String statementsAsStr(List<PsiStatement> statementList) {
        StringBuilder result = new StringBuilder();
        for (PsiStatement statement : statementList) {
            result.append(statement.getText());
            result.append('\n');
        }
        return result.toString().strip();
    }
}
