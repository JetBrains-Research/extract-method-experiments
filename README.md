# extract-method-experiments

The repository contains the tools and scripts used in [AntiCopyPaster project](https://github.com/JetBrains-Research/anti-copy-paster) to recommend extract method refactoring.

## Data gathering

The `Refactoring Experiments` directory contains the tools that were used to gather the data that was used to train the machine learning models.

## Trainig the model

The `RFTrain` directory contains Python scripts for evaluating different machine learning models on the collected dataset. The requirements are `python >=3.6`, `skipy`, `numpy`, and `pandas`.
