# extract-method-experiments

The repository contains the tools and scripts used in [AntiCopyPaster project](https://github.com/JetBrains-Research/anti-copy-paster) to recommend extract method refactoring.

## Data gathering

The `Refactoring Experiments` directory contains the tools that were used to gather the data that was used to train the machine learning models.

### How to use the tool
#### Unix systems

Open `Refactoring Experiments` directory and execute the following command

```
./gradlew run --args="-p=a.txt -n=b.txt" 
```
It will start the extraction of positive-labeled cases from repositories specified in `a.txt` file, and negative-labeled cases from `b.txt`. 

If you want to collect only one type of cases, just use `--args="-p=a.txt"` for positives, and `--args="-n=b.txt"` for negatives. 

#### Windows systems

The procedure is identical with one key difference, use command `gradlew.bat` instead of `./gradlew`


## Training the model

The `RFTrain` directory contains Python scripts for evaluating different machine learning models on the collected dataset. The requirements are `python >=3.6`, `skipy`, `numpy`, and `pandas`.
