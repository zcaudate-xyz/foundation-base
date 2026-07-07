const fs = require('fs')

module.exports = async ({github, context, core, exec}) => {
  const result = await exec.getExecOutput('git', ['diff', '--name-only', '--', 'src-doc'])
  const files = result.stdout.split('\n').map((line) => line.trim()).filter(Boolean)
  core.info(`staging ${files.length} formatted documentation files`)
}
