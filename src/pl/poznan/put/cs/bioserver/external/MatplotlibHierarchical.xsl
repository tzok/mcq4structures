<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:output method="text"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="hierarchical">
        <xsl:text>import matplotlib.pyplot&#10;</xsl:text>
        <xsl:text>import scipy.cluster.hierarchy&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>if __name__ == '__main__':&#10;</xsl:text>
        <xsl:text>    outfile = '</xsl:text><xsl:value-of select="outfile"/><xsl:text>'&#10;</xsl:text>
        <xsl:text>    method = '</xsl:text><xsl:value-of select="method"/><xsl:text>'&#10;</xsl:text>
        <xsl:text>    data = [ </xsl:text><xsl:for-each select="data/value"><xsl:value-of select="."/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ]&#10;</xsl:text>
        <xsl:text>    labels = [ </xsl:text><xsl:for-each select="labels/label"><xsl:text>'</xsl:text><xsl:value-of select="."/><xsl:text>', </xsl:text></xsl:for-each><xsl:text> ]&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.figure(figsize=(16, 9))&#10;</xsl:text>
        <xsl:text>    linkage = scipy.cluster.hierarchy.linkage(data, method=method)&#10;</xsl:text>
        <xsl:text>    scipy.cluster.hierarchy.dendrogram(linkage, orientation='right', labels=labels)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.savefig(outfile, dpi=500, bbox_inches='tight', transparent=True)&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>
