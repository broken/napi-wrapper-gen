Napi Wrapper Generator
======================

This reads C++ header files, and generates the necessary C++ header and source files using node-addon-api to use them as native Node components.

This is regloss of my [Node-Webkit-Cpp-Wrapper](https://github.com/broken/node-webkit-cpp-wrapper) which does the same thing but for Nan.

I wish I didn't have to write this myself, but I cannot find anything else like it. T_T


Requirements
------------
* java (jdk)
* node-webkit
* nw-gyp
* nan (v2+)
* ant
* antlr

* create a shortlink to your files to convert. Example: ln -s ../soulsifter/src/soulsifter resources


Dislaimer Section
-----------------
1. This is currently a work in progress to meet the needs for my current project. As such, it is unfinished and has no support.

2. I wrote just enough to wrap the classes I needed. For one, that means it's written mostly like a script, and less like a well structured program.  It also means this is by no means an exhaustive translation tool. However, it's simple enough that you should be able to plug the holes to fit your needs.

3. I do not know antlr. I read just enough to build this grammar to work for me. After testing a true C grammar on my header files, I realized that any complete grammar would be overkill, and writing my own would be much easier when building the translation listeners.

4. Please submit patches if you used/improved this. Or, let me know if you have run across a better tool, and I'll point to it from this doc.


How to run
----------
ant all

  note: that I haven't taken the time to split this out for other projects.


Sample output
-------------
You can view the output of this program by checking out the __wrap.[h,cc]_ files in my [SoulSifterE](https://github.com/broken/soulsifter-e/tree/master/be/src) project.


Test grammar
------------
ant -Dfile=__file.h__ gui
