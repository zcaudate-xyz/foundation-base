const fs = require('fs')

module.exports = async ({github, context, core, exec}) => {
  const {owner, repo} = context.repo
  const result = await exec.getExecOutput('git', ['diff', '--name-only', '--', 'src-doc'])
  const files = result.stdout.split('\n').map((line) => line.trim()).filter(Boolean)
  core.info(`staging ${files.length} formatted documentation files`)

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

  const cleanup = [
    '.github/scripts/convert_merged_docs_to_dsl.py',
    '.github/scripts/format_docs.py',
    '.github/scripts/format_all_docs.sh',
    '.github/scripts/convert_merged_docs_to_dsl.clj',
    '.github/scripts/render_merged_docs_dsl.py',
    '.github/scripts/test.txt',
    '.github/scripts/create_formatted_commit.js',
    '.github/workflows/apply-formatted-docs.yml',
    '.github/workflows/format-docs-pr.yml',
    '.github/workflows/validate-consolidated-docs.yml'
  ]
  for (const path of cleanup) {
    tree.push({path, mode: '100644', type: 'blob', sha: null})
  }

  const parent = await github.rest.git.getCommit({owner, repo, commit_sha: context.sha})
  const createdTree = await github.rest.git.createTree({
    owner,
    repo,
    base_tree: parent.data.tree.sha,
    tree
  })
  const commit = await github.rest.git.createCommit({
    owner,
    repo,
    message: 'Format consolidated guides with code.doc DSL',
    tree: createdTree.data.sha,
    parents: [context.sha]
  })
  core.info(`FORMATTED_COMMIT_SHA=${commit.data.sha}`)
  core.setOutput('commit_sha', commit.data.sha)
  await github.rest.issues.createComment({
    owner,
    repo,
    issue_number: 349,
    body: `Staged formatted documentation commit: \`${commit.data.sha}\``
  })
}
