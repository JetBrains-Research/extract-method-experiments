package org.jetbrains.research.extractMethodExperiments.legacy;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.jetbrains.research.extractMethodExperiments.csv.SparseCSVBuilder;
import org.jetbrains.research.extractMethodExperiments.csv.models.CSVItem;
import org.jetbrains.research.extractMethodExperiments.csv.models.Feature;
import org.jetbrains.research.extractMethodExperiments.utils.calcers.*;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.jetbrains.research.extractMethodExperiments.utils.trie.NodeUtils.locsString;

public class LegacyFalseRefactoringsExtractor {

    public void run(final String repoName, final String repoURL) throws Exception {
        GitService gitService = new GitServiceImpl();

        Repository repo = gitService.cloneIfNotExists(repoName, repoURL);
        System.out.printf("git repo: %s\n", repoName);

        try (Git git = new Git(repo)) {
            Iterable<RevCommit> commits = git.log().all().call();
            Iterator<RevCommit> it = commits.iterator();
            ArrayList<RevCommit> allCommits = new ArrayList<>();
            while (it.hasNext()) {
                handleCommit(repo, it.next());
            }
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
                    handleFile(repo, treeWalk.getPathString(), commitId);
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

            InputStream stream = new ByteArrayInputStream(allFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
            try {
                CompilationUnit root = JavaParser.parse(stream);
                traverse(repo, filePath, root, root);
            } catch (NullPointerException npe) {
                //skip parse error files
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void traverse(Repository repo, String filePath, CompilationUnit root, Node cur) throws GitAPIException {
        if (cur instanceof MethodDeclaration) {
            MethodDeclaration md = (MethodDeclaration) cur;
            BlockStmt body = md.getBody();
            if (body == null) {
                return;
            }
            traverseBlock(repo, filePath, root, md, body, 0, 1);

        } else {
            for (Node n : cur.getChildrenNodes()) {
                traverse(repo, filePath, root, n);
            }
        }
    }

    void traverseBlock(Repository repo, String filePath, CompilationUnit root, MethodDeclaration md, Statement blk, int dep, int k) throws GitAPIException {
        if (!canExtract(blk, dep)) {
            return;
        }

        int count = countStmts(blk);

        List<Statement> next = new ArrayList<>();
        List<Statement> seqAll = new ArrayList<>();
        for (Node n : blk.getChildrenNodes()) {
            if (n instanceof Statement) {
                Statement s = (Statement) n;
                seqAll.add(s);


                int cnt2 = countStmts(s);
                if (cnt2 <= 15 && cnt2 >= k && count - cnt2 >= k) {
                    analyzeBlock(repo, root, md, s, filePath);
                }
                if (cnt2 >= 2 * k) {
                    next.add(s);
                }
            }

        }

        int N = seqAll.size();
        for (int i = 0; i < N; ++i) {
            for (int j = i + 1; j < N; ++j) {
                List<Statement> seq = new ArrayList<>();
                for (int l = i; l <= j; ++l) {
                    seq.add(seqAll.get(l));
                }
                int cnt2 = countStmtsSeq(seq);
                if (cnt2 <= 10 && cnt2 >= k && count - cnt2 >= k) {
                    analyzeStatementsSeq(repo, root, md, seq, filePath);
                }
            }
        }

        for (Statement statement : next) {
            traverseBlock(repo, filePath, root, md, statement, dep + 1, k);
        }
    }

    private void addMd(MethodDeclaration md) {
        String fragment = md.toString();
        String linearFragment = fragment.replace('\n', ' ');
        int fragLocs = StringUtils.countMatches(fragment, "\n") + 1;

        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodDeclarationSymbols, linearFragment.length()));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodDeclarationAverageSymbols, (double) linearFragment.length() / fragLocs));
        analyzeDepth(fragment, fragLocs, false);
    }

    int countStmtsSeq(List<Statement> statement) {
        int res = 0;
        for (Statement s : statement) {
            res += countStmts(s);
        }

        return res;
    }

    int countStmts(Statement statement) {
        if (statement.getChildrenNodes().isEmpty()) {
            return 1;
        } else {
            int res = 1;
            for (Node s : statement.getChildrenNodes()) {
                if (s instanceof Statement) {
                    res += countStmts((Statement) s);
                }

            }

            return res;
        }
    }

