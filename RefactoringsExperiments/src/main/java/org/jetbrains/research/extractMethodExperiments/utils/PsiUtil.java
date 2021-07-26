package org.jetbrains.research.extractMethodExperiments.utils;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import git4idea.GitVcs;
import git4idea.config.GitVcsApplicationSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class PsiUtil {
    private static Logger LOG = Logger.getInstance(PsiUtil.class);

    public static int getNumberOfLine(PsiFile file, int offset) {
        FileViewProvider fileViewProvider = file.getViewProvider();
        Document document = fileViewProvider.getDocument();
        return document != null ? document.getLineNumber(offset) + 1 : 0;
    }

    /**
     * Setups VCS to get access to the project's Git root
     */
    public static ProjectLevelVcsManagerImpl vcsSetup(Project project, String projectPath) {
        VfsUtil.markDirtyAndRefresh(false, true, false, new File(projectPath));
        ProjectLevelVcsManagerImpl vcsManager = (ProjectLevelVcsManagerImpl) ProjectLevelVcsManager.getInstance(project);
        vcsManager.waitForInitialized();
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
}
