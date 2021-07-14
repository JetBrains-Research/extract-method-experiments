package org.jetbrains.research.extractMethodExperiments.extractors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.jetbrains.research.extractMethodExperiments.models.Fragment;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;

import java.io.*;

public class FalseRefactoringsExtractor {
    private FileWriter fw;
    private Logger logger;
    private int fileCount;

    public FalseRefactoringsExtractor(FileWriter fw, Logger logger) {
        this.fw = fw;
        this.logger = logger;
        this.fileCount = 0;
    }

    public void run(final String repoName, final String repoURL) throws Exception {
        GitService gitService = new GitServiceImpl();

        Repository repo = gitService.cloneIfNotExists(repoName, repoURL);
        logger.log(Level.INFO, "Processing repo at " + repoURL);
        Git git = new Git(repo);
        try {
            Iterable<RevCommit> commits = git.log().all().call();
            RevCommit latestCommit = commits.iterator().next(); //Access first item in commits, which are stored in reverse-chrono order.
            handleCommit(repo, repoName, latestCommit);
        } catch (Exception e) {
            logger.log(Level.ERROR, "Could not parse repository " + repoName);
        }
    }

    private void handleCommit(Repository repo, String repoName, RevCommit commit) throws Exception {
        String commitId = commit.getId().getName();
        RevTree tree = commit.getTree();

        try (TreeWalk treeWalk = new TreeWalk(repo)) {
            treeWalk.reset(tree);
            while (treeWalk.next()) {
                if (treeWalk.isSubtree()) {
                    treeWalk.enterSubtree();
                } else {
                    handleFile(repo, repoName, treeWalk.getPathString(), commitId);
                }
            }
        }
    }

    private void handleFile(Repository repo, String repoName, final String filePath, final String commitId) throws Exception {
        if (!filePath.endsWith(".java")) {
            return;
        }

        //Logging after each new 100 processed files.
        if (fileCount % 100 == 0){
            logger.log(Level.INFO, String.format("Processed %d .java files, currently at repo %s", fileCount, repoName));
        }
        fileCount++;

        RevWalk revWalk = new RevWalk(repo);
        ObjectId objectId = repo.resolve(commitId);
        RevCommit commit = revWalk.parseCommit(objectId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));
        if (!treeWalk.next()) {
            return;
        }
        ObjectId objtId = treeWalk.getObjectId(0);
        ObjectLoader loader = repo.open(objtId);
        InputStream in = loader.openStream();
        StringBuilder allFileBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            while (br.ready()) {
                allFileBuilder.append(br.readLine());
                allFileBuilder.append('\n');
            }
            String fileContents = allFileBuilder.toString();
            InputStream targetStream = new ByteArrayInputStream(fileContents.getBytes());
            handleMethods(targetStream, repo, repoName, filePath);
        }
    }

    /**
     * Uses JavaParser to extract all methods from .java file
     * contained in contents as InputStream, makes a Fragment,
     * and computes its features
     */
    public void handleMethods(InputStream contents, Repository repo, String repoName, final String filePath) {
        try {
            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(MethodDeclaration n, Object arg) {
                    if (n.getBody() != null) {
                        Fragment fragment = new Fragment(n, repo, repoName, filePath, logger);
                        fragment.processFragment(1, fw);
                    }
                    super.visit(n, arg);
                }
            }.visit(JavaParser.parse(contents, "UTF-8", false), null);
        } catch (Exception e) {
            logger.log(Level.ERROR, "Could not parse a java file " + filePath);
        }
    }
}
