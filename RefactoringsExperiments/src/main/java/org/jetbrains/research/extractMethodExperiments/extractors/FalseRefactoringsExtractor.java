package org.jetbrains.research.extractMethodExperiments.extractors;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import git4idea.GitCommit;
import git4idea.GitVcs;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethodExperiments.haas.Candidate;
import org.jetbrains.research.extractMethodExperiments.haas.HaasAlgorithm;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Processes repositories, gets the changes Java files from the latest commit,
 * and processes all methods to generate "false" examples for dataset.
 * The "false" examples are ones pieces of code that have the lowes score in terms of Haas ranking.
 */
public class FalseRefactoringsExtractor {
    private final Logger LOG = Logger.getInstance(FalseRefactoringsExtractor.class);
    private final List<String> repositoryPaths;

    public FalseRefactoringsExtractor(List<String> repositoryPaths) {
        this.repositoryPaths = repositoryPaths;
    }

    public void run() {
        for (String path : repositoryPaths) {
            LOG.info("Processing repo at: " + path);
            try {
                collectProjectExamples(path);
            } catch (Exception e) {
                LOG.error("Could not parse repository: " + path);
            }
        }
    }

    private void collectProjectExamples(String projectPath) {
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
                        List<GitCommit> gitCommits = GitHistoryUtils.history(project, root, "--all");
                        //process the latest commit
                        processCommit(gitCommits.get(gitCommits.size() - 1), project);
                    } catch (VcsException e) {
                        LOG.error("Error occurred while processing the latest commit in " + projectPath);
                    }
                }
            }
        });
    }

    private void processCommit(GitCommit commit, Project project) {
        List<VirtualFile> changedJavaFiles = commit.getChanges().stream()
                .filter(c -> c.getVirtualFile() != null && c.getVirtualFile().getName().endsWith(".java"))
                .map(Change::getVirtualFile)
                .collect(Collectors.toList());

        for (VirtualFile virtualFile : changedJavaFiles) {
            PsiFile psiFile = buildPsiFile(project, virtualFile.getCanonicalPath());
            handleMethods(psiFile);
        }
    }

    public void handleMethods(PsiFile psiFile) {
        @NotNull Collection<PsiMethod> psiMethods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);
        for (PsiMethod method : psiMethods) {
            HaasAlgorithm haasAlgorithm = new HaasAlgorithm(method);
            List<Candidate> candidateList = haasAlgorithm.getCandidateList();
            //TODO: rank candidates and calculate code metrics
        }
    }

    public PsiFile buildPsiFile(Project project, String content) {
        PsiFileFactory factory = PsiFileFactory.getInstance(project);
        return factory.createFileFromText(JavaLanguage.INSTANCE, content);
    }
}
