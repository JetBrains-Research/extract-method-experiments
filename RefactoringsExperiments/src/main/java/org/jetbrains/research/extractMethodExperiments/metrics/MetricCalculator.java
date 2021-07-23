package org.jetbrains.research.extractMethodExperiments.metrics;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.extractMethodExperiments.features.Feature;
import org.jetbrains.research.extractMethodExperiments.features.FeatureItem;
import org.jetbrains.research.extractMethodExperiments.features.FeaturesVector;
import org.jetbrains.research.extractMethodExperiments.haas.Candidate;
import org.jetbrains.research.extractMethodExperiments.utils.MemberSets;

import java.util.*;

public class MetricCalculator {
    private final Candidate candidate;
    private final int beginLine;
    private final int endLine;
    MetricCalculator(Candidate candidate, int beginLine, int endLine){
        this.candidate = candidate;
        this.beginLine =beginLine;
        this.endLine = endLine;
    }

    public FeaturesVector computeMetrics(){
        FeaturesVector result = new FeaturesVector(81);
        return null;
    }

    /**
     * Almost a direct copy from anti-copy-paster
     * */
    private Map<Feature, Double> couplingFeatures(){
        PsiFile thisFile = candidate.getContainingFile();
        MemberSets memberSets = MemberSets.extractAllMethodsAndFields(thisFile);

        int linesCount = endLine - beginLine + 1;

        int fieldMatches = 0;
        int methodMatches = 0;
        int totalMatches;

        PsiFileFactory factory = PsiFileFactory.getInstance(thisFile.getProject());
        @Nullable PsiFile psiFromText = factory.createFileFromText(candidate.getCandidateAsString(), thisFile);

        // search for all identifiers (methods and variables) in the code fragment
        @NotNull Collection<PsiIdentifier> identifiers = PsiTreeUtil.collectElementsOfType(psiFromText,
                PsiIdentifier.class);
        HashSet<String> identifiersNames = new HashSet<>();
        identifiers.forEach(i -> identifiersNames.add(i.getText()));

        for (String fieldName: memberSets.fields) {
            if (identifiersNames.contains(fieldName)) {
                fieldMatches += 1;
            }
        }

        for (String methodName: memberSets.methods) {
            if (identifiersNames.contains(methodName)) {
                methodMatches += 1;
            }
        }

        totalMatches = methodMatches + fieldMatches;

        Map<Feature, Double> result = new HashMap<>();

        result.put(Feature.TotalConnectivity, (double) totalMatches);
        result.put(Feature.TotalConnectivityPerLine,
                (double) totalMatches / linesCount);

        result.put(Feature.FieldConnectivity, (double) fieldMatches);
        result.put(Feature.FieldConnectivityPerLine,
                (double) fieldMatches / linesCount);

        result.put(Feature.MethodConnectivity, (double) methodMatches);
        result.put(Feature.MethodConnectivityPerLine,
                (double) methodMatches / linesCount);

        return result;
    }

    private Map<Feature, Double> keywordFeatures(){

    }

}
