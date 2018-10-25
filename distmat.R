#! /usr/bin/env Rscript
library(gplots)
library(RColorBrewer)

args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
  stop("At least one argument must be supplied (input file)\n")
}

c <- read.csv(file=args[1], header=TRUE)
m <- as.matrix(c[2:length(c)])
rownames(m) <- colnames(m)

pdf('heatmap.pdf', width=10, height=10)
dm <- as.dist(m)
heatmap.2(as.matrix(dm), hclustfun=function(d) hclust(d, method='ward.D2'), symm=TRUE, revC=TRUE, col=brewer.pal(9, 'YlOrRd'), trace='none')
dev.off()
