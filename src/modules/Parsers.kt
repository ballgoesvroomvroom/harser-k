package harser.modules

import harser.regex.Regex

fun warn(vararg item: String, separator: String = "") {
    print("\n[ WARNING ]: ${item.joinToString(separator)}\n")
}

class SubParser {
    // parser for a single article instance
    var root = Article()

    // should only contains instances whose .createsNewScope property is true
    private var containers = arrayListOf<Elements>(root)

    private var currentContainer: Elements = containers[0]
        get() {
            return containers.last()
        }

    private fun addContainer(container: Elements) {
        containers.add(container)
    }

    private fun closeContainer() {
        // close the current container
        containers.removeLast()
    }

    private fun collapseContainer(collapseTo: Int = 1) {
        // container index to collapse to; 1 references the root (article)
        for (i in containers.size -1 downTo collapseTo +1) {
            // stops iteration right before root element (stops at index 2)
            containers.removeLast()
        }
    }

    private fun getMatch(line: String): Pair<Int, List<String>> {
        // determines majority of the token type; ignores the ones handled in .matchLine() such as codeblocks
        val header: MatchResult?  = Regex.header.matchEntire(line)
        if (header != null) {
            return Pair(1, header.groupValues)
        }
        val img: MatchResult?  = Regex.image.matchEntire(line)
        if (img != null) {
            return Pair(2, img.groupValues)
        }
        val link: MatchResult?  = Regex.links.matchEntire(line)
        if (link != null) {
            return Pair(3, link.groupValues)
        }
        val directedDiv: MatchResult?  = Regex.directed_div.matchEntire(line)
        if (directedDiv != null) {
            return Pair(4, directedDiv.groupValues)
        }
        val normalDiv: MatchResult?  = Regex.normal_div.matchEntire(line)
        if (normalDiv != null) {
            return Pair(5, normalDiv.groupValues)
        }
        val closingDiv: MatchResult? = Regex.closing_div.matchEntire(line)
        if (closingDiv != null) {
            return Pair(6, closingDiv.groupValues)
        }
        val divHeader = Regex.div_header.matchEntire(line)
        if (divHeader != null) {
            return Pair(7, divHeader.groupValues)
        }
        val fileUpload = Regex.file_upload.matchEntire(line)
        if (fileUpload != null) {
            return Pair(8, fileUpload.groupValues)
        }

        // normal paragraph
        return Pair(0, arrayListOf(line)) // pass back line
    }

