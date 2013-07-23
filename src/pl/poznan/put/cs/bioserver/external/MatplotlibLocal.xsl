<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:output method="text" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:param name="angles"/>

    <xsl:template match="comparisonLocal">
        <xsl:text>import math&#10;</xsl:text>
        <xsl:text>import matplotlib.pyplot&#10;</xsl:text>
        <xsl:text>import matplotlib.ticker&#10;</xsl:text>
        <xsl:text>import sys&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>COLORS = </xsl:text><xsl:apply-templates select="colors"/><xsl:text>&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>if __name__ == '__main__':&#10;</xsl:text>
        <xsl:text>    if len(sys.argv) != 2:&#10;</xsl:text>
        <xsl:text>        print('Usage: python ' + sys.argv[0] + ' OUTFILE.(png|pdf|svg|eps)')&#10;</xsl:text>
        <xsl:text>        exit(1)&#10;</xsl:text>
        <xsl:text>    x = </xsl:text><xsl:apply-templates select="ticks"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    y = </xsl:text><xsl:apply-templates select="angles" mode="items"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    labels = </xsl:text><xsl:apply-templates select="angles" mode="names"/><xsl:text>&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    angles = </xsl:text><xsl:value-of select="$angles"/><xsl:text>&#10;</xsl:text>
        <xsl:text>    y = list(y[i] for i in range(len(y)) if labels[i] in angles)&#10;</xsl:text>
        <xsl:text>    labels = list(labels[i] for i in range(len(labels)) if labels[i] in angles)&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    greek = { 'ALPHA' : r'$\alpha$', 'BETA' : r'$\beta$', 'GAMMA' : r'$\gamma$', 'DELTA' : r'$\delta$', 'EPSILON' : r'$\varepsilon$', 'ZETA' : r'$\zeta$', 'CHI' : r'$\chi$', 'TAU0' : r'$\tau_0$', 'TAU1' : r'$\tau_1$', 'TAU2' : r'$\tau_2$', 'TAU3' : r'$\tau_3$', 'TAU4' : r'$\tau_4$', 'P' : r'$P$', 'AVG_SELECTED' : 'selected', 'AVG_ALL' : 'average', 'PHI' : r'$\phi$', 'PSI' : r'$\psi$', 'OMEGA' : r'$\omega$', 'CHI1' : r'$\chi_1$', 'CHI2' : r'$\chi_2$', 'CHI3' : r'$\chi_3$', 'CHI4' : r'$\chi_4$', 'CHI5' : r'$\chi_5$', 'ETA' : r'$\eta$', 'THETA' : r'$\theta$', 'ETA_PRIM' : r"$\eta'$", 'THETA_PRIM' : r"$\theta'$", 'CALPHA' : r'C-$\alpha$' }&#10;</xsl:text>
        <xsl:text>    labels = list(greek[i] for i in labels)</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    rads = ['0', r'$\frac{\pi}{12}$', r'$\frac{\pi}{6}$', r'$\frac{\pi}{4}$', r'$\frac{\pi}{3}$', r'$\frac{5\pi}{12}$', r'$\frac{\pi}{2}$', r'$\frac{7\pi}{12}$', r'$\frac{2\pi}{3}$', r'$\frac{3\pi}{4}$', r'$\frac{5\pi}{6}$', r'$\frac{11\pi}{12}$', r'$\pi$']&#10;</xsl:text>
        <xsl:text>    figure = matplotlib.pyplot.figure(figsize=(16, 9))&#10;</xsl:text>
        <xsl:text>    axes = figure.add_subplot(111)&#10;</xsl:text>
        <xsl:text>    axes.xaxis.set_major_formatter(matplotlib.ticker.FuncFormatter(lambda i, pos: x[int(i)] if int(i) &lt; len(x) else ''))&#10;</xsl:text>
        <xsl:text>    axes.xaxis.set_major_locator(matplotlib.ticker.MaxNLocator(nbins=16))&#10;</xsl:text>
        <xsl:text>    axes.xaxis.set_minor_locator(matplotlib.ticker.MultipleLocator(math.ceil(len(x) / matplotlib.ticker.Locator.MAXTICKS)))&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.tick_params(which='major', length=10)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.tick_params(which='minor', length=5)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.yticks([i*math.pi/12.0 for i in range(13)], ['{} = {}°'.format(rads[i], i*15) for i in range(13)])&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.ylim(0, math.pi)&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.xlabel('ResID')&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.ylabel('Angular distance')&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.grid()&#10;</xsl:text>
        <xsl:text>    &#10;</xsl:text>
        <xsl:text>    plots = []&#10;</xsl:text>
        <xsl:text>    for i in range(len(y)):&#10;</xsl:text>
        <xsl:text>        color = '#{:02x}{:02x}{:02x}'.format(int(255 * COLORS[i+1][0]), int(255 * COLORS[i+1][1]), int(255 * COLORS[i+1][2]))&#10;</xsl:text>
        <xsl:text>        matplotlib.pyplot.plot(y[i], c=color, lw=2)&#10;</xsl:text>
        <xsl:text>        plots.append(matplotlib.pyplot.Rectangle((0, 0), 1, 1, color=COLORS[i + 1]))&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.legend(plots, labels)&#10;</xsl:text>
        <xsl:text>    matplotlib.pyplot.savefig(sys.argv[1], dpi=500, bbox_inches='tight', transparent=True)&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="ticks">
        <xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:text>'</xsl:text><xsl:value-of select="."/><xsl:text>', </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>

    <xsl:template match="angles" mode="items">
        <xsl:text>[ </xsl:text><xsl:for-each select="entry/value"><xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:text>float('</xsl:text><xsl:value-of select="."/><xsl:text>'), </xsl:text></xsl:for-each><xsl:text> ], </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>

    <xsl:template match="angles" mode="names">
        <xsl:text>[ </xsl:text><xsl:for-each select="entry"><xsl:text>'</xsl:text><xsl:value-of select="key"/><xsl:text>', </xsl:text></xsl:for-each><xsl:text> ]</xsl:text>
    </xsl:template>

    <xsl:template match="colors">
        <xsl:text>( </xsl:text><xsl:for-each select="item"><xsl:text>(</xsl:text><xsl:value-of select="r"/><xsl:text>, </xsl:text><xsl:value-of select="g"/><xsl:text>, </xsl:text><xsl:value-of select="b"/><xsl:text>), </xsl:text></xsl:for-each><xsl:text>)</xsl:text>
    </xsl:template>
</xsl:stylesheet>
