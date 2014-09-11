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

namespace: NAMESPACE Identifier LCBRACE body RCBRACE;
classDeclaration: CLASS Identifier SEMICOLON;

usingDeclaration: USING NAMESPACE Identifier SEMICOLON;

classBlock: publicBlock* ;

publicBlock: constructor | destructor | method | friend | opMethod ;

constructor: EXPLICIT? Identifier parameterList SEMICOLON;
destructor: '~' Identifier LPAREN RPAREN SEMICOLON;
method: STATIC? type Identifier parameterList CONST? (block | SEMICOLON);
friend: FRIEND Identifier SEMICOLON;
opMethod: type ( OPEQ | OPLT | OPGT ) parameterList CONST? SEMICOLON ;

parameterList: LPAREN (parameter (parameter)*)? RPAREN;
parameter: type Identifier;

cppClass: CLASS Identifier LCBRACE classBlock RCBRACE SEMICOLON;

block: LCBRACE innerBlock RCBRACE;
innerBlock: (Identifier | ';' | block | type | STATIC) innerBlock;

type: CONST? Identifier Modifier?;

COMMA: ',' -> skip;
LPAREN: '(';
RPAREN: ')';
LCBRACE: '{';
RCBRACE: '}';
SEMICOLON: ';';

FRIEND: 'friend' ;
NAMESPACE: 'namespace' ;
USING: 'using' ;
CLASS: 'class' ;
CONST: 'const' ;
EXPLICIT: 'explicit' ;
STATIC: 'static' ;
OPEQ: 'operator=' ;
OPLT: 'operator<' ;
OPGT: 'operator>' ;

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

