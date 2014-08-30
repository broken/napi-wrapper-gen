/**
 * A C++ header grammar that only analyzes information that would be relevant to
 * a node-webkit wrapper class.
 */
grammar nodewebkitwrapper;

header: body ;

body
  :  namespace body
  |  classDeclaration body
  |  usingDeclaration body
  |  cppClass
  |
  ;

type: typeName Modifier? ;
typeName: INT | BOOL | STRING | VOID | Identifier ;

classDeclaration: CLASS Identifier ';' ;

usingDeclaration: USING NAMESPACE Identifier ';' ;

classBlock: publicBlock* ;

publicBlock: constructor | destructor | method | friend | equalsMethod ;

constructor: EXPLICIT? Identifier parameterList ;
destructor: '~' Identifier '();' ;
method: STATIC? type Identifier parameterList CONST? ';'? ;
friend: FRIEND Identifier ';' ;
equalsMethod: VOID OPEQ parameterList ;

parameterList: ('()' | '(' (parameter ',')* parameter ');' | '();') ;
parameter: CONST? type Identifier ;

namespace: NAMESPACE Identifier '{' body '}' ;

cppClass: CLASS Identifier '{' classBlock '};' ;



FRIEND: 'friend' ;
NAMESPACE: 'namespace' ;
USING: 'using' ;
CLASS: 'class' ;
CONST: 'const' ;
EXPLICIT: 'explicit' ;
STATIC: 'static' ;
VOID: 'void' ;

INT: 'int' ;
BOOL: 'bool' ;
STRING: 'string' ;

OPEQ: 'operator=' ;





Identifier: (Nondigit ( Nondigit | Digit )* '::')* Nondigit ( Nondigit | Digit )* (LT Nondigit ( Nondigit | Digit )* GT)? Modifier? ;

PrivateBlock: 'private:' ~['}']*
    -> skip ;

Public: 'public:'
    -> skip ;

Directive: '#' ~[\r\n]*
    -> skip ;

Modifier: '*' | '&' ;

WS: [ \t]+
    -> skip ;

Newline: ( '\r' '\n'?
        |   '\n'
        )
    -> skip ;

BlockComment: '/*' .*? '*/'
    -> skip ;

LineComment: '//' ~[\r\n]*
    -> skip ;

fragment
Nondigit: [a-zA-Z_] ;

fragment
Digit: [0-9] ;

fragment LT: '<' ;
fragment GT: '>' ;
