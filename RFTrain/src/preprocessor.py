import os

import pandas as pd


class Preprocessor:
    def __init__(self, config):
        self.path_to_positive = os.path.join(config.get('datasets_dir_path'), config.get('positive_dataset_name'))
        self.path_to_negative = os.path.join(config.get('datasets_dir_path'), config.get('negative_dataset_name'))
        self.quantile_to_negative = config.get('quantile_to_negative')

        self.df_pos = pd.read_csv(self.path_to_positive, delimiter=';', error_bad_lines=False).dropna()
        self.df_neg = pd.read_csv(self.path_to_negative, delimiter=';', error_bad_lines=False).dropna()
        self.df_neg = self.df_neg[self.df_neg.score > 0]

    def make_binary(self):
        negatives = self.df_neg[self.df_neg.score <= self.df_neg.score.quantile(
            self.quantile_to_negative)]  # Filter only lower part, up to specified quantile
        negatives = negatives.assign(label=lambda x: 0)  # Set label to zero, meaning negative
        negatives = negatives.drop(columns=['score'])

        whole_df = pd.concat([self.df_pos, negatives])
        X = whole_df.drop(columns=['label'])
        y = whole_df.label

        return X, y

    def make_neutral_positive(self, consider_quantile):
        if consider_quantile:
            neutral = self.df_neg[self.df_neg.score <= self.df_neg.score.quantile(
                self.quantile_to_negative)].drop(columns=['score', 'label'])

        else:
            neutral = self.df_neg.drop(columns=['score', 'label'])

        positive = self.df_pos.drop(columns=['label'])

        return neutral, positive
