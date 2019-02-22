#! /usr/bin/env python
from matplotlib.patches import Patch

import csv
import matplotlib
import matplotlib.cm
import matplotlib.colors
import matplotlib.pyplot as plt
import numpy as np
import optparse

if __name__ == '__main__':
    # option parsing
    parser = optparse.OptionParser()
    parser.add_option('-i', '--input', help='input CSV file')

    (options, args) = parser.parse_args()

    if not options.input:
        parser.print_help()
        exit(1)

    with open(options.input) as csvfile:
        reader = csv.reader(csvfile)
        rows = [row for row in reader]

    # top xlabels e.g. G (
    xlabels = []
    for i in range(1, len(rows[0])):
        xlabels.append('{}\n{}'.format(rows[1][i], rows[2][i]))

    # data and ylabels i.e. models names
    ylabels = []
    data = []
    for row in rows[3:]:
        ylabels.append(row[0].replace('_', '\\_'))
        row = map(lambda x: float(x), row[1:])
        row = map(lambda x: 0 if x < 15 else 1 if x < 30 else 2 if x < 60 else 3, row)
        data.append(list(row))

    # the plot itself
    fig, ax = plt.subplots()
    ax.pcolor(data, cmap=matplotlib.cm.YlOrRd)

    # bottom axis with residue numbers
    xticks = np.arange(len(xlabels), step=5)
    xticks = np.asarray(xticks) + 0.5
    ax.set_xticks(xticks)
    ax.set_xticklabels(map(lambda x: int(x + 0.5), xticks))

    # top axis with residue letter and dot-bracket
    ax2 = ax.twiny()
    ax2.set_xticks(np.arange(len(xlabels)))
    ax2.set_xticklabels(xlabels)

    # left axis with model names
    yticks = np.arange(len(rows) - 3)
    yticks = np.asarray(yticks) + 0.5
    plt.yticks(yticks, ylabels)

    # legend
    cmap = matplotlib.cm.YlOrRd
    norm = matplotlib.colors.Normalize(vmin=0, vmax=3)
    elements = [
        Patch(facecolor=cmap(norm(0)), label='$0^\circ-15^\circ$'),
        Patch(facecolor=cmap(norm(1)), label='$15^\circ-30^\circ$'),
        Patch(facecolor=cmap(norm(2)), label='$30^\circ-60^\circ$'),
        Patch(facecolor=cmap(norm(3)), label='$>60^\circ$')
    ]
    ax.legend(handles=elements, loc='upper center', bbox_to_anchor=(0.5, -0.05), ncol=4, fancybox=True, shadow=True)

    plt.show()
