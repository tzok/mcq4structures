#! /usr/bin/env python
import csv
import sys

import matplotlib.cm
import matplotlib.pyplot

if __name__ == '__main__':
    data = list()
    with open(sys.argv[1]) as csvfile:
        reader = csv.reader(csvfile)
        for row in reader:
            if row[0] == 'Name':
                continue
            data.append([row[0], float(row[1]), float(row[2]), row[3]])

    clusters = sorted({x[3] for x in data})

    fig, axis = matplotlib.pyplot.subplots()

    for current in clusters:
        xs, ys = [], []
        for name, x, y, cluster in data:
            if cluster == current:
                xs.append(x)
                ys.append(y)
        axis.scatter(xs, ys, label=current)

    box = axis.get_position()
    axis.set_position([box.x0, box.y0, box.width * 0.8, box.height])

    axis.legend(loc='upper left', bbox_to_anchor=(1.05, 1.0))
    axis.set_xlabel('MDS 1')
    axis.set_ylabel('MDS 2')
    matplotlib.pyplot.show()
