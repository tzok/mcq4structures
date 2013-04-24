<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:output method="text" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="local">
        <xsl:text>import math&#10;</xsl:text>
        <xsl:text>import matplotlib.pyplot&#10;</xsl:text>
        <xsl:text>import matplotlib.ticker&#10;</xsl:text>
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
        <xsl:text>    outfile = '</xsl:text><xsl:value-of select="outfile"/><xsl:text>'&#10;</xsl:text>
        <xsl:text>    x = [ </xsl:text><xsl:for-each select="residues/value"><xsl:text>'</xsl:text><xsl:value-of select="."/><xsl:text>', </xsl:text></xsl:for-each><xsl:text> ]&#10;</xsl:text>
        <xsl:text>    y = [ </xsl:text><xsl:for-each select="differences/array"><xsl:text>[ </xsl:text><xsl:for-each select="value"><xsl:value-of select="."/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ], </xsl:text></xsl:for-each><xsl:text> ]&#10;</xsl:text>
        <xsl:text>    labels = [ </xsl:text><xsl:for-each select="labels/label"><xsl:text>'</xsl:text><xsl:value-of select="."/><xsl:text>', </xsl:text></xsl:for-each><xsl:text> ]&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    rads = ['0', '&#960;/12', '&#x3c0;/6', 'π/4', 'π/3', '5π/12', 'π/2', '7π/12', '2π/3', '3π/4', '5π/6', '11π/12', 'π']&#10;</xsl:text>
        <xsl:text>    figure = matplotlib.pyplot.figure(figsize=(16, 9))&#10;</xsl:text>
        <xsl:text>    axes = figure.add_subplot(111)&#10;</xsl:text>
        <xsl:text>    axes.xaxis.set_major_locator(matplotlib.ticker.MultipleLocator(5))&#10;</xsl:text>
        <xsl:text>    axes.xaxis.set_major_formatter(matplotlib.ticker.FuncFormatter(lambda i, pos: x[int(i)] if int(i) &lt; len(x) else ''))&#10;</xsl:text>
        <xsl:text>    axes.xaxis.set_minor_locator(matplotlib.ticker.MultipleLocator(1))&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.tick_params(which='major', length=10)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.tick_params(which='minor', length=5)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.yticks([i*math.pi/12.0 for i in range(13)], ['{} = {}°'.format(rads[i], i*15) for i in range(13)])&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.ylim(0, math.pi)&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.xlabel('Residue')&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.ylabel('Difference')&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.grid()&#10;</xsl:text>
        <xsl:text>    &#10;</xsl:text>
        <xsl:text>    plots = []&#10;</xsl:text>
        <xsl:text>    for i in range(len(y)):&#10;</xsl:text>
        <xsl:text>        matplotlib.pyplot.plot(y[i], c=TABLE[i])&#10;</xsl:text>
        <xsl:text>        plots.append(matplotlib.pyplot.Rectangle((0, 0), 1, 1, color=TABLE[i]))&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.legend(plots, labels)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.savefig(outfile, dpi=500, bbox_inches='tight', transparent=True)&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>
