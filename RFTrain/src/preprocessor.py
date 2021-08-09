import os

import pandas as pd


class Preprocessor:
    def __init__(self, config):
        self.path_to_positive = os.path.join(config.get('datasets_dir_path'), config.get('positive_dataset_name'))
        self.path_to_negative = os.path.join(config.get('datasets_dir_path'), config.get('negative_dataset_name'))
        self.quantile_to_negative = config.get('quantile_to_negative')

        self.df_pos = pd.read_csv(self.path_to_positive, delimiter=';', error_bad_lines=False).dropna()
        self.df_neg = pd.read_csv(self.path_to_negative, delimiter=';', error_bad_lines=False).dropna()

    def make_binary(self):
        """
        Makes and returns  a pair X, y which correspond to features(X) and targets(y) dataframe,
        each sample has target label of 1 or 0, suitable for binary classification.
        """
        negatives = self.df_neg[self.df_neg.Score <= self.df_neg.Score.quantile(
            self.quantile_to_negative)]  # Filter only lower part, up to specified quantile
        negatives = negatives.assign(label=lambda x: 0)  # Set label to zero, meaning negative
        negatives = negatives.drop(columns=['Score'])

        positives = self.df_pos.assign(label=lambda x: 1)  # Set label to one, meaning positive

        whole_df = pd.concat([positives, negatives])
        X = whole_df.drop(columns=['label'])
        X[X < 0] = 0
        y = whole_df.label

        return X, y

    def make_neutral_positive(self, consider_quantile=False):
        """
        Makes and returns a pair neutral, positive which correspond to two features dataframes,
        each sample has no target label, suitable for one class classification.

        :param `consider_quantile` is responsible for choosing
        whether `neutral` should be built with quantile consideration
        """
        if consider_quantile:
            neutral = self.df_neg[self.df_neg.Score <= self.df_neg.Score.quantile(
                self.quantile_to_negative)].drop(columns=['Score'])

        else:
            neutral = self.df_neg.drop(columns=['Score'])

        positive = self.df_pos

        return neutral, positive
