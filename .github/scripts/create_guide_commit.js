const fs = require('fs')

module.exports = async ({github, context, core, exec}) => {
  const {owner, repo} = context.repo
  await exec.exec('bash', ['.github/scripts/format_guides.sh'])
  const result = await exec.getExecOutput('git', ['diff', '--name-only', '--', 'src-doc/documentation'])
  const files = result.stdout.split('\n').map((line) => line.trim()).filter(Boolean)
  if (files.length !== 9) {
    core.setFailed(`expected 9 documentation files, found ${files.length}`)
    return
  }

  const tree = []
  for (const file of files) {
    const blob = await github.rest.git.createBlob({
      owner,
      repo,
      content: fs.readFileSync(file).toString('base64'),
      encoding: 'base64'
    })
    tree.push({path: file, mode: '100644', type: 'blob', sha: blob.data.sha})
  }

  for (const path of [
    '.github/scripts/convert_imported_guides.py',
    '.github/scripts/create_guide_commit.js',
    '.github/scripts/format_guides.sh',
    '.github/workflows/convert-imported-guides.yml'
  ]) {
    tree.push({path, mode: '100644', type: 'blob', sha: null})
  }

  const branch = await github.rest.git.getRef({
    owner,
    repo,
    ref: 'heads/docs/convert-guides-code-doc'
  })
  const parentSha = branch.data.object.sha
  const parent = await github.rest.git.getCommit({owner, repo, commit_sha: parentSha})
  const createdTree = await github.rest.git.createTree({
    owner,
    repo,
    base_tree: parent.data.tree.sha,
    tree
  })
  const commit = await github.rest.git.createCommit({
    owner,
    repo,
    message: 'Convert imported guides to code.doc DSL',
    tree: createdTree.data.sha,
    parents: [parentSha]
  })
  core.info(`GUIDE_COMMIT_SHA=${commit.data.sha}`)
  await github.rest.repos.createCommitStatus({
    owner,
    repo,
    sha: parentSha,
    state: 'success',
    context: 'guide-docs/staged-commit',
    description: commit.data.sha,
    target_url: `https://github.com/${owner}/${repo}/commit/${commit.data.sha}`
  })
}
