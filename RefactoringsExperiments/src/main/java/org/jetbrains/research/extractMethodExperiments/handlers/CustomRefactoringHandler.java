package org.jetbrains.research.extractMethodExperiments.handlers;

import com.github.javaparser.ast.body.MethodDeclaration;
import gr.uom.java.xmi.LocationInfo;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.jetbrains.research.extractMethodExperiments.csv.SparseCSVBuilder;
import org.jetbrains.research.extractMethodExperiments.csv.models.CSVItem;
import org.jetbrains.research.extractMethodExperiments.csv.models.Feature;
import org.jetbrains.research.extractMethodExperiments.extractors.ExtractionConfig;
import org.jetbrains.research.extractMethodExperiments.extractors.MetadataExtractor;
import org.jetbrains.research.extractMethodExperiments.legacy.OutputUtils;
import org.jetbrains.research.extractMethodExperiments.utils.calcers.GitBlameAnalyzer;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;

import java.io.PrintWriter;
import java.util.List;

public class CustomRefactoringHandler extends RefactoringHandler {
    private final PrintWriter out;
    private final String repoURL;
    private final MetadataExtractor metadataExtractor;
    private final int total;
    private int current = 0;
    private int count = 0;
    private Logger logger;

    public CustomRefactoringHandler(final PrintWriter out,
                                    final String repoURL,
                                    final MetadataExtractor metadataExtractor,
                                    int total,
                                    Logger logger) {
        this.out = out;
        this.repoURL = repoURL;
        this.metadataExtractor = metadataExtractor;
        this.total = total;
        this.logger = logger;
    }

    @Override
    public boolean skipCommit(String commitId) {
        return false;
    }

    public void handle(String commitId, List<Refactoring> refactorings) {
        current++;
        handleCommit(commitId, refactorings, out);
    }

    public void handleException(String commitId, Exception e) {
        logger.log(Level.ERROR, "Cannot handle commit with ID: " + commitId);
    }

    public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
        OutputUtils.printLn(String.valueOf(errorCommitsCount), out);
    }

    private void handleCommit(String commitId, List<Refactoring> refactorings, PrintWriter pw) {
        boolean hasExtractMethod = false;
        for (Refactoring ref : refactorings) {
            if (ref.getRefactoringType() == RefactoringType.EXTRACT_OPERATION) {
                hasExtractMethod = true;
            }
        }

        String substring = repoURL.substring(0, repoURL.length() - 4);
        String commonURL = substring + "/commit/" + commitId;
        String blobURL = substring + "/blob/" + commitId;

        if (hasExtractMethod) {
            if (count % 100 == 0) {
                logger.log(Level.INFO, String.format("%d/%d, COMMIT ID: %s\n", this.current, this.total, commitId));
                count++;
            }
            OutputUtils.printLn("COMMIT ID: " + commitId, pw);
            OutputUtils.printLn("URL: " + commonURL, pw);
        }

        boolean hasEMRefactorings = false;

        for (Refactoring ref : refactorings) {

            if (ref.getRefactoringType() == RefactoringType.EXTRACT_OPERATION) {
                if (!hasEMRefactorings) {
                    hasEMRefactorings = true;
                }

                SparseCSVBuilder.sharedInstance.writeVector(true);
                OutputUtils.printLn("DESCRIPTION: " + ref.toString(), pw);
                ExtractOperationRefactoring refactoring = (ExtractOperationRefactoring) ref;
                LocationInfo locInfo = refactoring.getExtractedOperation().getLocationInfo();
                OutputUtils.printLn("REFACTORING FILE DIFF URL: " + commonURL + "/" + locInfo.getFilePath(), pw);
                OutputUtils.printLn("REFACTORING URL: " + blobURL + "/" + locInfo.getFilePath() + "#L" + locInfo.getStartLine(), pw);
                handleRefactoring(commitId, refactoring, pw);
            }

        }
        if (hasEMRefactorings) {
            OutputUtils.printLn("-----REFACTORINGS_END-----", pw);
        }
    }

    private void handleRefactoring(String commitId, ExtractOperationRefactoring refactoring, PrintWriter pw) {
        LocationInfo locInfo = refactoring.getExtractedOperation().getLocationInfo();
        String extractedOperationLocation = locInfo.getFilePath();
        if (ExtractionConfig.extractDirectly) {
            OutputUtils.printLn("DIRECTLY EXTRACTED OPERATION:", pw);
            MethodDeclaration md = null;
            try {
                md = metadataExtractor.extractFragment(commitId,
                        extractedOperationLocation,
                        locInfo.getStartLine(),
                        locInfo.getEndLine(),
                        locInfo.getStartColumn(),
                        locInfo.getEndColumn(),
                        false);

                GitBlameAnalyzer.extractLineAuthorAndCreationDate(
                        metadataExtractor.getRepo(),
                        locInfo.getStartLine(),
                        locInfo.getEndLine(),
                        extractedOperationLocation);

                OutputUtils.printLn("NUMBER OF LINES IN FRAGMENT: " + (locInfo.getEndLine() - locInfo.getStartLine() + 1), pw);
                SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalLinesOfCode, locInfo.getEndLine() - locInfo.getStartLine() + 1));
                if (md != null) {
                    addMd(md);
                }

            } catch (Exception e) {
                System.err.println("Cannot extract refactoring in commit: " + commitId);
                e.printStackTrace();
            }
        }

        if (!ExtractionConfig.onlyExtractedOperation) {
            OutputUtils.printLn("SOURCE BEFORE EXTRACTION:", pw);
            OutputUtils.printCompositeStatement(refactoring.getSourceOperationBeforeExtraction().getBody().getCompositeStatement(), 0, pw);

            OutputUtils.printLn("SOURCE AFTER EXTRACTION:", pw);
            OutputUtils.printCompositeStatement(refactoring.getSourceOperationAfterExtraction().getBody().getCompositeStatement(), 0, pw);
        }

        if (!ExtractionConfig.extractDirectly) {
            OutputUtils.printLn("EXTRACTED OPERATION:", pw);
            OutputUtils.printCompositeStatement(refactoring.getExtractedOperation().getBody().getCompositeStatement(), 0, pw);
        }

        OutputUtils.printLn("---REFACTORING_FINISH---", pw);
    }

    private void addMd(MethodDeclaration md) {
        String fragment = md.toString();
        String linearFragment = fragment.replace('\n', ' ');
        int fragLocs = StringUtils.countMatches(fragment, "\n") + 1;

        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodDeclarationSymbols, linearFragment.length()));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodDeclarationAverageSymbols, (double) linearFragment.length() / fragLocs));

        analyzeDepth(fragment, fragLocs);
    }

    void analyzeDepth(String code, int locCount) {
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

        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodDeclarationDepth, area));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.MethodDeclarationDepthPerLine, (double) area / locCount));
    }
}
