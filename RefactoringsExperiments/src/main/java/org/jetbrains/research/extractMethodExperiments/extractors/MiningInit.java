package org.jetbrains.research.extractMethodExperiments.extractors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.research.extractMethodExperiments.handlers.CustomRefactoringHandler;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.PrintWriter;

public class MiningInit {
    private PrintWriter out;
    private String repoURL;
    private String repoName;
    private int total = 1;
    private int current = 0;
    private Logger logger;

    public MiningInit(PrintWriter out, String repoURL, String repoName, Logger logger) {
        this.out = out;
        this.repoURL = repoURL;
        this.repoName = repoName;
        this.logger = logger;
    }

    public void run() throws Exception {
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

        Repository repo = gitService.cloneIfNotExists(repoName, repoURL);
        MetadataExtractor me = new MetadataExtractor(repo, out, logger);

        try (Git git = new Git(repo)) {
            Iterable<RevCommit> commits = git.log().all().call();
            int count = 0;
            for (RevCommit commit : commits) {
                count++;
            }
            this.total = count;
        } catch (Exception e) {
            logger.log(Level.ERROR, "Could not get commits from repository" + repoName);
        }


        miner.detectAll(repo, "master", new CustomRefactoringHandler(out, repoURL, me, total, logger));
        out.close();
    }
}
