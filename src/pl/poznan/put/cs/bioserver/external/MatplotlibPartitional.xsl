<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:output method="text"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="partitional">
        <xsl:text>import matplotlib.pyplot&#10;</xsl:text>
        <xsl:text>import textwrap&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>if __name__ == '__main__':&#10;</xsl:text>
        <xsl:text>    outfile = '</xsl:text><xsl:value-of select="outfile"/><xsl:text>'&#10;</xsl:text>
        <xsl:text>    x = [ </xsl:text><xsl:for-each select="x/array"><xsl:text>[ </xsl:text><xsl:for-each select="value"><xsl:value-of select="."/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ], </xsl:text></xsl:for-each><xsl:text> ]&#10;</xsl:text>
        <xsl:text>    y = [ </xsl:text><xsl:for-each select="y/array"><xsl:text>[ </xsl:text><xsl:for-each select="value"><xsl:value-of select="."/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ], </xsl:text></xsl:for-each><xsl:text> ]&#10;</xsl:text>
        <xsl:text>    mx = [ </xsl:text><xsl:for-each select="mx/value"><xsl:value-of select="."/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ]&#10;</xsl:text>
        <xsl:text>    my = [ </xsl:text><xsl:for-each select="my/value"><xsl:value-of select="."/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ]&#10;</xsl:text>
        <xsl:text>    labels = [ </xsl:text><xsl:for-each select="labels/label"><xsl:text>'\n'.join(textwrap.wrap('</xsl:text><xsl:value-of select="."/><xsl:text>')), </xsl:text></xsl:for-each><xsl:text> ]&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    c = ('b', 'g', 'r', 'c', 'm', 'y')&#10;</xsl:text>
        <xsl:text>    plots = []&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.figure(figsize=(16, 9))&#10;</xsl:text>
        <xsl:text>    for i in range(len(x)):&#10;</xsl:text>
        <xsl:text>        plots.append(matplotlib.pyplot.scatter(x[i], y[i], 100, c[i]))&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.scatter(mx, my, 200, 'k',    'x', lw=2)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.scatter(mx, my, 200, 'none', 'o', lw=2)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.legend(plots, labels, bbox_to_anchor=(1 + 1/128, 1), loc=2, borderaxespad=0)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.savefig(outfile, dpi=500, bbox_inches='tight', transparent=True)&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>
