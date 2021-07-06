package ru.hse.kirilenko.refactorings;

import ru.hse.kirilenko.refactorings.csv.models.Feature;
import ru.hse.kirilenko.refactorings.extractors.FalseRefactoringsExtractor;


import java.io.*;

public class FalseRefactoringsExtractorCaller {
    static void makeFileHeader(FileWriter fw) throws IOException{
        for (int i = 0; i < 117; i++)
            fw.write(Feature.fromId(i).getName() + ';');
        fw.write("score;");
        fw.write("label\n");
    }

    public static void run(String path) throws IOException {
        String outFilePath = "false.csv";

        FileWriter fw = new FileWriter(outFilePath);
        makeFileHeader(fw);

        FalseRefactoringsExtractor falseRefactoringsExtractor = new FalseRefactoringsExtractor(fw);

        File file = new File(path);

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            //List<String> repos = new ArrayList<>();
            br.lines().forEach(s -> {
                //repos.add(s);

                String url = "https://github.com/" + s + ".git";
                try {
                    falseRefactoringsExtractor.run(s, url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });



        } catch (Exception e) {
            String errormsg = String.format("Warning, there is no such file: %s\nExiting...", path);
            System.out.println(errormsg);
            System.exit(0);
        }
    }
}