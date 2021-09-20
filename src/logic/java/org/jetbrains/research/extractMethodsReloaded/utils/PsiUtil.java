package org.jetbrains.research.extractMethodsReloaded.utils;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.TimeoutUtil;
import git4idea.GitVcs;
import git4idea.config.GitVcsApplicationSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static org.jetbrains.research.extractMethodsReloaded.utils.StringUtil.calculateSignature;

public class PsiUtil {
    private static final String FILE_TYPE_NAME = "JAVA";
    private static final Logger LOG = Logger.getInstance(PsiUtil.class);

    public static int getNumberOfLine(PsiFile file, int offset) {
        FileViewProvider fileViewProvider = file.getViewProvider();
        Document document = fileViewProvider.getDocument();
        return document != null ? document.getLineNumber(offset) + 1 : 0;
    }

    public static PsiMethod findMethodByName(PsiFile psiFile, String methodName) {
        Collection<PsiMethod> psiMethods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);
        for (PsiMethod psiMethod : psiMethods) {
            if (psiMethod.getName().equals(methodName)) {
                return psiMethod;
            }
        }
        return null;
    }

    public static PsiMethod findMethodBySignature(PsiFile psiFile, String methodSignature) {
        Collection<PsiMethod> psiMethods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);
        for (PsiMethod psiMethod : psiMethods) {
            if (Objects.equals(calculateSignature(psiMethod), methodSignature)) {
                return psiMethod;
            }
        }
        return null;
    }

    /**
     * Setups VCS to get access to the project's Git root
     */
    public static ProjectLevelVcsManagerImpl vcsSetup(Project project, String projectPath) {
        VfsUtil.markDirtyAndRefresh(false, true, false, new File(projectPath));
        ProjectLevelVcsManagerImpl vcsManager = (ProjectLevelVcsManagerImpl) ProjectLevelVcsManager.getInstance(project);
        vcsManager.waitForInitialized();
        TimeoutUtil.sleep(10000);
        @NotNull GitVcs vcs = GitVcs.getInstance(project);
        try {
            vcs.doActivate();
        } catch (VcsException e) {
            LOG.error("[RefactoringJudge]: Error occurred during VCS setup.");
        }

        GitVcsApplicationSettings appSettings = GitVcsApplicationSettings.getInstance();
        appSettings.setPathToGit(findGitExecutable());
        return vcsManager;
    }

    public static String findGitExecutable() {
        return findExecutable("Git", "git", "git.exe", Collections.singletonList("IDEA_TEST_GIT_EXECUTABLE"));
    }

    @NotNull
    private static String findExecutable(@NotNull String programName,
                                         @NotNull String unixExec,
                                         @NotNull String winExec,
                                         @NotNull Collection<String> envs) {
        String exec = findEnvValue(envs);
        if (exec != null) {
            return exec;
        }
        File fileExec = PathEnvironmentVariableUtil.findInPath(SystemInfo.isWindows ? winExec : unixExec);
        if (fileExec != null) {
            return fileExec.getAbsolutePath();
        }
        throw new IllegalStateException(programName + " executable not found. " + (envs.size() > 0 ?
                "Please define a valid environment variable " +
                        envs.iterator().next() +
                        " pointing to the " +
                        programName +
                        " executable." : ""));
    }

    @Nullable
    private static String findEnvValue(@NotNull Collection<String> envs) {
        for (String env : envs) {
            String val = System.getenv(env);
            if (val != null && new File(val).canExecute()) {
                return val;
            }
        }
        return null;
    }

    public static List<PsiJavaFile> extractFiles(Project project) {
        final List<PsiJavaFile> javaFiles = new ArrayList<>();

        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(
                (VirtualFile file) -> {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                    if (psiFile instanceof PsiJavaFile && !psiFile.isDirectory()
                            && FILE_TYPE_NAME.equals(psiFile.getFileType().getName())) {
                        javaFiles.add((PsiJavaFile) psiFile);
                    }
                    return true;
                }
        );
        return javaFiles;
    }
}
