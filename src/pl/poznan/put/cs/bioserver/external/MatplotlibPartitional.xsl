<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:output method="text"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="partitionalClustering">
        <xsl:text>import matplotlib.pyplot&#10;</xsl:text>
        <xsl:text>import textwrap&#10;</xsl:text>
        <xsl:text>import sys&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>TABLE = (&#10;</xsl:text>
        <xsl:text>    (173/255,  35/255,  35/255),&#10;</xsl:text>
        <xsl:text>    ( 42/255,  75/255, 215/255),&#10;</xsl:text>
        <xsl:text>    ( 29/255, 105/255,  20/255),&#10;</xsl:text>
        <xsl:text>    (129/255,  74/255,  25/255),&#10;</xsl:text>
        <xsl:text>    (129/255,  38/255, 192/255),&#10;</xsl:text>
        <xsl:text>    (160/255, 160/255, 160/255),&#10;</xsl:text>
        <xsl:text>    (129/255, 197/255, 122/255),&#10;</xsl:text>
        <xsl:text>    (157/255, 175/255, 255/255),&#10;</xsl:text>
        <xsl:text>    (41/255,  208/255, 208/255),&#10;</xsl:text>
        <xsl:text>    (255/255, 146/255,  51/255),&#10;</xsl:text>
        <xsl:text>    (255/255, 238/255,  51/255),&#10;</xsl:text>
        <xsl:text>    (233/255, 222/255, 187/255),&#10;</xsl:text>
        <xsl:text>    (255/255, 205/255, 243/255),&#10;</xsl:text>
        <xsl:text>    ( 87/255,  87/255,  87/255),&#10;</xsl:text>
        <xsl:text>)&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>if __name__ == '__main__':&#10;</xsl:text>
        <xsl:text>    if len(sys.argv) != 2:&#10;</xsl:text>
        <xsl:text>        print('Usage: python ' + sys.argv[0] + ' OUTFILE.(png|pdf|svg|eps)')&#10;</xsl:text>
        <xsl:text>        exit(1)&#10;</xsl:text>
        <xsl:text>    x = </xsl:text><xsl:apply-templates select="cluster" mode="x"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    y = </xsl:text><xsl:apply-templates select="cluster" mode="y"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    mx = </xsl:text><xsl:apply-templates select="medoids" mode="x"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    my = </xsl:text><xsl:apply-templates select="medoids" mode="y"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    labels = </xsl:text><xsl:apply-templates select="labels"/><xsl:text>&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    plots = []&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.figure(figsize=(16, 9))&#10;</xsl:text>
        <xsl:text>    for i in range(len(x)):&#10;</xsl:text>
        <xsl:text>        plots.append(matplotlib.pyplot.scatter(x[i], y[i], 100, c=TABLE[i]))&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.scatter(mx, my, 200, 'k',    'x', lw=2)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.scatter(mx, my, 200, 'none', 'o', lw=2)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.legend(plots, labels, bbox_to_anchor=(1 + 1/128, 1), loc=2, borderaxespad=0)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.savefig(sys.argv[1], dpi=500, bbox_inches='tight', transparent=True)&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="medoids" mode="x">
        <xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:value-of select="x"/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>
    <xsl:template match="medoids" mode="y">
        <xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:value-of select="y"/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>

    <xsl:template match="cluster" mode="x">
        <xsl:text>[ </xsl:text><xsl:for-each select="points"><xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:value-of select="x"/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ], </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>
    <xsl:template match="cluster" mode="y">
        <xsl:text>[ </xsl:text><xsl:for-each select="points"><xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:value-of select="y"/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ], </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>


    <xsl:template match="labels">
        <xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:text>'\n'.join(textwrap.wrap('</xsl:text><xsl:value-of select="."/><xsl:text>')), </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>
        
</xsl:stylesheet>
