<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:output method="text" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:param name="cmap" select="'matplotlib.cm.RdYlGn'"/>
    <xsl:param name="interpolation" select="'none'"/>
    <xsl:param name="min" select="'0'"/>
    <xsl:param name="max" select="'3.1415'"/>

    <xsl:template match="comparisonLocalMulti">
        <xsl:text>import math&#10;</xsl:text>
        <xsl:text>import matplotlib.cm&#10;</xsl:text>
        <xsl:text>import matplotlib.pyplot&#10;</xsl:text>
        <xsl:text>import numpy&#10;</xsl:text>
        <xsl:text>import sys&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>if __name__ == '__main__':&#10;</xsl:text>
        <xsl:text>    if len(sys.argv) != 2:&#10;</xsl:text>
        <xsl:text>        print('Usage: python ' + sys.argv[0] + ' OUTFILE.(png|pdf|svg|eps)')&#10;</xsl:text>
        <xsl:text>        exit(1)&#10;</xsl:text>
        <xsl:text>    kwargs = dict()&#10;</xsl:text>
        <xsl:text>    kwargs['cmap'] = </xsl:text><xsl:value-of select="$cmap"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    kwargs['interpolation'] = '</xsl:text><xsl:value-of select="$interpolation"/><xsl:text>'&#10;</xsl:text>
        <xsl:text>    # vmin = -max, because the colormap is from red to green and we multiply vector x by -1 to reverse order&#10;</xsl:text>
        <xsl:text>    kwargs['vmin'] = -</xsl:text><xsl:value-of select="$max"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    kwargs['vmax'] = </xsl:text><xsl:value-of select="$min"/><xsl:text>&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    x = [-1 * numpy.array(i) for i in </xsl:text><xsl:apply-templates select="comparisonLocal"/><xsl:text>]&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    image = matplotlib.pyplot.imshow([x, x, x], **kwargs)&#10;</xsl:text>
        <xsl:text>    image.get_axes().set_xticks([])&#10;</xsl:text>
        <xsl:text>    image.get_axes().set_yticks([])&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.savefig(sys.argv[1], dpi=500, bbox_inches='tight', transparent=True)&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="comparisonLocal">
        <xsl:text>[ </xsl:text><xsl:apply-templates select="angles"/><xsl:text> ], </xsl:text>
    </xsl:template>

    <xsl:template match="angles">
        <xsl:for-each select="entry/value[@name='AVERAGE']"><xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:text>float('</xsl:text><xsl:value-of select="."/><xsl:text>'), </xsl:text></xsl:for-each><xsl:text> ], </xsl:text></xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
