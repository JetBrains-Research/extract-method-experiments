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
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import git4idea.GitCommit;
import git4idea.GitVcs;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethodExperiments.features.Feature;
import org.jetbrains.research.extractMethodExperiments.features.FeatureItem;
import org.jetbrains.research.extractMethodExperiments.features.FeaturesVector;
import org.jetbrains.research.extractMethodExperiments.haas.Candidate;
import org.jetbrains.research.extractMethodExperiments.haas.HaasAlgorithm;
import org.jetbrains.research.extractMethodExperiments.metrics.MetricCalculator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.jetbrains.research.extractMethodExperiments.utils.PsiUtil.getNumberOfLine;

/**
 * Processes repositories, gets the changes Java files from the latest commit,
 * and processes all methods to generate "negative" samples for dataset.
 * The "negative" samples are ones pieces of code that have the lowes score in terms of Haas ranking.
 */
public class NegativeRefactoringsExtractionRunner {
    private final Logger LOG = Logger.getInstance(NegativeRefactoringsExtractionRunner.class);
    private FileWriter fileWriter;
    private final List<String> repositoryPaths;

    public NegativeRefactoringsExtractionRunner(List<String> repositoryPaths, FileWriter fw) {
        this.repositoryPaths = repositoryPaths;
        this.fileWriter = fw;
    }

    public void run() {
        for (String path : repositoryPaths) {
            LOG.info("Processing repo at: " + path);
            System.out.println("Processing repo at: " + path);
            try {
                collectProjectExamples(path);
            } catch (Exception e) {
                LOG.error("Could not parse repository: " + path);
            }
        }
        try {
            this.fileWriter.close();
        } catch (IOException e) {
            LOG.error("Cannot close the file-writer.");
        }
        System.out.println("Done");
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
            try {
                handleMethods(psiFile);
            } catch (IOException e) {
                LOG.error("Cannot handle commit with ID: " + commit.getId());
            }
        }
    }

    public void handleMethods(PsiFile psiFile) throws IOException {
        @NotNull Collection<PsiMethod> psiMethods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);
        for (PsiMethod method : psiMethods) {
            HaasAlgorithm haasAlgorithm = new HaasAlgorithm(method);
            List<Candidate> candidateList = haasAlgorithm.getCandidateList();
            // rank candidates by Haas's score
            candidateList.sort(Candidate::compareTo);
            //TODO: get the candidates with the lowest score and calculate features only for them!
            writeFeaturesToFile(psiFile, method, candidateList);
        }
    }

    private void writeFeaturesToFile(PsiFile psiFile, PsiMethod method, List<Candidate> candidateList) throws IOException {
        for(Candidate candidate : candidateList){
            List<PsiStatement> statementList = candidate.getStatementList();
            int beginLine = getNumberOfLine(psiFile, statementList.get(0).getTextRange().getStartOffset());
            int endLine = getNumberOfLine(psiFile, statementList.get(statementList.size() - 1).getTextRange().getEndOffset());
            MetricCalculator metricCalculator = new MetricCalculator(candidate.getStatementList(), method, beginLine, endLine);

            FeaturesVector featuresVector = metricCalculator.getFeaturesVector();
            for(int i = 0; i < featuresVector.getDimension(); i++){
                this.fileWriter.append(String.format("%.4f", featuresVector.getFeature(Feature.fromId(i))));
                this.fileWriter.append(';');
            }
            this.fileWriter.append(String.format("%.4f", candidate.getScore()));
            this.fileWriter.append(';');
            this.fileWriter.append('\n');
        }
    }

    public PsiFile buildPsiFile(Project project, String content) {
        PsiFileFactory factory = PsiFileFactory.getInstance(project);
        return factory.createFileFromText(JavaLanguage.INSTANCE, content);
    }
}
