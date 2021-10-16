import git
import argparse
from pathlib import Path


def parse_in_file(filename):
    result = []
    with open(filename) as f:
        for line in f:
            url = f'https://github.com/{line.strip()}.git'
            result.append(url)
    return result


parser = argparse.ArgumentParser()
parser.add_argument("infile", help="file with enumerated github repos")
parser.add_argument("outdir", help="directory to which clone the repos")
args = parser.parse_args()

url_collection = parse_in_file(args.infile)
Path(args.outdir).mkdir(parents=True, exist_ok=True)

for url in url_collection:
    print(f'Cloning repository at {url}')
    git.Git(args.outdir).clone(url)