    fun matchLine(line: String) {
        // called on each line iteration
        // extracts tokens from line and builds the elements immediately
        if (line.isEmpty()) {
            // do nothing to an empty string
            return
        }

        // check if context is a code block
        if (currentContainer is CodeBlock) {
            // add content directly into codeblock with .addContent() method

            // match for closing tag first
            if (Regex.codeblock.matches(line)) {
                // escape codeblock
                closeContainer()
            } else {
                // add content
                (currentContainer as CodeBlock).addContent(line) // should be possible since we checked; and is not affected by other threads since addContainer and closeContainer are private methods
            }
            return
        } else {
            val codeblock = Regex.codeblock.matchEntire(line)
            if (codeblock != null) {
                // create a new codeblock
                CodeBlock().let {
                    currentContainer.addChildren(it)
                    addContainer(it)
                }

                return
            }
        }

        // match for list next: check if current container is a list
        val listele = Regex.list_ele.matchEntire(line)
        if (currentContainer is ListContainer) {
            if (listele != null) {
                // line is a list content, add it and move on to the next line
                ListItem().apply {
                    content = listele.groupValues[1]
                    currentContainer.addChildren(this)
                }
                return
            } else {
                // line is not a list content
                // exit list and continue with the remaining matching
                closeContainer()
            }
        } else if (listele != null) {
            // current container is not a list
            ListContainer().apply {
                // create actual list element object

                addChildren(ListItem().apply {
                    content = listele.groupValues[1]
                })

                // add itself to currentContainers
                currentContainer.addChildren(this)
                addContainer(this)
            }
        }

        // start of actual matching for different tags
        val (tokenType: Int, tokenData: List<String>?) = getMatch(line)

        when (tokenType) {
            0 -> {
                // normal paragraphs
                Para().apply {
                    content = line
                    currentContainer.addChildren(this)
                }
            }
            1 -> {
                // header
                val (hashtags: String, chaptername: String, articleid: String?) = tokenData
                if (hashtags.length == 1) {
                    // header 1: regex captured with article id
                    root.id = articleid
                } else {
                    // articleid would be none/null @tobeconfirmed
                    // do nothing
                }

                Section(className="root-section").apply {
                    addChildren(Header().apply {
                        content = chaptername
                        headerType = hashtags.length
                    })

                    collapseContainer(1) // escape back to root
                    currentContainer.addChildren(this) // add section object into root
                    addContainer(this) // add section object as a container
                }
            }
            2 -> {
                // image
                val (alttext: String, src: String, caption: String) = tokenData

                // create new figure instance
                Figure().apply {
                    addChildren(Image().apply {
                        content = src
                        alt = alttext
                    }, Figcaption().apply {
                        content = caption
                    })

                    currentContainer.addChildren(this)
                }
            }
            3 -> {
                // link
                val (linktext: String, path: String) = tokenData

                Link().apply {
                    content = path
                    text = linktext

                    currentContainer.addChildren(this)
                }
            }
            4 -> {
                // directed_div
                val (dir: String) = tokenData

                Div(className="content-container").apply {
                    when (dir) {
                        "l-r" -> addClass("left-right")
                        "r-l" -> addClass("right-left")
                        else -> {
                            warn("Defined direction is invalid; input:", dir)
                        }
                    }

                    currentContainer.addChildren(this)
                    addChildren(this)
                }
            }
            5 -> {
                // normal_div
                Div().apply {
                    currentContainer.addChildren(this)
                    addContainer(this)
                }
            }
            6 -> {
                // closing_div
                closeContainer()
            }
            7 -> {
                // div_header
                val (headerLevel: String, heading: String) = tokenData

                Header().apply {
                    headerType = headerLevel.length
                    content = heading

                    currentContainer.addChildren(this)
                }
            }
            8 -> {
                // file_upload
                val (dispname: String, filedesc: String, filepath: String) = tokenData

                Div(className="fileupload").apply {
                    val fileicon = Image().apply {
                        content = "img/includes/text-file.webp"
                        alt = "icon of .txt files"
                    }

                    val innerDiv = Div(className="fileupload-text")

                    val dispnameP = Para(className="fileupload-header")
                    dispnameP.content = dispname
                    val descP = Para(className="fileupload-desc")
                    descP.content = filedesc

                    innerDiv.addChildren(dispnameP, descP)

                    addChildren(fileicon, innerDiv, Button())
                    currentContainer.addChildren(this)
                }
            }
        }
    }
}

class Parser(var content: String) {
    var subparsers = ArrayList<SubParser>() // stores the subparser instances here
    lateinit var parsedContent: String // caches parsed contents here

    var currentSubParser: SubParser? = null
        get() {
            return subparsers.last()
        }


    // states
    var isActive: Boolean = false // triggered whenever the first article object is created
    var isParsed: Boolean = false

    private fun createArticle() {
        // let the sub parser do all the heavy-lifting
        subparsers.add(SubParser())
        isActive = true
    }

    fun lex() {
        // not really a lexer, takes the string contents and builds a tree map
        // along with adding the semi-parsed tags
        val lines = content.split("\n")

        for (linec in 0..lines.size -1) {
            // ignore any meaningless data that may be possibly at the start of the file
            val line = lines[linec]

            // look for header
            val header: MatchResult? = Regex.header.find(line)
            if (header != null && header.groupValues[1].length == 1) {
                // only trigger for h1 tags; start of a new article
                createArticle()
                currentSubParser?.matchLine(line) // wrap in unnecessary safe call..
            } else if (isActive) {
                // an existing article exists
                currentSubParser?.matchLine(line)
            } else {
                // irrelevant content
                // do nothing
            }
        }
    }

    fun parse(indentLevel: Int = 0): String {
        parsedContent = ""
        for (article in subparsers) {
            parsedContent += "${article.root.parse(indentLevel)}\n"
        }

        parsedContent = parsedContent.dropLast(1) // strip trailing line feed
        return parsedContent
    }
}