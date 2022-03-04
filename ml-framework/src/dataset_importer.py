import os

import pandas as pd


class TrainImporter:
    def __init__(self, config):
        self.path_to_positive = os.path.join(config.get('datasets_dir_path'), config.get('positive_dataset_name'))
        self.path_to_negative = os.path.join(config.get('datasets_dir_path'), config.get('negative_dataset_name'))

        self.quantile_to_negative = config.get('quantile_to_negative')

        self.df_pos = pd.read_csv(self.path_to_positive, delimiter=';').dropna()
        self.df_neg = pd.read_csv(self.path_to_negative, delimiter=';').dropna()

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

        whole_df = pd.concat([positives, negatives], ignore_index=True).drop(columns='RepositoryName').sample(frac=1)
        x = whole_df.drop(columns=['label'])
        y = whole_df.label

        return x, y


class TestImporter:
    def __init__(self, config):
        pos_path = os.path.join(config.get('datasets_dir_path'), config.get('positive_dataset_name'))
        neg_path = os.path.join(config.get('datasets_dir_path'), config.get('negative_dataset_name'))

        positives = pd.read_csv(pos_path, delimiter=';').dropna().drop(columns=['RepositoryName'])
        negatives = pd.read_csv(neg_path, delimiter=';').dropna().drop(columns=['RepositoryName', 'Score'])
        self.df = pd.concat([positives, negatives], ignore_index=True)

    def make_test(self):
        return self.df.drop(columns=['label']), self.df.label
