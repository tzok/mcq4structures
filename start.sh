#! /bin/bash
java -cp bin/:$(ls lib/* | tr '\n' ':') pl.poznan.put.cs.bioserver.gui.Gui
