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
  core.info(`created ${tree.length} documentation blobs`)
}
