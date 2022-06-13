package harser

import harser.modules.Parser
import java.io.File
import java.io.InputStream

fun getContents(filename: String): String {
//    val inputStream: InputStream = File(filename).inputStream()
//    val inputString: String = inputStream.bufferedReader().use { it.readText() }
//    return inputString
    return "# Header 1\n\nLorem ipsum\n{filename.txt}[file desc..](../resources/hello.txt)"
}

fun main() {
    Parser(getContents("input.txt")).apply {
        lex()
        parse()

        // write using .parsedContent
        println(parsedContent)
    }
}