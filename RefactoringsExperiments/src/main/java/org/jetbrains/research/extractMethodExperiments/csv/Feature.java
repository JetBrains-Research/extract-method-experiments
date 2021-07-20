package org.jetbrains.research.extractMethodExperiments.csv;

public enum Feature {
    TotalLinesOfCode("TotalLinesOfCode", 0),
    TotalSymbolsInFragment("TotalSymbolsInCode", 1),
    AverageSymbolsInCodeLine("AverageSymbolsInCodeLine", 2),
    FragmentArea("CodeArea", 3),
    AverageAreaInFragmentLine("AverageAreaInCodeLine", 3),
    TotalCommitsInFragment("TotalCommitsInFragment", 4),
    TotalAuthorsInFragment("TotalAuthorsInFragment", 5),
    LiveTimeOfFragment("LiveTimeOfFragment", 6),
    AverageLiveTimeOfLine("AverageLiveTimeOfLine", 7),
    TotalConnectivity("TotalConnectivity", 8),
    TotalConnectivityPerLine("TotalConnectivityPerLine", 9),
    FieldConnectivity("FieldConnectivity", 10),
    FieldConnectivityPerLine("FieldConnectivityPerLine", 11),
    MethodConnectivity("MethodConnectivity", 12),
    MethodConnectivityPerLine("MethodConnectivityPerLine", 13),
    MethodDeclarationLines("MethodDeclarationLines", 14),
    MethodDeclarationSymbols("MethodDeclarationSymbols", 15),
    MethodDeclarationAverageSymbolsInLine("MethodDeclarationAverageSymbolsInLine", 16),
    MethodDeclarationDepth("MethodDeclarationArea", 17),
    MethodDeclarationDepthPerLine("MethodDeclarationDepthPerLine", 18),

    KeywordContinueTotalCount("KeywordContinueTotalCount", 19),
    KeywordContinueCountPerLine("KeywordContinueCountPerLine", 20),
    KeywordForTotalCount("KeywordForTotalCount", 21),
    KeywordForCountPerLine("KeywordForCountPerLine", 22),
    KeywordNewTotalCount("KeywordNewTotalCount", 23),
    KeywordNewCountPerLine("KeywordNewCountPerLine", 24),
    KeywordSwitchTotalCount("KeywordSwitchTotalCount", 25),
    KeywordSwitchCountPerLine("KeywordSwitchCountPerLine", 26),
    KeywordAssertTotalCount("KeywordAssertTotalCount", 27),
    KeywordAssertCountPerLine("KeywordAssertCountPerLine", 28),
    KeywordSynchronizedTotalCount("KeywordSynchronizedTotalCount", 29),
    KeywordSynchronizedCountPerLine("KeywordSynchronizedCountPerLine", 30),
    KeywordBooleanTotalCount("KeywordBooleanTotalCount", 31),
    KeywordBooleanCountPerLine("KeywordBooleanCountPerLine", 32),
    KeywordDoTotalCount("KeywordDoTotalCount", 33),
    KeywordDoCountPerLine("KeywordDoCountPerLine", 34),
    KeywordIfTotalCount("KeywordIfTotalCount", 35),
    KeywordIfCountPerLine("KeywordIfCountPerLine", 36),
    KeywordThisTotalCount("KeywordThisTotalCount", 37),
    KeywordThisCountPerLine("KeywordThisCountPerLine", 38),
    KeywordBreakTotalCount("KeywordBreakTotalCount", 39),
    KeywordBreakCountPerLine("KeywordBreakCountPerLine", 40),
    KeywordDoubleTotalCount("KeywordDoubleTotalCount", 41),
    KeywordDoubleCountPerLine("KeywordDoubleCountPerLine", 42),
    KeywordThrowTotalCount("KeywordThrowTotalCount", 43),
    KeywordThrowCountPerLine("KeywordThrowCountPerLine", 44),
    KeywordByteTotalCount("KeywordByteTotalCount", 45),
    KeywordByteCountPerLine("KeywordByteCountPerLine", 46),
    KeywordElseTotalCount("KeywordElseTotalCount", 47),
    KeywordElseCountPerLine("KeywordElseCountPerLine", 48),
    KeywordCaseTotalCount("KeywordCaseTotalCount", 49),
    KeywordCaseCountPerLine("KeywordCaseCountPerLine", 50),
    KeywordInstanceofTotalCount("KeywordInstanceofTotalCount", 51),
    KeywordInstanceofCountPerLine("KeywordInstanceofCountPerLine", 52),
    KeywordReturnTotalCount("KeywordReturnTotalCount", 53),
    KeywordReturnCountPerLine("KeywordReturnCountPerLine", 54),
    KeywordTransientTotalCount("KeywordTransientTotalCount", 55),
    KeywordTransientCountPerLine("KeywordTransientCountPerLine", 56),
    KeywordCatchTotalCount("KeywordCatchTotalCount", 57),
    KeywordCatchCountPerLine("KeywordCatchCountPerLine", 58),
    KeywordIntTotalCount("KeywordIntTotalCount", 59),
    KeywordIntCountPerLine("KeywordIntCountPerLine", 60),
    KeywordShortTotalCount("KeywordShortTotalCount", 61),
    KeywordShortCountPerLine("KeywordShortCountPerLine", 62),
    KeywordTryTotalCount("KeywordTryTotalCount", 63),
    KeywordTryCountPerLine("KeywordTryCountPerLine", 64),
    KeywordCharTotalCount("KeywordCharTotalCount", 65),
    KeywordCharCountPerLine("KeywordCharCountPerLine", 66),
    KeywordFinalTotalCount("KeywordFinalTotalCount", 67),
    KeywordFinalCountPerLine("KeywordFinalCountPerLine", 68),
    KeywordFinallyTotalCount("KeywordFinallyTotalCount", 69),
    KeywordFinallyCountPerLine("KeywordFinallyCountPerLine", 70),
    KeywordLongTotalCount("KeywordLongTotalCount", 71),
    KeywordLongCountPerLine("KeywordLongCountPerLine", 72),
    KeywordStrictfpTotalCount("KeywordStrictfpTotalCount", 73),
    KeywordStrictfpCountPerLine("KeywordStrictfpCountPerLine", 74),
    KeywordFloatTotalCount("KeywordFloatTotalCount", 75),
    KeywordFloatCountPerLine("KeywordFloatCountPerLine", 76),
    KeywordSuperTotalCount("KeywordSuperTotalCount", 77),
    KeywordSuperCountPerLine("KeywordSuperCountPerLine", 78),
    KeywordWhileTotalCount("KeywordWhileTotalCount", 79),
    KeywordWhileCountPerLine("KeywordWhileCountPerLine", 80);

    private String name;
    private int id;

    Feature(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public static Feature fromId(int id) {
        return Feature.values()[id];
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
