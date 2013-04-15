all: libclustalw.so clustalw.dll

libclustalw.so: clustalw1.83.linux
	cd $^; make CC=gcc CFLAGS="-c -fPIC" LFLAGS="-shared" -B -j8 -f makefile.linux
	cp $^/clustalw $@

clustalw.dll: clustalw1.83.linux
	cd $^; make CC=i486-mingw32-gcc CFLAGS="-c" LFLAGS="-shared" -B -j8 -f makefile.linux
	cp $^/clustalw $@

clustalw1.83.linux:
	curl ftp://ftp-igbmc.u-strasbg.fr/pub/ClustalW/clustalw1.83.linux.tar.gz | tar xz
	patch $@/clustalw.c 0001-clustalw-noexit.patch
	patch $@/amenu.c 0002-amenu-noexit.patch
	patch $@/interface.c 0003-interface-noexit.patch
