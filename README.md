# extract-method-experiments

The repository contains the tools and scripts used in [AntiCopyPaster project](https://github.com/JetBrains-Research/anti-copy-paster) to recommend extract method refactoring.

## Data gathering

The `Refactoring Experiments` directory contains the tools that were used to gather the data that was used to train the machine learning models.

### Usage
There are two possible ways to use the tool:
* Generate positive-labeled samples using option `generatePositiveSamples`. It will start extraction of already performed Extract Method refactorings in changes history of existing Java projects using [RefactoringMiner](https://github.com/JetBrains-Research/RefactoringMiner).
* Generate negative-labeled samples using option `generateNegativeSamples`. It will generate all possible sequences of statements in methods, rank it using algorithm suggested by [Haas et al.](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.721.2014&rep=rep1&type=pdf) and get the worse ones as candidates that should not be recommended for extraction.

#### Unix systems

Open `Refactoring Experiments` directory and execute the following command:

```
./gradlew runRefactoringsExperiments -Prunner=RefactoringsExperiments -PprojectsDirPath=/path/to/projects/ -PdatasetsDirPath=/path/to/output/ -PgeneratePositiveSamples
```
*Note:* All paths provided as arguments should be absolute.

It will start the extraction of positive-labeled cases from git repositories on your computer located directly in the specified folder.
To run the generation of negative-labeled cases, you need to use `-PgenerateNegativeSamples` option.

The output of the tool are two datasets labeled `positive.csv` and `negative.csv` located in accordance with `-PdatasetsDirPath` argument.

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
| LogisticRegression | `sklearn.linear_model.LogisticRegression`   | LRC |
| SGD over SVM  | `sklearn.ensemble.SGDClassifier` | SGD |
| GaussianNaiveBayes  | `sklearn.naive_bayes.GaussianNB`   | GNB |
| ComplementNaiveBayes | `sklearn.naive_bayes.ComplementNB`   |CNB  |
| MultiLayerPerceptron  | `sklearn.neural_network.MLPClassifier` | MLP|


The `RFTrain/` directory contains Python scripts for evaluating machine learning models on the collected datasets. 
The requirements are specified in `requirements.txt`.

### Training

To train a model, one has to complete a list of procedures:
- Obtain datasets from `RefactoringExperiments/`
- Configure training procedure through config files.
    - Make a model config and specify model's arguments there. 
    Directory `model_settings/` has example config files with exhaustive lists of mutable arguments (arguments can be passed using `;` delimiter to initiate gridsearch over given parameters).
    - Make a config for training (see `settings/` directory).
- Use `train_by_config()` function specified in `trainer.py`. Examples of usage can be obtained in scripts `main.py` and `batch_training.py` 


### Testing

After having trained a model, one may configure a testing procedure(see `test_settings/` directory). 
Similarly to the training, function `test_by_config()` specified in `trainer.py` is used to test the model.

