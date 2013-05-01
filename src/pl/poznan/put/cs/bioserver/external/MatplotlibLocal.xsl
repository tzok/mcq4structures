<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:output method="text" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:param name="angles"/>

    <xsl:template match="localComparisonResults">
        <xsl:text>import math&#10;</xsl:text>
        <xsl:text>import matplotlib.pyplot&#10;</xsl:text>
        <xsl:text>import matplotlib.ticker&#10;</xsl:text>
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
        <xsl:text>    x = </xsl:text><xsl:apply-templates select="ticks"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    y = </xsl:text><xsl:apply-templates select="deltas" mode="items"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    labels = </xsl:text><xsl:apply-templates select="deltas" mode="names"/><xsl:text>&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    angles = </xsl:text><xsl:value-of select="$angles"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    y = list(y[i] for i in range(len(y)) if labels[i] in angles)&#10;</xsl:text>
        <xsl:text>    labels = list(labels[i] for i in range(len(labels)) if labels[i] in angles)&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    greek = { 'ALPHA' : r'$\alpha$', 'BETA' : r'$\beta$', 'GAMMA' : r'$\gamma$', 'DELTA' : r'$\delta$', 'EPSILON' : r'$\varepsilon$', 'ZETA' : r'$\zeta$', 'CHI' : r'$\chi$', 'TAU0' : r'$\tau_0$', 'TAU1' : r'$\tau_1$', 'TAU2' : r'$\tau_2$', 'TAU3' : r'$\tau_3$', 'TAU4' : r'$\tau_4$', 'P' : r'$P$', 'AVERAGE' : 'average', 'PHI' : r'$\phi$', 'PSI' : r'$\psi$', 'OMEGA' : r'$\omega$' }&#10;</xsl:text>
        <xsl:text>    labels = list(greek[i] for i in labels)</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    rads = ['0', r'$\frac{\pi}{12}$', r'$\frac{\pi}{6}$', r'$\frac{\pi}{4}$', r'$\frac{\pi}{3}$', r'$\frac{5\pi}{12}$', r'$\frac{\pi}{2}$', r'$\frac{7\pi}{12}$', r'$\frac{2\pi}{3}$', r'$\frac{3\pi}{4}$', r'$\frac{5\pi}{6}$', r'$\frac{11\pi}{12}$', r'$\pi$']&#10;</xsl:text>
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
        <xsl:text>        matplotlib.pyplot.plot(y[i], c=TABLE[i], lw=2)&#10;</xsl:text>
        <xsl:text>        plots.append(matplotlib.pyplot.Rectangle((0, 0), 1, 1, color=TABLE[i]))&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.legend(plots, labels)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.savefig(sys.argv[1], dpi=500, bbox_inches='tight', transparent=True)&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="ticks">
        <xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:text>'</xsl:text><xsl:value-of select="."/><xsl:text>', </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>

    <xsl:template match="deltas" mode="items">
        <xsl:text>[ </xsl:text><xsl:for-each select="angle"><xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:value-of select="."/><xsl:text>, </xsl:text></xsl:for-each><xsl:text> ], </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>

    <xsl:template match="deltas" mode="names">
        <xsl:text>[ </xsl:text><xsl:for-each select="angle"><xsl:text>'</xsl:text><xsl:value-of select="@name"/><xsl:text>', </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>
</xsl:stylesheet>
