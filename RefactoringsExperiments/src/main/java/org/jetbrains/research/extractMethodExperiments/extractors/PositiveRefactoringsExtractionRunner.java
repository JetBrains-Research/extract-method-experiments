package org.jetbrains.research.extractMethodExperiments.extractors;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitCommit;
import git4idea.GitVcs;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.util.List;

/**
 * Runs RefactoringMiner and processes discovered "Extract Method" refactorings in project's changes history.
 */
public class PositiveRefactoringsExtractionRunner {
    private final List<String> repositoriesPaths;
    private Logger LOG = Logger.getInstance(PositiveRefactoringsExtractionRunner.class);

    public PositiveRefactoringsExtractionRunner(List<String> repositoriesPaths) {
        this.repositoriesPaths = repositoriesPaths;
    }

    public void run() {
        for (String repo : repositoriesPaths) {
            collectSamples(repo);
        }
    }

    private void collectSamples(String projectPath) {
        Project project = ProjectUtil.openOrImport(projectPath, null, true);
        if (project == null) {
            LOG.error("Could not open project " + projectPath);
            return;
        }

        ProjectLevelVcsManager vcsManager = ServiceManager.getService(project, ProjectLevelVcsManager.class);
        GitRepositoryManager gitRepoManager = ServiceManager.getService(project, GitRepositoryManager.class);

        vcsManager.runAfterInitialization(() -> {
            VirtualFile[] gitRoots = vcsManager.getRootsUnderVcs(GitVcs.getInstance(project));
            for (VirtualFile root : gitRoots) {
                GitRepository repo = gitRepoManager.getRepositoryForRoot(root);
                if (repo != null) {
                    try {
                        List<GitCommit> gitCommits = GitHistoryUtils.history(project, root, "--master");
                        gitCommits.forEach(c -> processCommit(c, project));
                    } catch (VcsException e) {
                        LOG.error("Error occurred while processing commit in " + projectPath);
                    }
                }
            }
        });
    }

    private void processCommit(GitCommit commit, Project project) {
        GitService gitService = new GitServiceImpl();
        Repository repository = null;
        try {
            repository = gitService.openRepository(project.getProjectFilePath());
        } catch (Exception e) {
            LOG.error("Error occurred while opening git repository.");
        }
        GitHistoryRefactoringMiner refactoringMiner = new GitHistoryRefactoringMinerImpl();
        refactoringMiner.detectAtCommit(repository, commit.getId().asString(),
                new CustomRefactoringHandler(project, project.getProjectFilePath(), commit));
    }

}
