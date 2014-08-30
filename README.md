node-webkit-cpp-wrapper
=======================

I went looking for a tool like this, but to my surprise it hasn't been built yet. O_o

This is currently a work in progress, but once it meets the needs for my current project, I'll most likely cease work until I finish my current project.

Disclaimer: I wrote just enough to wrap the classes I needed. That means this is by no means an exhaustive translation tool. However, it's simple enough that you should be able to fix the holes to fit your needs.

Disclaimer 2: I do not know antlr. I read just enough to build my janky grammar to work for me. Too bad there already is not a c++ grammar made that I could've used, and I didn't want to invest the time to actually get the c one working for c++.

How to run:
antlr4 nodewebkitcppwrapper.g4 && javac nodewebkitcppwrapper*.java HeaderWrapper*.java && grun nodewebkitcppwrapper header -tree < __file.h__ && java HeaderWrapperTool __file.h__

Test grammar:
antlr4 nodewebkitcppwrapper.g4 && javac nodewebkitcppwrapper*.java && grun nodewebkitcppwrapper header -gui < __file.h__

