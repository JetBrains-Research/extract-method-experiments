package ru.hse.kirilenko.refactorings.extractors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import jdk.internal.joptsimple.internal.Strings;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import ru.hse.kirilenko.refactorings.csv.SparseCSVBuilder;
import ru.hse.kirilenko.refactorings.csv.models.CSVItem;
import ru.hse.kirilenko.refactorings.csv.models.Feature;
import ru.hse.kirilenko.refactorings.csv.models.Fragment;
import ru.hse.kirilenko.refactorings.utils.calcers.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.System.exit;
import static ru.hse.kirilenko.refactorings.utils.trie.NodeUtils.locsString;

public class FalseRefactoringsExtractor {

    public void run(final String repoName, final String repoURL) throws Exception {
        GitService gitService = new GitServiceImpl();

        Repository repo = gitService.cloneIfNotExists(repoName, repoURL);
        System.out.printf("git repo: %s\n", repoName);

        try (Git git = new Git(repo)) {
            Iterable<RevCommit> commits = git.log().all().call();
            RevCommit latestCommit = commits.iterator().next(); //Access first item in commits, which are stored in reverse-chrono order.
            handleCommit(repo, latestCommit);
        } catch (Exception e){
            System.err.printf("Could not parse repository %s\n", repoURL);
        }
    }

    private void handleCommit(Repository repo, RevCommit commit) throws Exception {
        String commitId = commit.getId().getName();
        System.out.printf("COMMIT ID: %s\n", commitId);

        RevTree tree = commit.getTree();

        try (TreeWalk treeWalk = new TreeWalk(repo)) {
            treeWalk.reset(tree);
            while (treeWalk.next()) {
                if (treeWalk.isSubtree()) {
                    treeWalk.enterSubtree();
                } else {
//                    System.out.printf("what is in treeWalk: %s\n", treeWalk.getPathString());
                    handleFile(repo, treeWalk.getPathString(), commitId);
//                    exit(0);
                }
            }
        }
    }


    private void handleFile(Repository repo, final String filePath, final String commitId) throws Exception {
        if (!filePath.endsWith(".java")) {
            return;
        }

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
            handleMethods(targetStream, repo, filePath, commitId);
        }
    }

    /**Uses JavaParser to extract all methods from .java file
     * contained in contents as InputStream, makes a Fragment,
     * and computes its features*/
    public static void handleMethods(InputStream contents, Repository repo, final String filePath, final String commitId) throws Exception {
        try {
            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(MethodDeclaration n, Object arg) {
                    if(n.getBody() != null){
                        System.out.println(n.getBody().toString());
//                        exit(0);
//                        Fragment fragment = new Fragment(n, repo, filePath, commitId);
//                        fragment.computeFeatures();
//                        fragment.writeFeatures();
                    }
                    super.visit(n, arg);


                }
            }.visit(JavaParser.parse(contents), null);
        } catch (ParseException e) {
            System.err.println("Could not parse a java file");
            e.printStackTrace();
        }
    }
}
