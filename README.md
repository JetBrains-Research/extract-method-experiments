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

## Machine Learning

Currently, there are a number of ML algorithms proposed as valid models:

| classifier name    | implementation source | shortcut name  |
|:-------------|:-------------| :-----:|
| RandomForest  | `sklearn.ensemble.RandomForest` | RF |
| SupportVectorMachine      | `sklearn.svm.SVC`      | SVC |
| LinearSupportVectorMachine | `sklearn.svm.LinearSVC`   | LSVC |
| SGD over SVM  | `sklearn.ensemble.SGDClassifier` | SGD |
| OneClassSVM  | `sklearn.ensemble.OneClassSVM` | OCC |
| GaussianNaiveBayes  | `sklearn.naive_bayes.GaussianNB`   | GNB |
| ComplementNaiveBayes | `sklearn.naive_bayes.ComplementNB`   |CNB  |
| DeepNeuralNetwork  | neural network built with `keras` | DNN |

The `RFTrain` directory contains Python scripts for evaluating machine learning models on the collected datasets. 
The requirements are specified in `requirements.txt`.

### Training

To train a model, one has to complete a list of procedures:
- Obtain datasets from `RefactoringExperiments/`
- Configure training procedure through config files.
    - Make a model config and specify model's arguments there. 
    Directory `model_settings/` has example config files with exhaustive lists of mutable arguments.
    - Make a config for training and specify the path to model' config there.
- Run `trainer.py`