    boolean canExtract(Statement blk, int dep) {
        List<Node> l = blk.getChildrenNodes();
        if (l == null) {
            return false;
        }

        int ans = 0;
        for (Node n : l) {
            if (n instanceof Statement) {
                ++ans;
            }
        }

        return ans >= 2 && dep + ans >= 4;
    }

    void analyzeStatementsSeq(Repository repo, CompilationUnit root, MethodDeclaration md, List<Statement> seq, String filePath) throws GitAPIException {
        if (seq.isEmpty()) {
            return;
        }

        SparseCSVBuilder.sharedInstance.writeVector(false);
        calcConnectivity(root, md, seq);

        String fragment = buildFragmentFromSeq(seq);
        String linearFragment = fragment.replace('\n', ' ');
        int fragLocs = StringUtils.countMatches(fragment, "\n") + 1;
        KeywordsCalculator.calculateCSV(linearFragment, fragLocs);

        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalSymbolsInCodeFragment, linearFragment.length()));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.AverageSymbolsInCodeLine, (double) linearFragment.length() / fragLocs));
        analyzeDepth(fragment, fragLocs, true);

        GitBlameAnalyzer.extractLineAuthorAndCreationDate(repo, seq.get(0).getBeginLine(), seq.get(seq.size() - 1).getEndLine(), filePath);
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalLinesOfCode, fragLocs));
        addMd(md);
    }

    void analyzeBlock(Repository repo, CompilationUnit root, MethodDeclaration md, Statement blk, String filePath) throws GitAPIException {
        SparseCSVBuilder.sharedInstance.writeVector(false);
        calcConnectivity(root, md, Collections.singletonList(blk));

        String fragment = blk.toString();
        String linearFragment = fragment.replace('\n', ' ');
        int fragLocs = StringUtils.countMatches(fragment, "\n") + 1;
        KeywordsCalculator.calculateCSV(linearFragment, fragLocs);

        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalSymbolsInCodeFragment, linearFragment.length()));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.AverageSymbolsInCodeLine, (double) linearFragment.length() / fragLocs));
        analyzeDepth(fragment, fragLocs, true);

        GitBlameAnalyzer.extractLineAuthorAndCreationDate(repo, blk.getBeginLine(), blk.getEndLine(), filePath);
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalLinesOfCode, fragLocs));
        addMd(md);
    }

    void calcConnectivity(CompilationUnit root, MethodDeclaration md, List<Statement> statementsSeq) {
        String fragment = buildFragmentFromSeq(statementsSeq);
        int fragmentLocs = locsString(fragment);
        MembersSets members = new MemberSetsGenerator().instanceMembers(root);

        int totalConnectivity = CouplingCalculator.calcConnectivity(fragment, members.total);
        int methodConnectivity = CouplingCalculator.calcConnectivity(fragment, members.methods);
        int fieldsConnectivity = CouplingCalculator.calcConnectivity(fragment, members.fields);

        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.FieldConnectivity, fieldsConnectivity));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.FieldConnectivityPerLine, (double) fieldsConnectivity / fragmentLocs));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalConnectivity, totalConnectivity));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalConnectivityPerLine, (double) totalConnectivity / fragmentLocs));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodConnectivity, methodConnectivity));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodConnectivityPerLine, (double) methodConnectivity / fragmentLocs));
    }

    void analyzeDepth(String code, int locCount, boolean isCodeFragment) {
        int dep = 0;
        int area = 0;
        int depInLine = 0;
        for (Character ch : code.toCharArray()) {
            if (ch == '{') {
                dep++;
                depInLine++;
            } else if (ch == '}') {
                dep--;
                depInLine--;
            } else if (ch == '\n') {
                int resDep = dep;
                if (depInLine > 0) {
                    resDep--;
                }
                depInLine = 0;
                area += resDep;
            }
        }

        if (isCodeFragment) {
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalLinesDepth, area));
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.AverageLinesDepth, (double) area / locCount));
        } else {
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodDeclarationDepth, area));
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodDeclarationDepthPerLine, (double) area / locCount));
        }

    }

    private String buildFragmentFromSeq(List<Statement> statementsSeq) {
        StringBuilder fragmentBuilder = new StringBuilder();
        for (Statement stmt : statementsSeq) {
            fragmentBuilder.append(stmt.toStringWithoutComments());
            fragmentBuilder.append('\n');
        }
        return fragmentBuilder.toString();
    }
}