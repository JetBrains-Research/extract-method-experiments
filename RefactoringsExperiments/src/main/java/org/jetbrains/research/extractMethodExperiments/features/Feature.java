package org.jetbrains.research.extractMethodExperiments.features;

public enum Feature {
    //Meta-Features
    TotalLinesOfCode("TotalLinesOfCode", 0),
    TotalSymbols("TotalSymbols", 1),
    SymbolsPerLine("SymbolsPerLine", 2),
    Area("Area", 3),
    AreaPerLine("AreaPerLine", 3),

    //Historical-Features
    TotalCommitsInFragment("TotalCommitsInFragment", 4),
    TotalAuthorsInFragment("TotalAuthorsInFragment", 5),
    LiveTimeOfFragment("LiveTimeOfFragment", 6),
    LiveTimePerLine("LiveTimePerLine", 7),

    //Coupling-Features
    TotalConnectivity("TotalConnectivity", 8),
    TotalConnectivityPerLine("TotalConnectivityPerLine", 9),
    FieldConnectivity("FieldConnectivity", 10),
    FieldConnectivityPerLine("FieldConnectivityPerLine", 11),
    MethodConnectivity("MethodConnectivity", 12),
    MethodConnectivityPerLine("MethodConnectivityPerLine", 13),

    //Method-Features
    MethodDeclarationLines("MethodDeclarationLines", 14),
    MethodDeclarationSymbols("MethodDeclarationSymbols", 15),
    MethodDeclarationSymbolsPerLine("MethodDeclarationSymbolsPerLine", 16),
    MethodDeclarationArea("MethodDeclarationArea", 17),
    MethodDeclarationAreaPerLine("MethodDeclarationDepthPerLine", 18),

    //Keyword-Features
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


    Feature(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCyrName() {
        if (name.startsWith("Keyword") && name.endsWith("TotalCount")) {
            return "the total count of the " + name.substring(7, 7 + name.length() - "Keyword".length() - "TotalCount".length()) + " keyword";
        }

        if (name.startsWith("Keyword") && name.endsWith("CountPerLine")) {
            return "the average count of the " + name.substring(7, 7 + name.length() - "Keyword".length() - "CountPerLine".length()) + " keyword";
        }

        switch (this) {
            case MethodDeclarationSymbols:
                return "the total size of the enclosing method in symbols";
            case MethodDeclarationSymbolsPerLine:
                return "the per-line-averaged size of the enclosing method in symbols";
            case MethodDeclarationArea:
                return "the total nesting area of the enclosing method";
            case MethodDeclarationAreaPerLine:
                return "the per-line-averaged nesting area of the enclosing method";
            case TotalSymbols:
                return "the total size of the code fragment in symbols";
            case SymbolsPerLine:
                return "the per-line-averaged size of the code fragment in symbols";
            case Area:
                return "the total nested area of the code fragment";
            case AreaPerLine:
                return "the per-line-averaged nested area of the code fragment";
            case TotalLinesOfCode:
                return "the total number of lines of code";
            case TotalConnectivity:
                return "the total coupling with the enclosing class";
            case TotalConnectivityPerLine:
                return "the average coupling with the enclosing class";
            case FieldConnectivity:
                return "the total coupling with the enclosing class by fields";
            case FieldConnectivityPerLine:
                return "the average coupling with the enclosing class by fields";
            case MethodConnectivity:
                return "the total coupling with the enclosing class by methods";
            case MethodConnectivityPerLine:
                return "the average coupling with the enclosing class by methods";
            default:
                return "";
        }
    }

    public int getId() {
        return id;
    }

    private String name;
    private int id;

    public static Feature fromId(int id) {
        return Feature.values()[id];
    }
}

