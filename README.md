# extract-method-experiments

The repository contains the tools and scripts used in [AntiCopyPaster project](https://github.com/JetBrains-Research/anti-copy-paster) to recommend extract method refactoring.

## Data gathering

The `Refactoring Experiments` directory contains the tools that were used to gather the data that was used to train the machine learning models.

### Usage
There are two possible ways to use the tool:
* Generate positive-labeles samples using option `generatePositiveSamples`. It will start extraction of already performed Extract Method refactorings in changes history of existing Java projects using [RefactoringMiner](https://github.com/JetBrains-Research/RefactoringMiner).
* Generate negative-labeles samples using option `generateNegativeSamples`. It will generate all possible sequences of statements in methods, rank it using algorithm suggested by [Haas et al.](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.721.2014&rep=rep1&type=pdf) and get the worse ones as candidates that should not be recommended for extraction.

#### Unix systems

Open `Refactoring Experiments` directory and execute the following command

```
./gradlew runRefactoringsExperiments -Prunner=RefactoringsExperiments -PprojectsFilePath=/path/to/file.txt -PgeneratePositiveSamples
```
It will start the extraction of positive-labeled cases from repositories on your computer specified in `file.txt`.
To run the generation of negative-labeled cases, you need to use `-PgenerateNegativeSamples` option.

#### Windows systems

The procedure is identical with one key difference, use command `gradlew.bat` instead of `./gradlew`

### Data directory

The `data/` directory is used for storing text files with lists of repositories for processing, for instance, file `apache_repos.txt` consists of small list of Apache repositories, and `star_top100.txt` includes list of 100 most starred java repositories on GitHub. 

### Output directory

The `output/` directory holds the `true.csv` and `false.csv`, obtained by running the extraction.

## Training the model

The `RFTrain` directory contains Python scripts for evaluating different machine learning models on the collected dataset. The requirements are `python >=3.6`, `skipy`, `numpy`, and `pandas`.
