# extract-method-experiments

The repository contains the tools and scripts used in [AntiCopyPaster project](https://github.com/JetBrains-Research/anti-copy-paster) to recommend extract method refactoring.

## Repository cloning

The tools provided in this repository depend on having cloned repositories directly on the local machine. 
For the purposes of automatization and simplification of work pipeline we provide a simple Python script written with [GitPython](https://github.com/gitpython-developers/GitPython) for cloning large quantities of Git repositories. 

Usage: 
```
python gitcloner/main.py <input_file> <output_directory>
```
`<input_file>` has to consist of names of GitHub repositories, one name in a line (see directory `data/` for examples).

It is recommended to use this script to clone repositories, however, it is not mandatory.

## Data gathering

The `extract-method-plugin` contains gradle module that is responsible for initiation of the data gathering plugin.

### Usage
There are two possible ways to use the tool:
* Generate positive-labeled samples using option `generatePositiveSamples`. It will start extraction of already performed Extract Method refactorings in changes history of existing Java projects using [RefactoringMiner](https://github.com/JetBrains-Research/RefactoringMiner).
* Generate negative-labeled samples using option `generateNegativeSamples`. It will generate all possible sequences of statements in methods, rank it using algorithm suggested by [Haas et al.](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.721.2014&rep=rep1&type=pdf) and get the worse ones as candidates that should not be recommended for extraction.

#### Unix systems

Execution of the following command from the root directory 
of the repository will initiate the collection of data.
```
./gradlew runRefactoringsExperiments -Prunner=RefactoringsExperiments -PprojectsDirPath=/path/to/projects/ -PdatasetsDirPath=/path/to/output/ -PgeneratePositiveSamples
```
*Note:* All paths provided as arguments should be absolute.

It will start the extraction of positive-labeled cases from git repositories on your computer located directly in the specified folder.
To run the generation of negative-labeled cases, you need to use `-PgenerateNegativeSamples` option.

The output of the tool are two datasets labeled `positive.csv` and `negative.csv` located in accordance with `-PdatasetsDirPath` argument.

#### Windows systems

The procedure is identical with one key difference, use command `gradlew.bat` instead of `./gradlew`

For convenience purposes, we also included a bash script `generateDataset.sh`, that can be used as a shorthand form of calling the gradle task, with flags for both positive, and negative labels:

```
bash generateDataset.sh /relative/path/to/projects/ /relative/path/to/output/
```

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


The `ml-framework/` directory contains Python scripts for creation, training and evaluation of machine learning models on the collected datasets. 
The requirements are specified in `requirements.txt`, and can be built with `pip install -r requirements.txt`.

### Training

To train a model, one has to complete a list of procedures:
- Obtain datasets by running the gradle task `runRefactoringsExperiments`
- Configure training procedure through config files.
    - Make a model config and specify model's arguments there. 
    Directory `model_settings/` has example config files with exhaustive lists of mutable arguments (arguments can be passed using `;` delimiter to initiate gridsearch over given parameters).
    - Make a config for training (see `settings/` directory).
- Use `train_by_config()` function specified in `trainer.py`. Examples of usage can be obtained in scripts `main.py` and `batch_training.py` 


### Testing

After having trained a model, one may configure a testing procedure(see `test_settings/` directory). 
Similarly to the training, function `test_by_config()` specified in `trainer.py` is used to test the model.

### Additional information
For more on the ml-framework we recommend visiting GitHub wiki page of this repository

