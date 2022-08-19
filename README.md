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

The `extract-method` gradle modules contain driving Intellij Idea plugin, logic and code metrics implementations, that are 
used for collection of positive and negative labeled samples of refactorings from Java git repositories.

### Usage
There are two possible ways to use the tool, for which two gradle tasks are implemented:
* For generation of positive labeled samples, one can use gradle task `runPositiveRefactorings`, which requires 
two arguments passed, namely
    - `<projectsDirPath>` - absolute path to the directory containing cloned git repositories.
    - `<outputFilePath>` - absolute path to the desired output destination.
* Similarly, for generation of negative labeled samples the gradle task is `runNegativeRefactorings`, 
with two arguments
    - `<inputProjectPath>` - absolute path to the project that should be analyzed.
    - `<outputFilePath>` - absolute path to the desired output destination.

For example, the execution of the following command from the root
of the repository will initiate the collection of data.
```
./gradlew runPositiveRefactorings -PprojectsDirPath=/path/to/projects/ -PoutputFilePath=/path/to/output/
```

#### Windows systems

The procedure is identical with one key difference, use command `gradlew.bat` instead of `./gradlew`

For convenience purposes, we also included a bash scripts `generatePositiveDataset.sh` and
`generateNegativeDatataset.sh`, that can be used as a shorthand form of calling the gradle tasks.

```
bash generatePositiveDataset.sh /relative/path/to/project/ /relative/path/to/output/
```

```
bash generateNegativeDataset.sh /relative/path/to/projects_dir/ 
```

## Machine Learning

Currently, there are a number of ML algorithms implemented in the experiments' pipeline:

| classifier name    | implementation source | shortcut name  |
|:-------------|:-------------| :-----:|
| RandomForest  | `sklearn.ensemble.RandomForest` | RF |
| GradientBoostingClassifier  | `sklearn.ensemble.GradientBoostingClassifier` | GBC |
| LogisticRegression | `sklearn.linear_model.LogisticRegression`   | LRC |
| SGD over SVM  | `sklearn.ensemble.SGDClassifier` | SGD |
| GaussianNaiveBayes  | `sklearn.naive_bayes.GaussianNB`   | GNB |
| ComplementNaiveBayes | `sklearn.naive_bayes.ComplementNB`   |CNB  |
| MultiLayerPerceptron  | `sklearn.neural_network.MLPClassifier` | MLP|
| Convolutional Neural Network  | `CNN-framework/TrainedModel/saved_model.pb` | CNN|


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

