import groovy.io.FileType

println '===================================='
println 'Creating markdown rule documentation'
println '===================================='


def pmdRuleSourceDirectory = '../sonar-pmd-plugin/src/main/resources/org/sonar/l10n/pmd/rules'
def markdownRuleOutputDirectory = './rules'

/**
 * Creates a markdown warning message for deprecated rules
 * @param rules List of rule identifiers that replace the deprecated rule
 * @return Formatted deprecation warning message in markdown
 */
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

/**
 * Converts HTML rule documentation into markdown format for a given category
 * @param category The rule category to process
 */
def createMarkdownPagesForCategory = {
    category ->
        def currentDir = new File("${pmdRuleSourceDirectory}/${category}")
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
            def file = new File("${markdownRuleOutputDirectory}/${rulename}.md").newWriter()
            file << ruleContent
            file.close()
        }
}

createMarkdownPagesForCategory('pmd')
