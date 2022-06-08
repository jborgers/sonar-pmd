import groovy.io.FileType

println '===================================='
println 'Creating markdown rule documentation'
println '===================================='


def ruleSourcePath = '../sonar-pmd7-plugin/src/main/resources/org/sonar/l10n/pmd/rules'
def ruleTargetPath = './rules'

def createDeprecationWarning = {
    rules ->

        if (!rules.isEmpty()) {
            def parsedRules = rules.collect {
                def ruleNumber = it.substring(1)
                (ruleNumber.isInteger()) ?
                        "[${it}](https://rules.sonarsource.com/java/RSPEC-${ruleNumber.toInteger()})" : "`java:${it}`"
            }

            return "> :warning: This rule is **deprecated** in favour of ${parsedRules.join(', ')}."
        }
        ""
}

def extractRulesFromContent = {
    content ->
        def pattern = /(rule):(squid):(\w+)/
        def group = (content =~ /$pattern/)

        return group.collect {
            it[3]
        }
}

def removeDeprecationMessage = {
    content ->
        def regex = /(?ms)<p>(\s*)This rule is deprecated, use \{rule:java:(\w+)\} (.*)instead.(\s*)<\/p>/

        if (content =~ regex) {
            return content.replaceFirst(regex, "")
        }

        return content
}

def createMarkdownPagesForCategory = {
    category ->
        def currentDir = new File("${ruleSourcePath}/${category}")
        currentDir.eachFile FileType.FILES, {
            String rulename = it.name.tokenize('.')[0]

            println " * Processing Rule ${rulename}"

            String htmlContent = it.text
            String deprecationWarning = createDeprecationWarning(extractRulesFromContent(htmlContent))
            htmlContent = removeDeprecationMessage(htmlContent).trim()
            String ruleContent = """# ${rulename}
**Category:** `${category}`<br/>
**Rule Key:** `${category}:${rulename}`<br/>
${deprecationWarning}

-----

${htmlContent}
"""
            def file = new File("${ruleTargetPath}/${rulename}.md").newWriter()
            file << ruleContent
            file.close()
        }
}

createMarkdownPagesForCategory('pmd')
createMarkdownPagesForCategory('pmd7-unit-tests')