node-webkit-cpp-wrapper
=======================

I went looking for a tool like this, but to my surprise, it hasn't been built yet. O_o


Requirements
------------
* node-webkit
* nw-gyp
* nan


Dislaimer Section
-----------------
1. This is currently a work in progress, but once it meets the needs for my current project, I'll most likely cease work until I finish that project.

2. I wrote just enough to wrap the classes I needed. That means this is by no means an exhaustive translation tool. However, it's simple enough that you should be able to plug the holes to fit your needs.

3. I do not know antlr. I read just enough to build this grammar to work for me. I could have probably used antlr3, as I believe there is a C++ grammar for it, but after testing the C grammar on my header files, I realized that any complete grammar would be overkill, and writing my own would be much easier when building the translation listeners.

4. Please submit patches if you used/improved this. Or, let me know if you have run across a better tool, and I'll point to it from this doc.


How to run
----------
antlr4 nodewebkitwrapper.g4 && javac nodewebkitwrapper*.java HeaderWrapper*.java SourceWrapperTool*.java && grun nodewebkitwrapper header -tree < __file.h__ && java HeaderWrapperTool __file.h__ > __file_wrap.h__ && java SourceWrapperTool __file.h__ > __file_wrap.cpp__

Test grammar
------------
antlr4 nodewebkitwrapper.g4 && javac nodewebkitwrapper*.java && grun nodewebkitwrapper header -gui < __file.h__

