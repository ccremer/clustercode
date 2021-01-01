#!/bin/bash
set -euo pipefail


gh_pages_worktree=$(mktemp -d)
git worktree add "${gh_pages_worktree}" gh-pages

rm -r "${gh_pages_worktree}"/* || true
cp -r --force docs/public/. "${gh_pages_worktree}"/

pushd "${gh_pages_worktree}" > /dev/null

tree "${gh_pages_worktree}"

git add *
echo "Finding changed files..."
if [[ -n "$(git status --porcelain)" ]]; then

    git commit --message="Update documentation" --signoff

    repo_url="https://x-access-token:$GITHUB_TOKEN@github.com/$GITHUB_REPOSITORY"
    git push "$repo_url" gh-pages

    popd > /dev/null
    git add *
    git commit --message="Update documentation version tags" --signoff
    git push
else
    echo "No documentation to update."
    popd > /dev/null
fi
