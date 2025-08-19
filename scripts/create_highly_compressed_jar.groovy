#!/usr/bin/env groovy
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.nio.charset.StandardCharsets

// Build the text: "compress-this" repeated 1000 times, separated by dashes
def content = (['compress-this'] * 1000).join('-')

// Create a ZIP (JAR) file containing a single content.txt entry
def outputName = (args && args.length > 0) ? args[0] : 'test-highly-compressed.jar'
new ZipOutputStream(new FileOutputStream(outputName)).withCloseable { zos ->
    zos.putNextEntry(new ZipEntry('content.txt'))
    zos.write(content.getBytes(StandardCharsets.UTF_8))
    zos.closeEntry()
}

println "Created ${outputName} containing content.txt"

// Move file to sonar-pmd-lib/src/test/resources
def moveToFile = new File('../sonar-pmd-lib/src/test/resources/' + outputName)
new File(outputName).renameTo(moveToFile)
println "Moved to: $moveToFile"