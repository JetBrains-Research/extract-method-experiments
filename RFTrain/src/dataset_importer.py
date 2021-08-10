import os

import pandas as pd


class DatasetImporter:
    def __init__(self, config):
        self.path_to_positive = os.path.join(config.get('datasets_dir_path'), config.get('positive_dataset_name'))
        self.path_to_negative = os.path.join(config.get('datasets_dir_path'), config.get('negative_dataset_name'))
        self.quantile_to_negative = config.get('quantile_to_negative')

        self.df_pos = pd.read_csv(self.path_to_positive, delimiter=';', error_bad_lines=False).dropna()
        self.df_neg = pd.read_csv(self.path_to_negative, delimiter=';', error_bad_lines=False).dropna()

    def make_binary(self):
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

        return x, y
