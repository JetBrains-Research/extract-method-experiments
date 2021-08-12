import os

import pandas as pd
from sklearn.model_selection import train_test_split


class TrainImporter:
    def __init__(self, config):
        self.path_to_positive = os.path.join(config.get('datasets_dir_path'), config.get('positive_dataset_name'))
        self.path_to_negative = os.path.join(config.get('datasets_dir_path'), config.get('negative_dataset_name'))

        self.quantile_to_negative = config.get('quantile_to_negative')

        self.df_pos = pd.read_csv(self.path_to_positive, delimiter=';', error_bad_lines=False).dropna()
        self.df_neg = pd.read_csv(self.path_to_negative, delimiter=';', error_bad_lines=False).dropna()

    def make_binary(self, test_size=0.25):
        """
        Makes and returns  a pair x, y which correspond to features(x) and targets(y) dataframe,
        each sample has target label of 1 or 0, suitable for binary classification.
        """
        negatives = self.df_neg[self.df_neg.Score <= self.df_neg.Score.quantile(
            self.quantile_to_negative)]  # Filter only lower part, up to specified quantile

        negatives = negatives.assign(label=lambda value: 0)  # Set label to zero, meaning negative
        negatives = negatives.drop(columns=['Score'])

        positives = self.df_pos.assign(label=lambda value: 1)  # Set label to one, meaning positive

        whole_df = pd.concat([positives, negatives])
        x = whole_df.drop(columns=['label'])
        y = whole_df.label
        x[x < 0] = 0

        x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=test_size)

        return x_train, x_test, y_train, y_test


class TestImporter:
    def __init__(self, config):
        config.get('dataset_path')
        self.df = pd.read_csv(config.get('dataset_path'), delimiter=';', error_bad_lines=False).dropna()

    def make_test(self):
        return self.df.drop(columns=['label']), self.df.label
