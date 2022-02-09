import git
import argparse
from pathlib import Path
import os

parser = argparse.ArgumentParser()
parser.add_argument("infile", help="file with enumerated github repos")
parser.add_argument("outdir", help="directory to which clone the repos")
args = parser.parse_args()

Path(args.outdir).mkdir(parents=True, exist_ok=True)

with open(args.infile) as f:
    for line in f:
        repoName = line.strip()
        shortName = repoName.split('/')[1]
        url = f'https://github.com/{repoName}.git'
        print(f'Cloning repository at {url}')
        loc_path = os.path.abspath(os.path.join(args.outdir, repoName.split('/')[1]))

        r = git.Repo.clone_from(url, loc_path)
        sha = r.rev_parse('HEAD')

        with open(os.path.join(args.outdir, 'mapping.txt'), 'a') as out_f:
            out_f.write(f'{loc_path};{repoName};{sha}\n')

