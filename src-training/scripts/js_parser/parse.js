const parser = require('@babel/parser');
const fs = require('fs');

const inputFile = process.argv[2];
const code = fs.readFileSync(inputFile, 'utf8');

const ast = parser.parse(code, {
    sourceType: 'module',
    plugins: [
        'jsx',
        'typescript',
        'classProperties',
        'objectRestSpread'
    ]
});

console.log(JSON.stringify(ast, null, 2));
