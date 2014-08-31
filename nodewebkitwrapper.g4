/**
 * A C++ header grammar that only analyzes information that would be relevant to
 * a node-webkit wrapper class.
 */
grammar nodewebkitwrapper;

header: body;

body
  :  namespace body
  |  classDeclaration body
  |  usingDeclaration body
  |  cppClass
  |
  ;

namespace: NAMESPACE Identifier '{' body '}';
classDeclaration: CLASS Identifier SEMICOLON;

usingDeclaration: USING NAMESPACE Identifier SEMICOLON;

classBlock: publicBlock* ;

publicBlock: constructor | destructor | method | friend | equalsMethod ;

constructor: EXPLICIT? Identifier parameterList SEMICOLON;
destructor: '~' Identifier LPAREN RPAREN SEMICOLON;
method: STATIC? type Identifier parameterList CONST? SEMICOLON;
friend: FRIEND Identifier SEMICOLON;
equalsMethod: VOID OPEQ parameterList ;

parameterList: LPAREN (parameter (parameter)*)? RPAREN;
parameter: type Identifier;

cppClass: CLASS Identifier '{' classBlock '}' SEMICOLON;

type: CONST? (INT | BOOL | STRING | VOID | VECTOR | Identifier) Modifier?;

COMMA: ',' -> skip;
LPAREN: '(';
RPAREN: ')';
SEMICOLON: ';';

FRIEND: 'friend' ;
NAMESPACE: 'namespace' ;
USING: 'using' ;
CLASS: 'class' ;
CONST: 'const' ;
EXPLICIT: 'explicit' ;
STATIC: 'static' ;
OPEQ: 'operator=' ;

VOID: 'void';
INT: 'int';
BOOL: 'bool';
STRING: 'string';
VECTOR: 'vector';

STAR: '*';
AMPERSAND: '&';


NamespacePrefix: IdentifierName '::' (NamespacePrefix)?;
Identifier: NamespacePrefix? IdentifierName Generic? Modifier*;
fragment IdentifierName: Nondigit ( Nondigit | Digit )*;
Generic: '<' TypeList '>';
TypeList: Identifier (COMMA Identifier)?;

Modifier: STAR | AMPERSAND;



PrivateBlock: 'private:' ~['}']*
    -> skip ;

Public: 'public:'
    -> skip ;

Directive: '#' ~[\r\n]*
    -> skip ;

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

fragment Nondigit: [a-zA-Z_];
fragment Digit: [0-9];

