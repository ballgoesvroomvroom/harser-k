package harser.regex

class Regex {
    companion object {
        // .group(1) is hashtags captured
        // .group(2) is chapter name captured
        // .group(3) is article id
        val header = Regex("^(#{1,6}) (.*?)(?: \\[(.*)\\])?$")

        // use to capture images
        // .group(1) is the image alt text
        // .group(2) is the image src path
        // .group(3) is the image caption (figcaption contents)
        val image = Regex("^!\\[(.*?)\\]\\((.*?)\\)\\[(.*?)\\]$")

        // .group(1) is the link text
        // .group(2) is the link path
        val links = Regex("\\[(.*?)\\]\\((.*?)\\)")

        // use to replace backticks with <code> tags
        val code = Regex("`([^`]+?)`")

        // code block; multi-line code block
        val codeblock = Regex("^```$")

        // denotes a new div
        // .group(1) is the direction "l-r" or "r-l"
        val directed_div = Regex("^\\{ \\[(\\w-\\w)\\]$")

        // denotes a new plain div
        val normal_div = Regex("^\\{$")

        // denotes a closure of div
        val closing_div = Regex("^}$")

        // captures a header for a div; used when there is no need to create a section
        // .group(1) returns the hashtags corersponding to the header level
        // .group(2) returns the actual header content
        val div_header = Regex("^(?<!\\\\)\\{ *(#{1,6})([^ ]+) *}$")

        // captures a list element
        // .group(1) is the list element's content
        val list_ele = Regex("^(?<!\\\\)- (.*)$")

        // captures a file upload element (custom)
        // group 1 returns file name (to be displayed)
        // group 2 returns file description
        // group 3 returns file path stored (along with the file extension of course)
        val file_upload = Regex("\\{(.*?)}\\[(.*?)\\]\\((.*?)\\)")

        // REPLACEMENTS FOR _safeParse
        // to capture ampersand tags
        val amp = Regex("&(?!nbsp;)")

    }
}