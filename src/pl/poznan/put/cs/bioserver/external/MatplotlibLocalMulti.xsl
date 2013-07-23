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
        <xsl:text>import os&#10;</xsl:text>
        <xsl:text>import os.path&#10;</xsl:text>
        <xsl:text>import shutil&#10;</xsl:text>
        <xsl:text>import subprocess&#10;</xsl:text>
        <xsl:text>import sys&#10;</xsl:text>
        <xsl:text>import tempfile&#10;</xsl:text>
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
        <xsl:text>    x = [-1 * numpy.array(i) for i in [ </xsl:text><xsl:apply-templates select="results/angles"/><xsl:text>] ]&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    files = []&#10;</xsl:text>
        <xsl:text>    for data in x:&#10;</xsl:text>
        <xsl:text>        image = matplotlib.pyplot.imshow([data, data, data], **kwargs)&#10;</xsl:text>
        <xsl:text>        image.get_axes().set_xticks([])&#10;</xsl:text>
        <xsl:text>        image.get_axes().set_yticks([])&#10;</xsl:text>
        <xsl:text>        tmp = tempfile.mktemp('.pdf')&#10;</xsl:text>
        <xsl:text>        matplotlib.pyplot.savefig(tmp, dpi=500, bbox_inches='tight', transparent=True)&#10;</xsl:text>
        <xsl:text>        files.append(tempfile.mktemp('.pdf'))&#10;</xsl:text>
        <xsl:text>        subprocess.call(['pdfcrop', '--hires', tmp, files[-1]])&#10;</xsl:text>
        <xsl:text>        os.remove(tmp)&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    latex = tempfile.mktemp('.tex')&#10;</xsl:text>
        <xsl:text>    with open(latex, 'w') as f:&#10;</xsl:text>
        <xsl:text>        f.write(r'\documentclass[varwidth=true]{standalone}' + '\n')&#10;</xsl:text>
        <xsl:text>        f.write(r'\usepackage{graphicx}' + '\n')&#10;</xsl:text>
        <xsl:text>        f.write(r'\usepackage{setspace}' + '\n')&#10;</xsl:text>
        <xsl:text>        f.write(r'\begin{document}' + '\n')&#10;</xsl:text>
        <xsl:text>        f.write(r'\begin{spacing}{0.5}' + '\n')&#10;</xsl:text>
        <xsl:text>        for tmp in files:&#10;</xsl:text>
        <xsl:text>            f.write(r'    \includegraphics[width=\columnwidth]{' + tmp + r'}' + '\n')&#10;</xsl:text>
        <xsl:text>        f.write(r'\end{spacing}' + '\n')&#10;</xsl:text>
        <xsl:text>        f.write(r'\end{document}' + '\n')&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    outputs = set()&#10;</xsl:text>
        <xsl:text>    subprocess.call(['pdflatex', '-halt-on-error', '-recorder', '-output-directory', tempfile.gettempdir(), latex])&#10;</xsl:text>
        <xsl:text>    with open(latex.replace('.tex', '.fls')) as f:&#10;</xsl:text>
        <xsl:text>        for line in f:&#10;</xsl:text>
        <xsl:text>            if line.startswith('OUTPUT'):&#10;</xsl:text>
        <xsl:text>                for word in line.split():&#10;</xsl:text>
        <xsl:text>                    outputs.add(word)&#10;</xsl:text>
        <xsl:text>                    if '.pdf' in word:&#10;</xsl:text>
        <xsl:text>                        document = word&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    if sys.argv[1].endswith('.pdf'):&#10;</xsl:text>
        <xsl:text>        shutil.copy(document, sys.argv[1])&#10;</xsl:text>
        <xsl:text>    elif sys.argv[1].endswith('.eps'):&#10;</xsl:text>
        <xsl:text>        subprocess.call(['pdftops', document, sys.argv[1]])&#10;</xsl:text>
        <xsl:text>    elif sys.argv[1].endswith('.svg'):&#10;</xsl:text>
        <xsl:text>        subprocess.call(['inkscape', '-z', '-f', document, sys.argv[1]])&#10;</xsl:text>
        <xsl:text>    else:&#10;</xsl:text>
        <xsl:text>        subprocess.call(['gs', '-q', '-dNOPAUSE', '-sDEVICE=png16m', '-sOutputFile=' + sys.argv[1], '-r500', document, '-c', 'quit'])&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>    for f in files:&#10;</xsl:text>
        <xsl:text>        os.remove(f)&#10;</xsl:text>
        <xsl:text>    for f in outputs:&#10;</xsl:text>
        <xsl:text>        if os.path.exists(f):&#10;</xsl:text>
        <xsl:text>            os.remove(f)&#10;</xsl:text>
        <xsl:text>    os.remove(latex)&#10;</xsl:text>
        <xsl:text>    os.remove(latex.replace('.tex', '.fls'))&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="angles">
        <xsl:for-each select="entry/value[@name='AVG_ALL']"><xsl:text>[ </xsl:text><xsl:for-each select="item"><xsl:text>float('</xsl:text><xsl:value-of select="."/><xsl:text>'), </xsl:text></xsl:for-each><xsl:text> ], </xsl:text></xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
