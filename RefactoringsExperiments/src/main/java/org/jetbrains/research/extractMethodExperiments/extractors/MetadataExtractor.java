package org.jetbrains.research.extractMethodExperiments.extractors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
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
import org.jetbrains.research.extractMethodExperiments.legacy.OutputUtils;
import org.jetbrains.research.extractMethodExperiments.utils.MethodDataExtractor;
import org.jetbrains.research.extractMethodExperiments.utils.feature.generators.CouplingCalculator;
import org.jetbrains.research.extractMethodExperiments.utils.feature.generators.KeywordsCalculator;
import org.jetbrains.research.extractMethodExperiments.utils.feature.generators.MemberSetsGenerator;
import org.jetbrains.research.extractMethodExperiments.utils.feature.generators.MembersSets;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MetadataExtractor {
    private Repository repo;
    private PrintWriter out;
    private Logger logger;

    public MetadataExtractor(final Repository repo, final PrintWriter out, Logger logger) {
        this.repo = repo;
        this.out = out;
        this.logger = logger;
    }

    public Repository getRepo() {
        return repo;
    }

    public MethodDeclaration extractFragment(final String commitId,
                                             final String filePath,
                                             int firstLine,
                                             int lastLine,
                                             int firstCol,
                                             int lastCol,
                                             boolean applyLineConstraints) throws Exception {
        RevWalk revWalk = new RevWalk(repo);
        ObjectId objectId = repo.resolve(commitId);
        RevCommit commit = revWalk.parseCommit(objectId);

        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));
        if (!treeWalk.next()) {
            return null;
        }
        ObjectId objtId = treeWalk.getObjectId(0);
        ObjectLoader loader = repo.open(objtId);
        InputStream in = loader.openStream();
        StringBuilder allFileBuilder = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        int skipLines = 0;
        while (skipLines++ < firstLine) {
            allFileBuilder.append(br.readLine());
            allFileBuilder.append('\n');
        }

        if (firstLine == lastLine) {
            String line = br.readLine();
            allFileBuilder.append(line);
            allFileBuilder.append('\n');
        }

        StringBuilder codeFragmentBuilder = new StringBuilder();
        int procLines = firstLine;
        while (procLines <= lastLine) {
            String line = br.readLine();
            allFileBuilder.append(line);
            allFileBuilder.append('\n');
            if (line != null) {
                int arg1 = (applyLineConstraints && firstLine == procLines) ? firstCol : 0;
                int arg2 = (applyLineConstraints && lastLine == procLines) ? lastCol : line.length() - 1;
                String extractedLineFragment = extractLineFragment(arg1, arg2, line);
                codeFragmentBuilder.append(extractedLineFragment);
                codeFragmentBuilder.append(' ');
                OutputUtils.printLn(extractedLineFragment, out);
            }
            procLines++;
        }

        while (br.ready()) {
            allFileBuilder.append(br.readLine());
            allFileBuilder.append('\n');
        }

        MethodDeclaration md = null;
        if (ExtractionConfig.parseJava) {
            try {
                InputStream stream = new ByteArrayInputStream(allFileBuilder.toString().getBytes(StandardCharsets.UTF_8));
                CompilationUnit root = JavaParser.parse(stream);
                MembersSets members = new MemberSetsGenerator().instanceMembers(root);
                md = traverse(root, firstCol, firstLine, lastCol, lastLine, members);
            } catch (Exception ex) {
                logger.log(Level.ERROR, "Could not parse .java file " + filePath);
            }
        }

        String codeFragmentString = codeFragmentBuilder.toString();
        int fragLinesCount = lastLine - firstLine + 1;
        KeywordsCalculator.calculateCSV(codeFragmentString, fragLinesCount);

        OutputUtils.printLn("FRAGMENT LENGTH: " + codeFragmentString.length(), out);
        OutputUtils.printLn("FRAGMENT LINE AVG SIZE: " + (double) codeFragmentString.length() / fragLinesCount, out);
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalSymbolsInCodeFragment, codeFragmentString.length()));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.AverageSymbolsInCodeLine, (double) codeFragmentString.length() / fragLinesCount));
        analyzeDepth(allFileBuilder.toString(), firstLine, lastLine);
        return md;
    }

    MethodDeclaration traverse(Node cur, int firstColumn, int firstRow, int lastColumn, int lastRow, MembersSets instanceMembers) {
        // node inside fragment
        boolean isAfterFirst = isBefore(firstColumn, firstRow, cur.getBeginColumn(), cur.getBeginLine());
        boolean isBeforeLast = isBefore(cur.getEndColumn(), cur.getEndLine(), lastColumn, lastRow);
        if (isAfterFirst && isBeforeLast) {
            String fragment = cur.toString();

            if (cur instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) cur;
                MethodDataExtractor.extractParamsCount(md, out);
                MethodDataExtractor.isVoidMethod(md, out);
                int totalConnectivity = CouplingCalculator.calcConnectivity(fragment, instanceMembers.total);
                int methodConnectivity = CouplingCalculator.calcConnectivity(fragment, instanceMembers.methods);
                int fieldsConnectivity = CouplingCalculator.calcConnectivity(fragment, instanceMembers.fields);

                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalConnectivity, totalConnectivity));
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalConnectivityPerLine, (double) totalConnectivity / (lastRow - firstRow + 1)));
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.FieldConnectivity, fieldsConnectivity));
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.FieldConnectivityPerLine, (double) fieldsConnectivity / (lastRow - firstRow + 1)));
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodConnectivity, methodConnectivity));
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodConnectivityPerLine, (double) methodConnectivity / (lastRow - firstRow + 1)));

                return md;
            }

            return null;
        } else {
            for (Node n : cur.getChildrenNodes()) {
                MethodDeclaration md = traverse(n, firstColumn, firstRow, lastColumn, lastRow, instanceMembers);
                if (md != null) {
                    return md;
                }
            }
        }
        return null;
    }

    void analyzeDepth(String code,
                      int firstLine,
                      int lastLine) {
        int dep = 0;
        int line = 0;
        int area = 0;
        OutputUtils.printLn("DEPTHS:", out);
        StringBuilder depsBuilder = new StringBuilder();
        int depInLine = 0;
        for (Character ch : code.toCharArray()) {
            if (ch == '{') {
                dep++;
                depInLine++;
            } else if (ch == '}') {
                dep--;
                depInLine--;
            } else if (ch == '\n') {
                if (line >= firstLine && line <= lastLine) {
                    int resDep = dep;
                    if (depInLine > 0) {
                        resDep--;
                    }
                    depInLine = 0;
                    out.print(resDep + " ");
                    depsBuilder.append(resDep).append("_");
                    area += resDep;
                }
                line++;
            }
        }

        out.println();
        OutputUtils.printLn("AREA: " + area, out);
        OutputUtils.printLn("AVG DEPTH: " + (double) area / (lastLine - firstLine + 1), out);
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalLinesDepth, area));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.AverageLinesDepth, (double) area / (lastLine - firstLine + 1)));
    }

    String extractLineFragment(int firstCol, int lastCol, String line) {
        StringBuilder result = new StringBuilder();

        for (int pos = firstCol; pos <= lastCol; ++pos) {
            result.append(line.charAt(pos));
        }

        return result.toString();
    }

    private boolean isBefore(int firstColumn, int firstRow, int secondColumn, int secondRow) {
        return (firstRow < secondRow) || (firstRow == secondRow && firstColumn <= secondColumn);
    }
}
