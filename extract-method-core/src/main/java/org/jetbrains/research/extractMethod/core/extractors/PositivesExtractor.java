package org.jetbrains.research.extractMethod.core.extractors;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitCommit;
import git4idea.GitVcs;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.FileWriter;
import java.util.List;

import static org.jetbrains.research.extractMethod.core.utils.PsiUtil.vcsSetup;

/**
 * Runs RefactoringMiner and processes discovered "Extract Method" refactorings in project's changes history.
 */
public class PositivesExtractor implements RefactoringsExtractor {
    private final FileWriter fileWriter;
    private final Logger LOG = LogManager.getLogger(PositivesExtractor.class);

    public PositivesExtractor(FileWriter fw) {
        this.fileWriter = fw;
    }

    @Override
    public void collectSamples(Project project, String repoFullName, String headCommitHash) {
        GitRepositoryManager gitRepoManager = ServiceManager.getService(project, GitRepositoryManager.class);
        ProjectLevelVcsManagerImpl vcsManager = vcsSetup(project, project.getProjectFilePath());
        VirtualFile[] gitRoots = vcsManager.getRootsUnderVcs(GitVcs.getInstance(project));
        for (VirtualFile root : gitRoots) {
            GitRepository repo = gitRepoManager.getRepositoryForRoot(root);
            if (repo != null) {
                try {
                    List<GitCommit> gitCommits = GitHistoryUtils.history(project, root, "--all");
                    gitCommits.forEach(c -> processCommit(c, project, repoFullName));
                } catch (VcsException e) {
                    LOG.error("Error occurred while processing commit in " + project.getProjectFilePath());
                }
            }
        }
    }

    private void processCommit(GitCommit commit, Project project, String repoFullName) {
        GitService gitService = new GitServiceImpl();
        Repository repository = null;
        try {
            repository = gitService.openRepository(project.getBasePath());
        } catch (Exception e) {
            LOG.error("Error occurred while opening git repository.");
        }
        GitHistoryRefactoringMiner refactoringMiner = new GitHistoryRefactoringMinerImpl();
        refactoringMiner.detectAtCommit(repository, commit.getId().asString(),
                new CustomRefactoringHandler(project,
                        project.getProjectFilePath().replace(".idea/misc.xml", ""),
                        commit, fileWriter));
    }

}
