<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:output method="text"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="hierarchicalClustering">
        <xsl:text>import matplotlib.pyplot&#10;</xsl:text>
        <xsl:text>import scipy.cluster.hierarchy&#10;</xsl:text>
        <xsl:text>import sys&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>if __name__ == '__main__':&#10;</xsl:text>
        <xsl:text>    if len(sys.argv) != 2:&#10;</xsl:text>
        <xsl:text>        print('Usage: python ' + sys.argv[0] + ' OUTFILE.(png|pdf|svg|eps)')&#10;</xsl:text>
        <xsl:text>        exit(1)&#10;</xsl:text>
        <xsl:text>    method = '</xsl:text><xsl:value-of select="method"/><xsl:text>'.lower()&#10;</xsl:text>
        <xsl:text>    data = </xsl:text><xsl:apply-templates select="comparison/distanceMatrix"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    labels = </xsl:text><xsl:apply-templates select="comparison/labels"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.figure(figsize=(16, 9))&#10;</xsl:text>
        <xsl:text>    linkage = scipy.cluster.hierarchy.linkage(data, method=method)&#10;</xsl:text>
        <xsl:text>    scipy.cluster.hierarchy.dendrogram(linkage, orientation='right', labels=labels)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.savefig(sys.argv[1], dpi=500, bbox_inches='tight', transparent=True)&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="labels">
        <xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:text>'</xsl:text><xsl:value-of select="."/><xsl:text>', </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>

    <xsl:template match="distanceMatrix">
        <xsl:text>[ </xsl:text><xsl:for-each select="row"><xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:text>float('</xsl:text><xsl:value-of select="."/><xsl:text>'), </xsl:text></xsl:for-each><xsl:text> ], </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>
</xsl:stylesheet>
