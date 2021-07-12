# extract-method-experiments

The repository contains the tools and scripts used in [AntiCopyPaster project](https://github.com/JetBrains-Research/anti-copy-paster) to recommend extract method refactoring.

## Data gathering

The `Refactoring Experiments` directory contains the tools that were used to gather the data that was used to train the machine learning models.

### How to use the tool
#### Unix systems

Open `Refactoring Experiments` directory and execute the following command

```
./gradlew run --args="-p=data/star_top100.txt -n=data/apache_repos.txt" 
```
It will start the extraction of positive-labeled cases from repositories specified in `data/star_top100.txt` file, and negative-labeled cases from `data/apache_repos.txt`. These files can be changed to any other pair of files for any reason.

If you want to collect only one type of cases, just use `--args="-p=data/a.txt"` for positives, and `--args="-n=data/b.txt"` for negatives. 

#### Windows systems

The procedure is identical with one key difference, use command `gradlew.bat` instead of `./gradlew`

### Data directory

The `data/` directory is used for storing text files with lists of repositories for processing, for instance, file `apache_repos.txt` consists of small list of Apache repositories, and `star_top100.txt` includes list of 100 most starred java repositories on GitHub. 

### Output directory

The `output/` directory holds the `true.csv` and `false.csv`, obtained by running the extraction.

## Training the model

The `RFTrain` directory contains Python scripts for evaluating different machine learning models on the collected dataset. The requirements are `python >=3.6`, `skipy`, `numpy`, and `pandas`.
