package org.jetbrains.research.extractMethodExperiments.utils.calcers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.research.extractMethodExperiments.csv.SparseCSVBuilder;
import org.jetbrains.research.extractMethodExperiments.csv.models.CSVItem;
import org.jetbrains.research.extractMethodExperiments.csv.models.Feature;
import org.jetbrains.research.extractMethodExperiments.csv.models.ICSVItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GitBlameAnalyzer {

    public static void extractLineAuthorAndCreationDate(Repository repo,
                                                        int firstLine,
                                                        int lastLine,
                                                        String filePath) throws GitAPIException {
        final BlameResult result = new Git(repo).blame().setFilePath(filePath)
                .setTextComparator(RawTextComparator.WS_IGNORE_ALL).call();


        ArrayList<Integer> creationDates = new ArrayList<>();
        Set<String> commits = new HashSet<>();
        Set<String> authors = new HashSet<>();
        final RawText rawText = result.getResultContents();
        for (int i = firstLine; i < Math.min(rawText.size(), lastLine + 1); i++) {
            final PersonIdent sourceAuthor = result.getSourceAuthor(i);
            final RevCommit sourceCommit = result.getSourceCommit(i);
            if (sourceCommit != null) {
                creationDates.add(sourceCommit.getCommitTime());
                commits.add(sourceCommit.getName());
                authors.add(sourceAuthor.getName());
            }
        }

        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalCommitsInFragment, commits.size()));
        SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.TotalAuthorsInFragment, authors.size()));
        int minTime = Integer.MAX_VALUE;
        int maxTime = Integer.MIN_VALUE;

        for (Integer time : creationDates) {
            if (minTime > time) {
                minTime = time;
            }
            if (maxTime < time) {
                maxTime = time;
            }
        }

        if (minTime != Integer.MAX_VALUE) {
            int totalTime = 0;
            for (Integer time : creationDates) {
                totalTime += time - minTime;
            }

            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.LiveTimeOfFragment, maxTime - minTime));
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.AverageLiveTimeOfLine, (double) totalTime / creationDates.size()));
        }
    }

    public static void extractToList(Repository repo,
                                            int firstLine,
                                            int lastLine,
                                            String filePath, List<ICSVItem> features) throws GitAPIException {
        final BlameResult result = new Git(repo).blame().setFilePath(filePath)
                .setTextComparator(RawTextComparator.WS_IGNORE_ALL).call();


        ArrayList<Integer> creationDates = new ArrayList<>();
        Set<String> commits = new HashSet<>();
        Set<String> authors = new HashSet<>();
        final RawText rawText = result.getResultContents();
        for (int i = firstLine; i < Math.min(rawText.size(), lastLine + 1); i++) {
            final PersonIdent sourceAuthor = result.getSourceAuthor(i);
            final RevCommit sourceCommit = result.getSourceCommit(i);
            if (sourceCommit != null) {
                creationDates.add(sourceCommit.getCommitTime());
                commits.add(sourceCommit.getName());
                authors.add(sourceAuthor.getName());
            }
        }

        features.add(new CSVItem(Feature.TotalCommitsInFragment, (double) commits.size()));
        features.add(new CSVItem(Feature.TotalAuthorsInFragment, (double) authors.size()));

        int minTime = Integer.MAX_VALUE;
        int maxTime = Integer.MIN_VALUE;

        for (Integer time : creationDates) {
            if (minTime > time) {
                minTime = time;
            }
            if (maxTime < time) {
                maxTime = time;
            }
        }

        if (minTime != Integer.MAX_VALUE) {
            int totalTime = 0;
            for (Integer time : creationDates) {
                totalTime += time - minTime;
            }
            features.add(new CSVItem(Feature.LiveTimeOfFragment, (double) maxTime - minTime));
            features.add(new CSVItem(Feature.AverageLiveTimeOfLine, (double) totalTime / creationDates.size()));
        }
    }

}
