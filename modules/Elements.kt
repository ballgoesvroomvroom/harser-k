package harser.modules

import harser.constants.INDENT

open class Elements(var className: String = "", var id: String = "") {
    open var hasClosingTag = true
    open var createsNewScope = false

    var children = ArrayList<Elements>()

    open var parent: Elements? = null
    open var content: String = ""

    fun addChildren(vararg childrenToAdd: Elements): Boolean {
        // can only be added if createsNewScope is true (can host children elements)
        if (!this.createsNewScope) {
            return false
        } else {
            this.children.addAll(*childrenToAdd.toMutableList())
        }

        return true
    }

    fun addClass(vararg classNameToAdd: String) {
        if (className.length == 0) {
            // no previous content
            className = classNameToAdd.joinToString(" ")
        } else {
            className += " ${classNameToAdd.joinToString(" ")}" // add an extra space padding at the front
        }
    }

    fun parseChildren(parentIndentLevel: Int = 1): String {
        // iterate through children and concatenate the parsed content
        var childrenParsed = ""
        for (childrenObj in children) {
            childrenParsed += "${childrenObj.parse(parentIndentLevel +1)}\n"
        }

        return childrenParsed.dropLast(1)
    }

    open fun parse(indentLevel: Int = 0): String {
        return ""
    }
}

class Article(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override var createsNewScope = true

    override fun parse(indentLevel: Int): String {
        var indentation = INDENT.repeat(indentLevel)
        return "$indentation<article id=\"$id\" class=\"$className\">\n${parseChildren(indentLevel)}\n$indentation</article>"
    }
}

class Section(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override var createsNewScope = true

    override fun parse(indentLevel: Int): String {
        // self.content being the actual destination link
        var indentation = INDENT.repeat(indentLevel)
        return "$indentation<section id=\"$id\" class=\"$className\" href=\"$content\">\n${parseChildren(indentLevel)}\n$indentation</section>"
    }
}

class Div(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override var createsNewScope = true

    override fun parse(indentLevel: Int): String {
        // self.content being the actual destination link
        var indentation = INDENT.repeat(indentLevel)
        return "$indentation<div id=\"$id\" class=\"$className\" href=\"$content\">\n${parseChildren(indentLevel)}\n$indentation</div>"
    }
}

class Para(className: String = "", id: String = "") : Elements(className=className, id=id) {
    fun _parseInline() {
        // parses inline tags in content
        content = content
    }

    override fun parse(indentLevel: Int): String {
        _parseInline()

        return "${INDENT.repeat(indentLevel)}<p id=\"$id\" class=\"$className\">$content</p>"
    }
}

class Header (className: String = "", id: String = "") : Elements(className=className, id=id) {
    override var createsNewScope = true

    // specific properties
    var headerType: Int = 1 // headerTpe == 1; h1 tag; all the way until headerType == 6; h6 tag

    override fun parse(indentLevel: Int): String {
        var indentation = INDENT.repeat(indentLevel)

        return "$indentation<h$headerType id=\"$id\" class=\"$className\">\n$indentation$INDENT$content\n$indentation</h$headerType>"
    }
}

class Image(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override var hasClosingTag = false

    // specific properties
    var alt: String = ""

    override fun parse(indentLevel: Int): String {
        // self.content being the src for the image
        return "${INDENT.repeat(indentLevel)}<img id=\"$id\" class=\"$className\" src=\"$content\" alt=\"$content\">"
    }
}

class Link(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override var createsNewScope = true

    // specific properties
    var text: String = ""

    override fun parse(indentLevel: Int): String {
        // self.content being the actual destination link
        var indentation = INDENT.repeat(indentLevel)
        return "$indentation<a id=\"$id\" class=\"$className\" href=\"$content\">\n$indentation$INDENT$text${parseChildren(indentLevel)}\n$indentation</a>"
    }
}

class Figure(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override var createsNewScope = true

    override fun parse(indentLevel: Int): String {
        var indentation = INDENT.repeat(indentLevel)
        return "$indentation<figure id=\"$id\" class=\"$className\">\n${parseChildren(indentLevel)}\n$indentation</figure>"
    }
}

class Figcaption(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override fun parse(indentLevel: Int): String {
        return "${INDENT.repeat(indentLevel)}<figcaption>$content</figcaption>"
    }
}

class CodeBlock(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override var createsNewScope = true

    // specific properties
    var code_contents = ArrayList<String>()

    fun addContent(line: String) {
        // adds the line (without a line feed character at the end) to code_contents
        code_contents.add(line)
    }

    override fun parse(indentLevel: Int): String {
        return "${INDENT.repeat(indentLevel)}<pre><code>${parseChildren(indentLevel)}</code></pre>"
    }
}

class ListContainer(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override var createsNewScope = true

    // specific properties
    var listType: String = "ul" // unordered list // "ol": "ordered list"

    override fun parse(indentLevel: Int): String {
        var indentation = INDENT.repeat(indentLevel);
        return "$indentation<$listType id=\"$id\" class=\"$className\">\n${parseChildren(indentLevel)}\n$indentation</$listType>"
    }
}

class ListItem(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override fun parse(indentLevel: Int): String {
        // content is the actual list content
        return "${INDENT.repeat(indentLevel)}<li id=\"$id\" class=\"$className\">${parseChildren(indentLevel)}</li>"
    }
}

class Button(className: String = "", id: String = "") : Elements(className=className, id=id) {
    override var createsNewScope = true

    override fun parse(indentLevel: Int): String {
        var indentation = INDENT.repeat(indentLevel);
        var header = "$indentation<button id=\"$id\" class=\"$className\">"
        var children = parseChildren(indentLevel)

        return if (children.length > 0) {
            "$header\n$children\n$indentation</button>"
        } else {
            // no children content; add immediate content without line breaks
            "$header$content</button>"
        }
    }
}
