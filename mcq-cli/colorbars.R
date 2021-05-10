#! /usr/bin/env Rscript
library(lattice)
library(RColorBrewer)

args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
  stop("At least one argument must be supplied (input file)\n")
}

c <- read.csv(file=args[1], header=FALSE)
residues <- c[1,]
dotbrackets <- c[2,]
c <- c[3:nrow(c),1:ncol(c)]
models <- c[,1]
c <- c[1:nrow(c),2:ncol(c)]
c <- t(c)
m <- as.matrix(c)
mode(m) <- 'numeric'

residues <- unlist(residues)
dotbrackets <- unlist(dotbrackets)
models <- unlist(models)

m <- ifelse(m > 60, 60, m)
p <- colorRampPalette(brewer.pal(9, 'YlOrRd'))

pdf('colorbars.pdf', width=11)
levelplot(m, col.regions=p, xlab='', ylab='', scales=list(x=list(at=0:nrow(m), labels=dotbrackets), y=list(at=1:ncol(m), labels=models)))
dev.off()
