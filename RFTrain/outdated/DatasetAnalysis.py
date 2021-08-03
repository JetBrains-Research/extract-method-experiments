import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
from sklearn.ensemble import RandomForestClassifier

sns.set(color_codes=True)


def main():
    rf = RandomForestClassifier(n_estimators=1000, random_state=42)

    features = pd.read_csv('false_train.csv')
    labels = np.array(features['label'])

    positive_ = features[features.label == 1]
    negative_ = features[features.label == 0]

    positive_cleaned = positive_.drop('label', axis=1)
    negative_cleaned = negative_.drop('label', axis=1)

    features_to_plot = range(112)

    for f in features_to_plot:
        f_str = str(f)
        plt.clf()
        pv = positive_cleaned[f_str]
        nv = negative_cleaned[f_str]
        sns.distplot(pv)
        sns.distplot(nv)
        plt.savefig("plots/feature_" + f_str + ".png")


if __name__ == "__main__":
    main()
